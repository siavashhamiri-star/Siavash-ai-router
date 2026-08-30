package com.example.tavanacity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tavanacity.domain.model.UserAccount
import com.example.ui.theme.TavanaPrimary
import com.example.ui.theme.TavanaSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAuthDialog(
    userAccount: UserAccount,
    onDismiss: () -> Unit,
    onLoginPhone: (String, String) -> Unit,
    onLoginEmail: (String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var phoneNumber by remember { mutableStateOf(userAccount.phoneNumber ?: "") }
    var phoneOtp by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf(userAccount.email ?: "") }
    var emailCode by remember { mutableStateOf("") }

    var isOtpSent by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag("account_auth_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = TavanaPrimary
                    )
                    Text(
                        text = "مدیریت حساب کاربری توانا",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Account info summary
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "شناسه کاربری: ${userAccount.userId}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "وضعیت اتصال: ${if (userAccount.isAuthenticated) "متصل به حساب رسمی" else "حساب مهمان (دستگاه)"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (userAccount.isAuthenticated) Color(0xFF2E7D32) else TavanaSecondary
                                )
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                isOtpSent = false
                            },
                            text = { Text("ورود با پیامک (موبایل)") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                isOtpSent = false
                            },
                            text = { Text("ورود با ایمیل") }
                        )
                    }

                    if (selectedTab == 0) {
                        // Phone Auth Tab
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("شماره موبایل (مثال: 09121234567)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )

                        if (!isOtpSent) {
                            Button(
                                onClick = {
                                    if (phoneNumber.isNotBlank()) isOtpSent = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("send_otp_button")
                            ) {
                                Text("دریافت کد تایید پیامکی")
                            }
                        } else {
                            OutlinedTextField(
                                value = phoneOtp,
                                onValueChange = { phoneOtp = it },
                                label = { Text("کد تایید پیامک‌شده") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_otp_input")
                            )

                            Button(
                                onClick = {
                                    onLoginPhone(phoneNumber, phoneOtp)
                                },
                                enabled = phoneNumber.isNotBlank() && phoneOtp.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_phone_login_button")
                            ) {
                                Text("تایید و ورود به حساب")
                            }
                        }
                    } else {
                        // Email Auth Tab
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("آدرس ایمیل") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input")
                        )

                        if (!isOtpSent) {
                            Button(
                                onClick = {
                                    if (emailAddress.isNotBlank()) isOtpSent = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("send_email_code_button")
                            ) {
                                Text("ارسال کد تایید به ایمیل")
                            }
                        } else {
                            OutlinedTextField(
                                value = emailCode,
                                onValueChange = { emailCode = it },
                                label = { Text("کد تایید ایمیل") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_code_input")
                            )

                            Button(
                                onClick = {
                                    onLoginEmail(emailAddress, emailCode)
                                },
                                enabled = emailAddress.isNotBlank() && emailCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_email_login_button")
                            ) {
                                Text("تایید و ورود به حساب")
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "با ورود به حساب، اشتراک‌ها و سکه‌های خریداری‌شده شما امن باقی می‌ماند.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_account_dialog_button")
                ) {
                    Text("بستن")
                }
            }
        )
    }
}
