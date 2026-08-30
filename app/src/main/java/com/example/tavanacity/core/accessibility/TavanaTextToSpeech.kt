package com.example.tavanacity.core.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TavanaTextToSpeech(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("fa"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default or English
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.9f) // A calm, comfortable pace for clarity
            tts?.setPitch(1.0f)
            isInitialized = true
        } else {
            Log.e("TavanaTTS", "TextToSpeech initialization failed")
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotBlank()) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TavanaTTS_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
