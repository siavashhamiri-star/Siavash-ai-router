package com.example.tavanacity.ui

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.tavanacity.data.billing.CreditPackage
import com.example.tavanacity.domain.model.UserAccount
import com.example.tavanacity.domain.model.UserPlan
import com.example.ui.theme.TavanaPrimary
import com.example.ui.theme.TavanaSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionStoreModal(
    userAccount: UserAccount,
    isPurchasing: Boolean,
    onDismiss: () -> Unit,
    onPurchasePlan: (Context, UserPlan) -> Unit,
    onPurchaseCreditPack: (Context, CreditPackage) -> Unit,
    onRestorePurchases: () -> Unit,
    onOpenAccount: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("subscription_modal_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TavanaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = TavanaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.subscription_store_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.subscription_store_desc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_store_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Account & Active Entitlement Summary Banner
                UserEntitlementSummaryCard(
                    userAccount = userAccount,
                    onOpenAccount = onOpenAccount
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Plans vs Top-up Packs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "طرح‌های اشتراک ماهانه",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "بسته‌های افزایش اعتبار",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        items(UserPlan.entries.toTypedArray()) { plan ->
                            PlanCardItem(
                                plan = plan,
                                currentPlan = userAccount.plan,
                                isPurchasing = isPurchasing,
                                onSelect = { onPurchasePlan(context, plan) }
                            )
                        }
                    } else {
                        items(CreditPackage.entries.toTypedArray()) { pack ->
                            CreditPackCardItem(
                                pack = pack,
                                isPurchasing = isPurchasing,
                                onSelect = { onPurchaseCreditPack(context, pack) }
                            )
                        }
                    }

                    // Restore Purchases & Security Footer
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRestorePurchases,
                                enabled = !isPurchasing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("restore_purchases_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.restore_purchases),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TavanaSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "تاییدیه آنی مایکت + جلوگیری از فعال‌سازی جعلی در سرور",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
fun UserEntitlementSummaryCard(
    userAccount: UserAccount,
    onOpenAccount: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("fa-IR")) }
    val timeFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("fa-IR")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TavanaPrimary.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TavanaPrimary.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "پلن فعلی: ${userAccount.plan.titleFa}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (userAccount.plan) {
                            UserPlan.FREE -> Color.Gray.copy(alpha = 0.2f)
                            UserPlan.PLUS -> Color(0xFF1976D2).copy(alpha = 0.2f)
                            UserPlan.PRO -> Color(0xFFF57C00).copy(alpha = 0.2f)
                            UserPlan.PRO_MAX -> Color(0xFF7B1FA2).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = userAccount.entitlementStatus.titleFa,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "موجودی: ${numberFormat.format(userAccount.creditBalance)} سکه",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    )
                }

                userAccount.subscriptionEnd?.let { expiry ->
                    Text(
                        text = "انقضای اشتراک: ${timeFormatter.format(Date(expiry))}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenAccount,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_account_from_sheet")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "حساب کاربری",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun PlanCardItem(
    plan: UserPlan,
    currentPlan: UserPlan,
    isPurchasing: Boolean,
    onSelect: () -> Unit
) {
    val isCurrent = plan == currentPlan
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("fa-IR")) }

    val accentColor = when (plan) {
        UserPlan.FREE -> Color(0xFF546E7A)
        UserPlan.PLUS -> Color(0xFF0288D1)
        UserPlan.PRO -> Color(0xFFF57C00)
        UserPlan.PRO_MAX -> Color(0xFF7B1FA2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrent) 2.dp else 1.dp,
            if (isCurrent) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (plan == UserPlan.PRO_MAX) Icons.Default.Diamond else Icons.Default.Star,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = plan.titleFa,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    )
                }

                Text(
                    text = if (plan.priceTomans == 0L) "رایگان" else "${numberFormat.format(plan.priceTomans)} تومان / ماه",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Text(
                text = "سهمیه روزانه: ${numberFormat.format(plan.dailyCredits)} سکه هوش مصنوعی (اولویت ${plan.priorityFa})",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Feature list
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                plan.featuresFa.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = feat,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            if (isCurrent) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "طرح فعال شما",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        )
                    }
                }
            } else if (plan != UserPlan.FREE) {
                Button(
                    onClick = onSelect,
                    enabled = !isPurchasing,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buy_plan_${plan.id}")
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "خرید اشتراک از طریق مایکت",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun CreditPackCardItem(
    pack: CreditPackage,
    isPurchasing: Boolean,
    onSelect: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("fa-IR")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credit_pack_${pack.sku}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = pack.titleFa,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    pack.badgeFa?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE91E63),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${numberFormat.format(pack.credits)} سکه مصرفی (بدون تاریخ انقضا)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${numberFormat.format(pack.priceTomans)} تومان",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TavanaPrimary
                    )
                )
            }

            Button(
                onClick = onSelect,
                enabled = !isPurchasing,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TavanaPrimary),
                modifier = Modifier.testTag("buy_pack_${pack.sku}")
            ) {
                Text(
                    text = "خرید در مایکت",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
