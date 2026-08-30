package com.example.tavanacity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tavanacity.domain.model.AccessibilityConfig
import com.example.ui.theme.TavanaPrimary
import com.example.ui.theme.TavanaSecondary

@Composable
fun AccessibilityComfortDialog(
    config: AccessibilityConfig,
    onToggleLargeFont: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onToggleCalmMode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = TavanaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = "Accessibility",
                            tint = TavanaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "مرکز آرامش و دسترس‌پذیری توانا",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "تنظیمات ویژه عزیزان با توانایی‌های خاص و سالمندان",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Large Font Option
                AccessibilityOptionCard(
                    icon = Icons.Default.FormatSize,
                    title = "درشت‌نمایی متون و دکمه‌ها",
                    description = "افزایش ۲۵ درصدی اندازه فونت‌ها جهت خوانایی آسان‌تر کم‌بینایان",
                    isChecked = config.isLargeFontEnabled,
                    onToggle = onToggleLargeFont,
                    tag = "toggle_large_font"
                )

                // 2. High Contrast Option
                AccessibilityOptionCard(
                    icon = Icons.Default.Visibility,
                    title = "حالت کنتراست شفاف (High Contrast)",
                    description = "کنتراست عمیق مشکی و زرد فسفری جهت تفکیک دقیق محتوا",
                    isChecked = config.isHighContrastEnabled,
                    onToggle = onToggleHighContrast,
                    tag = "toggle_high_contrast"
                )

                // 3. Calm Mental Mode (حالت آرامش اعصاب)
                AccessibilityOptionCard(
                    icon = Icons.Default.Spa,
                    title = "حالت آرامش اعصاب و تمرکز (Calm Mode)",
                    description = "حذف المان‌های شلوغ، پالت رنگی ملایم و پیام‌های بسیار شمرده و صبور",
                    isChecked = config.isCalmModeEnabled,
                    onToggle = onToggleCalmMode,
                    tag = "toggle_calm_mode"
                )

                // 4. Screen reader / TTS guidance
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TavanaSecondary.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "TTS Support",
                            tint = TavanaSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "💡 در کنار هر پیام، دکمه بلندگو 🔊 برای خواندن صوتی شمرده و رسا تعبیه شده است.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("close_accessibility_dialog")
            ) {
                Text(
                    text = "ثبت و بازگشت",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    )
}

@Composable
private fun Surface(
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        shape = shape,
        color = color,
        modifier = modifier,
        content = content
    )
}

@Composable
private fun AccessibilityOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) TavanaPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isChecked) TavanaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isChecked) TavanaPrimary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isChecked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TavanaPrimary
                ),
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
