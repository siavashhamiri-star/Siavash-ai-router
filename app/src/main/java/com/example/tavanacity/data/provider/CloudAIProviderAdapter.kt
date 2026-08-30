package com.example.tavanacity.data.provider

import android.os.SystemClock
import android.util.Log
import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AIResponse
import com.example.tavanacity.domain.model.RouterError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class CloudAIProviderAdapter(
    private val apiKeyProvider: () -> String = { getApiKeyFromBuildConfig() },
    private val customBaseUrl: String? = null,
    private val gatewayEndpoint: String? = null,
    private val baseClient: OkHttpClient = OkHttpClient.Builder().build()
) : AIProvider {

    override val id: String = "cloud_gemini_primary"
    override val displayName: String = "Cloud AI (Gemini / Gateway)"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateResponse(
        prompt: String,
        persona: AIPersona,
        timeoutMs: Long
    ): Result<AIResponse> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()

        // If neither gateway endpoint nor valid API key is present, fail securely with AuthenticationError
        if (gatewayEndpoint.isNullOrBlank() && (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY")) {
            Log.w(TAG, "API Key is missing or placeholder and no gatewayEndpoint is specified.")
            return@withContext Result.failure(
                RouterError.AuthenticationError(
                    technicalLog = "Gemini API key is missing or default placeholder, and no custom gateway is configured.",
                    userMessageFa = "کلید دسترسی سرویس هوش مصنوعی ابری تنظیم نشده است."
                )
            )
        }

        val startTime = SystemClock.elapsedRealtime()

        // Build client with exact dynamic timeout
        val client = baseClient.newBuilder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMs + 1500L, TimeUnit.MILLISECONDS)
            .build()

        val targetUrl = when {
            !gatewayEndpoint.isNullOrBlank() -> gatewayEndpoint
            !customBaseUrl.isNullOrBlank() -> customBaseUrl
            else -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        }

        // Construct standard REST payload with persona system prompt and user contents
        val requestPayload = try {
            JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", persona.systemPrompt)
                        })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }.toString()
        } catch (e: Exception) {
            return@withContext Result.failure(
                RouterError.UnknownError(
                    technicalLog = "Failed to construct JSON payload: ${e.message}",
                    userMessageFa = "خطا در آماده‌سازی داده‌های ارسالی به هوش مصنوعی."
                )
            )
        }

        val request = Request.Builder()
            .url(targetUrl)
            .post(requestPayload.toRequestBody(jsonMediaType))
            .apply {
                if (!gatewayEndpoint.isNullOrBlank() && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    addHeader("Authorization", "Bearer $apiKey")
                }
            }
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val latency = SystemClock.elapsedRealtime() - startTime
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val code = response.code
                    Log.e(TAG, "HTTP failure $code received from AI Provider")
                    return@withContext when (code) {
                        401, 403 -> Result.failure(
                            RouterError.AuthenticationError(
                                technicalLog = "HTTP $code - Invalid API Key or Unauthorized",
                                userMessageFa = "اعتبارسنجی سرویس ابری ناموفق بود (کد $code)."
                            )
                        )
                        429 -> Result.failure(
                            RouterError.ProviderError(
                                httpCode = code,
                                technicalLog = "HTTP 429 - Rate limit exceeded on AI Provider",
                                userMessageFa = "سقف مجاز ارسال درخواست به پایان رسید. لطفاً لحظاتی دیگر تلاش کنید."
                            )
                        )
                        else -> Result.failure(
                            RouterError.ProviderError(
                                httpCode = code,
                                technicalLog = "HTTP $code - Server error from AI Provider",
                                userMessageFa = "خطای سرور ابری ($code). لطفاً مجدداً تلاش کنید."
                            )
                        )
                    }
                }

                if (responseBody.isBlank()) {
                    return@withContext Result.failure(
                        RouterError.ProviderError(
                            httpCode = response.code,
                            technicalLog = "Empty response body received from provider",
                            userMessageFa = "پاسخ دریافتی از سرور خالی بود."
                        )
                    )
                }

                val text = extractTextFromGeminiResponse(responseBody)
                if (text.isNullOrBlank()) {
                    return@withContext Result.failure(
                        RouterError.ProviderError(
                            httpCode = response.code,
                            technicalLog = "Malformed response or missing candidate text parts in JSON",
                            userMessageFa = "پاسخ دریافتی از هوش مصنوعی فاقد محتوای معتبر بود."
                        )
                    )
                }

                Result.success(
                    AIResponse(
                        text = text.trim(),
                        latencyMs = latency,
                        providerName = displayName
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException after ${timeoutMs}ms")
            Result.failure(
                RouterError.TimeoutError(
                    technicalLog = "SocketTimeoutException after ${timeoutMs}ms",
                    userMessageFa = "زمان انتظار برای دریافت پاسخ (${timeoutMs / 1000} ثانیه) به پایان رسید."
                )
            )
        } catch (e: IOException) {
            Log.e(TAG, "IOException contacting AI Provider: ${e.javaClass.simpleName}")
            Result.failure(
                RouterError.NetworkError(
                    technicalLog = "IOException: ${e.message}",
                    userMessageFa = "خطای اتصال شبکه در برقراری ارتباط با سرویس هوش مصنوعی."
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception contacting AI Provider: ${e.javaClass.simpleName}")
            Result.failure(
                RouterError.UnknownError(
                    technicalLog = "Exception: ${e.message}",
                    userMessageFa = "خطای پیش‌بینی‌نشده در دریافت پاسخ هوش مصنوعی."
                )
            )
        }
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String? {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val stringBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val partObj = parts.getJSONObject(i)
                val partText = partObj.optString("text", "")
                stringBuilder.append(partText)
            }
            stringBuilder.toString().ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "CloudAIProvider"

        fun getApiKeyFromBuildConfig(): String {
            return try {
                val clazz = Class.forName("com.example.BuildConfig")
                val field = clazz.getField("GEMINI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }
}
