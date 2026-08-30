package com.example.tavanacity.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.tavanacity.data.local.VaultMessageEntity
import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.NetworkStatus
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusUnstable
import com.example.ui.theme.TavanaPrimary
import com.example.ui.theme.TavanaSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TavanaCityScreen(
    viewModel: RouterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val messages by viewModel.messages.collectAsState()
    val routerState by viewModel.routerState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val reportingMessage by viewModel.reportingMessage.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val showSubscriptionSheet by viewModel.showSubscriptionSheet.collectAsState()
    val showAccountDialog by viewModel.showAccountDialog.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val accessibilityConfig by viewModel.accessibilityConfig.collectAsState()
    val showAccessibilityDialog by viewModel.showAccessibilityDialog.collectAsState()
    val currentlySpeakingMessageId by viewModel.currentlySpeakingMessageId.collectAsState()

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(event.text))
                }
            }
        }
    }

    // Use RTL Layout for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("tavana_city_scaffold"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.router_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                // Active Router Badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TavanaPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "AI Router",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TavanaPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مخزن هوشمند گفت‌وگو (Chat Vault)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
                    actions = {
                        // Plan & Credits Quick Chip (Opens Monetization Sheet)
                        PlanCreditChip(
                            plan = userAccount.plan,
                            creditBalance = userAccount.creditBalance,
                            onClick = { viewModel.openSubscriptionSheet() }
                        )

                        // Real-Time Network Status Indicator Badge
                        NetworkStatusChip(networkStatus = networkStatus)

                        // Accessibility & Calm Center Quick Button
                        IconButton(
                            onClick = { viewModel.openAccessibilityDialog() },
                            modifier = Modifier.testTag("accessibility_settings_button")
                        ) {
                            Icon(
                                imageVector = if (accessibilityConfig.isCalmModeEnabled) Icons.Default.Spa else Icons.Default.Accessibility,
                                contentDescription = "مرکز آرامش و دسترس‌پذیری",
                                tint = if (accessibilityConfig.isLargeFontEnabled || accessibilityConfig.isHighContrastEnabled || accessibilityConfig.isCalmModeEnabled) TavanaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Clear Chat Vault Button
                        IconButton(
                            onClick = { viewModel.openClearDialog() },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.clear_chat),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                MessageInputBar(
                    inputText = inputText,
                    onInputChanged = viewModel::onInputChanged,
                    onSend = { viewModel.sendMessage() },
                    isRouting = routerState.isRouting,
                    networkStatus = networkStatus
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Pipeline & Telemetry Status Strip
                RouterPipelineStatusHeader(
                    routerState = routerState,
                    networkStatus = networkStatus,
                    userAccount = userAccount,
                    onOpenStore = { viewModel.openSubscriptionSheet() }
                )

                // Persona Selector Chips
                PersonaSelectorBar(
                    currentPersona = routerState.currentPersona,
                    userPlan = userAccount.plan,
                    onSelectPersona = { viewModel.selectPersona(it) },
                    enabled = !routerState.isRouting
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )

                // Chat Vault Message Stream
                if (messages.isEmpty()) {
                    EmptyVaultGreeting(
                        currentPersona = routerState.currentPersona,
                        onPromptClick = { prompt ->
                            viewModel.onInputChanged(prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            VaultMessageCard(
                                message = message,
                                isSpeaking = currentlySpeakingMessageId == message.id,
                                onSpeak = {
                                    viewModel.speakMessage(message)
                                },
                                onCopy = {
                                    viewModel.copyMessageText(message.content)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("متن پیام در کلیپ‌بورد کپی شد")
                                    }
                                },
                                onReport = {
                                    viewModel.openReportDialog(message)
                                },
                                onRetry = {
                                    viewModel.retryMessage(message.content)
                                }
                            )
                        }

                        if (routerState.isRouting) {
                            item {
                                RoutingProgressCard(
                                    persona = routerState.currentPersona,
                                    provider = routerState.activeProviderName
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subscription & Monetization Store Modal (Myket In-App Billing)
        if (showSubscriptionSheet) {
            SubscriptionStoreModal(
                userAccount = userAccount,
                isPurchasing = isPurchasing,
                onDismiss = { viewModel.closeSubscriptionSheet() },
                onPurchasePlan = { ctx, plan -> viewModel.purchasePlan(ctx, plan) },
                onPurchaseCreditPack = { ctx, pack -> viewModel.purchaseCreditPack(ctx, pack) },
                onRestorePurchases = { viewModel.restorePurchases() },
                onOpenAccount = {
                    viewModel.closeSubscriptionSheet()
                    viewModel.openAccountDialog()
                }
            )
        }

        // User Account & Auth Dialog (Phone/OTP and Email)
        if (showAccountDialog) {
            AccountAuthDialog(
                userAccount = userAccount,
                onDismiss = { viewModel.closeAccountDialog() },
                onLoginPhone = { phone, otp -> viewModel.loginPhone(phone, otp) },
                onLoginEmail = { email, code -> viewModel.loginEmail(email, code) }
            )
        }

        // Report Message Dialog
        reportingMessage?.let { msg ->
            ReportMessageDialog(
                message = msg,
                onDismiss = { viewModel.closeReportDialog() },
                onSubmit = { reason -> viewModel.submitReport(reason) }
            )
        }

        // Accessibility & Calm Comfort Dialog
        if (showAccessibilityDialog) {
            AccessibilityComfortDialog(
                config = accessibilityConfig,
                onToggleLargeFont = { viewModel.toggleLargeFont() },
                onToggleHighContrast = { viewModel.toggleHighContrast() },
                onToggleCalmMode = { viewModel.toggleCalmMode() },
                onDismiss = { viewModel.closeAccessibilityDialog() }
            )
        }

        // Clear Vault Confirmation Dialog
        if (showClearDialog) {
            ClearVaultConfirmationDialog(
                onConfirm = { viewModel.confirmClearVault() },
                onDismiss = { viewModel.closeClearDialog() }
            )
        }
    }
}

@Composable
fun PlanCreditChip(
    plan: com.example.tavanacity.domain.model.UserPlan,
    creditBalance: Int,
    onClick: () -> Unit
) {
    val numberFormat = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("fa-IR")) }
    val accentColor = when (plan) {
        com.example.tavanacity.domain.model.UserPlan.FREE -> Color(0xFF546E7A)
        com.example.tavanacity.domain.model.UserPlan.PLUS -> Color(0xFF0288D1)
        com.example.tavanacity.domain.model.UserPlan.PRO -> Color(0xFFF57C00)
        com.example.tavanacity.domain.model.UserPlan.PRO_MAX -> Color(0xFF7B1FA2)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("plan_credit_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = plan.titleFa,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall.copy(color = accentColor)
            )
            Text(
                text = "${numberFormat.format(creditBalance)} 🪙",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            )
        }
    }
}

@Composable
fun NetworkStatusChip(networkStatus: NetworkStatus) {
    val (bgColor, dotColor, icon) = when (networkStatus) {
        NetworkStatus.ONLINE -> Triple(
            StatusOnline.copy(alpha = 0.15f),
            StatusOnline,
            Icons.Default.Wifi
        )
        NetworkStatus.UNSTABLE -> Triple(
            StatusUnstable.copy(alpha = 0.15f),
            StatusUnstable,
            Icons.Default.SignalCellularAlt
        )
        NetworkStatus.OFFLINE -> Triple(
            StatusOffline.copy(alpha = 0.15f),
            StatusOffline,
            Icons.Default.WifiOff
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .padding(end = 4.dp)
            .testTag("network_status_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Icon(
                imageVector = icon,
                contentDescription = networkStatus.labelFa,
                tint = dotColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = networkStatus.labelFa,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = dotColor
                )
            )
        }
    }
}

@Composable
fun RouterPipelineStatusHeader(
    routerState: com.example.tavanacity.domain.router.RouterState,
    networkStatus: NetworkStatus,
    userAccount: com.example.tavanacity.domain.model.UserAccount,
    onOpenStore: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Safety badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Safety Layer",
                    tint = TavanaSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "لایحه ایمنی: فعال",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            // Cost per request & Tier info
            val currentTier = routerState.currentPersona.modelTier
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onOpenStore() }
            ) {
                Text(
                    text = "مصرف: ${currentTier.creditCost} سکه (${currentTier.titleFa})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TavanaPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Latency / Provider Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Latency",
                    tint = TavanaPrimary,
                    modifier = Modifier.size(14.dp)
                )
                val latencyText = routerState.lastRoutingDurationMs?.let { "${it}ms" } ?: "آماده"
                Text(
                    text = "تاخیر: $latencyText",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun PersonaSelectorBar(
    currentPersona: AIPersona,
    userPlan: com.example.tavanacity.domain.model.UserPlan,
    onSelectPersona: (AIPersona) -> Unit,
    enabled: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AIPersona.entries.toTypedArray()) { persona ->
            val isSelected = persona == currentPersona
            val hasAccess = userPlan.canAccessModelTier(persona.modelTier)

            val icon = when (persona) {
                AIPersona.GENERAL_ASSISTANT -> Icons.Default.Assistant
                AIPersona.TECHNICAL_EXPERT -> Icons.Default.Code
                AIPersona.SMART_CITY_GUIDE -> Icons.Default.LocationCity
                AIPersona.DATA_ANALYST -> Icons.Default.Analytics
                AIPersona.CODER -> Icons.Default.Code
                AIPersona.EMPATHETIC -> Icons.Default.Assistant
                AIPersona.CREATIVE -> Icons.Default.Speed
                AIPersona.CRITIC -> Icons.Default.Security
                AIPersona.ACCESSIBILITY_CALM -> Icons.Default.Spa
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) TavanaPrimary else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasAccess) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    else Color.Red.copy(alpha = 0.3f)
                ),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onSelectPersona(persona) }
                    .testTag("persona_chip_${persona.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (!hasAccess) Icons.Default.Lock else icon,
                        contentDescription = persona.titleFa,
                        tint = if (isSelected) Color.White else if (!hasAccess) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = persona.titleFa,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    // Tier indicator
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = persona.modelTier.titleFa,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultMessageCard(
    message: VaultMessageEntity,
    isSpeaking: Boolean = false,
    onSpeak: () -> Unit = {},
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onRetry: () -> Unit
) {
    val isUser = message.isUser()
    val isSystem = message.isSystem()

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) {
        timeFormatter.format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_card_${message.id}"),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> MaterialTheme.colorScheme.primaryContainer
                    message.isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    isSystem -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 2.dp),
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Header (Persona name or User indicator)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isUser) {
                            Icon(
                                imageVector = when (message.personaId) {
                                    AIPersona.TECHNICAL_EXPERT.id -> Icons.Default.Code
                                    AIPersona.SMART_CITY_GUIDE.id -> Icons.Default.LocationCity
                                    AIPersona.DATA_ANALYST.id -> Icons.Default.Analytics
                                    AIPersona.ACCESSIBILITY_CALM.id -> Icons.Default.Spa
                                    else -> Icons.Default.Assistant
                                },
                                contentDescription = null,
                                tint = TavanaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isUser) "شما" else message.personaTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else TavanaPrimary
                            )
                        )
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Message Text with Selectable Content
                SelectionContainer {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // If reported badge
                if (message.isReported) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StatusUnstable.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Reported",
                                tint = StatusUnstable,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "گزارش شده: ${message.reportReason ?: "بازخورد کاربر"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = StatusUnstable,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Metadata & Action Buttons (Copy / Report)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Provider & Latency Info (for AI)
                    if (!isUser && message.providerUsed != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = message.providerUsed,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            if (message.latencyMs != null && message.latencyMs > 0) {
                                Text(
                                    text = "${message.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Action Controls: Speak, Copy & Report
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Text to Speech Button (شنیدن صوتی شمرده پیام)
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("speak_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "توقف خوانش صوتی" else "شنیدن صوتی پیام",
                                tint = if (isSpeaking) TavanaPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy Button (علامت کپی)
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.copy_text),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Report Button (علامت گزارش) - For AI messages
                        if (!isUser) {
                            IconButton(
                                onClick = onReport,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("report_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (message.isReported) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                    contentDescription = stringResource(R.string.report_message),
                                    tint = if (message.isReported) StatusUnstable else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Retry Button (if error)
                        if (message.isError) {
                            IconButton(
                                onClick = onRetry,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("retry_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.retry),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutingProgressCard(
    persona: AIPersona,
    provider: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.5.dp,
                    color = TavanaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "در حال مسیریابی و پردازش پیام...",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${persona.titleFa} | $provider",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyVaultGreeting(
    currentPersona: AIPersona,
    onPromptClick: (String) -> Unit
) {
    val samplePrompts = remember(currentPersona) {
        when (currentPersona) {
            AIPersona.GENERAL_ASSISTANT -> listOf(
                "خدمات اصلی اکوسیستم توانا سیتی چیست؟",
                "نحوه عملکرد مسیریاب هوشمند را توضیح بده.",
                "یک برنامه کاری روزانه برای مدیریت پروژه‌ها بنویس."
            )
            AIPersona.TECHNICAL_EXPERT -> listOf(
                "الگوی معماری MVVM و Clean Architecture در اندروید را شرح بده.",
                "نحوه مدیریت خطا با Sealed Classes در کاتلین چگونه است؟",
                "چگونه از StateFlow و Coroutines برای Thread Safety استفاده کنیم؟"
            )
            AIPersona.SMART_CITY_GUIDE -> listOf(
                "فرایند احراز هویت و دسترسی به خدمات شهروندی توانا چیست؟",
                "راهکارهای بهینه‌سازی ترافیک و انرژی در شهر هوشمند توانا چیست؟",
                "چگونه درخواست بازخورد شهری ثبت کنم؟"
            )
            AIPersona.DATA_ANALYST -> listOf(
                "شاخص‌های کلیدی عملکرد (KPI) در پردازش درخواست‌های AI چیست؟",
                "روش‌های تحلیل تاخیر شبکه و بهینه‌سازی Fallback را مقایسه کن.",
                "یک ساختار گزارش تحلیلی برای عملکرد سرور پیشنهاد بده."
            )
            AIPersona.CODER -> listOf(
                "یک تابع بهینه برای مدیریت خطاهای شبکه در کاتلین بنویس.",
                "نحوه پیاده‌سازی Repository Pattern در اندروید را شرح بده.",
                "یک الگوی تمیز برای اعتبارسنجی ورودی کاربر پیاده کن."
            )
            AIPersona.EMPATHETIC -> listOf(
                "امروز روز پرفشاری داشتم، چطور می‌توانم تمرکزم را بازیابی کنم؟",
                "چگونه می‌توان در محیط کاری چالش‌برانگیز انگیزه را حفظ کرد؟",
                "یک راهکار آرامش‌بخش برای کاهش استرس ارائه بده."
            )
            AIPersona.CREATIVE -> listOf(
                "یک ایده نوآورانه برای ارتقای خدمات هوشمند شهری پیشنهاد بده.",
                "یک سناریوی جذاب برای معرفی هوش مصنوعی به شهروندان بنویس.",
                "ایده‌هایی برای توسعه پایدار و انرژی پاک در کلان‌شهرها مطرح کن."
            )
            AIPersona.CRITIC -> listOf(
                "ریسک‌ها و نقاط ضعف احتمالی سیستم‌های هوش مصنوعی بدون Fallback چیست؟",
                "نقدهای وارد بر معماری‌های ابری متمرکز را بررسی کن.",
                "سوگیری‌های رایج در تصمیم‌گیری‌های داده‌محور را نقد کن."
            )
            AIPersona.ACCESSIBILITY_CALM -> listOf(
                "لطفاً یک متن آرامش‌بخش و ساده برای شروع روز بگو.",
                "چگونه می‌توانم از امکانات دسترس‌پذیری و صوتی برنامه بهتر استفاده کنم؟",
                "یک پیام انرژی‌بخش، شمرده و دلنشین برای من بنویس."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = TavanaPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Assistant,
                    contentDescription = null,
                    tint = TavanaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "مسیریاب هوشمند Tavana City",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = currentPersona.descriptionFa,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "پرسش‌های پیشنهادی برای شروع:",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        for (prompt in samplePrompts) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPromptClick(prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Select",
                        tint = TavanaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isRouting: Boolean,
    networkStatus: NetworkStatus
) {
    val context = LocalContext.current
    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenTextList = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenTextList?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val combined = if (inputText.isNotBlank()) "$inputText $spokenText" else spokenText
                onInputChanged(combined)
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.input_placeholder),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TavanaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                )

                // Voice Speech-To-Text Microphone Button (ورودی صوتی برای معلولان و راحتی کاربر)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(
                                        android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "fa")
                                    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "لطفاً با آرامش پیام خود را بیان نمایید...")
                                }
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "سرویس تبدیل گفتار به متن فعال نیست", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ورودی صوتی گفتار به متن",
                            tint = TavanaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (inputText.isNotBlank() && !isRouting) TavanaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = onSend,
                        enabled = inputText.isNotBlank() && !isRouting,
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        if (isRouting) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = TavanaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.send_button),
                                tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMessageDialog(
    message: VaultMessageEntity,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val reportReasons = listOf(
        stringResource(R.string.report_inaccurate),
        stringResource(R.string.report_inappropriate),
        stringResource(R.string.report_quality),
        stringResource(R.string.report_technical)
    )

    var selectedReason by remember { mutableStateOf(reportReasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = StatusUnstable
                )
                Text(
                    text = stringResource(R.string.report_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.report_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text(text = stringResource(R.string.report_submit))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_report_button")
            ) {
                Text(text = stringResource(R.string.report_cancel))
            }
        }
    )
}

@Composable
fun ClearVaultConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.clear_chat),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.clear_chat_confirm),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_clear_button")
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_button")
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
