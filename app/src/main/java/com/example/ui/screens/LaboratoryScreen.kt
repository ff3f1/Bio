package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.InventoryItemEntity
import com.example.data.db.PetEntity
import com.example.data.db.UpgradeEntity
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.TextPrimaryDark

@Composable
fun LaboratoryScreen(
    pet: PetEntity,
    upgrades: List<UpgradeEntity>,
    inventory: List<InventoryItemEntity>,
    onBuyUpgrade: (String) -> Unit,
    onBuyItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .padding(top = 8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Садовая Мастерская",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Улучшение клумбы и садовая лавка",
                    style = MaterialTheme.typography.bodySmall,
                    color = MeadowGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Coins Wallet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, SunnyYellow, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${pet.bioCredits}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MeadowGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MeadowGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Улучшения сада", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_upgrades")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Садовая Лавка", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_shop")
            )
        }

        if (selectedTab == 0) {
            UpgradesList(
                upgrades = upgrades,
                currentCredits = pet.bioCredits,
                onBuyUpgrade = onBuyUpgrade
            )
        } else {
            ShopItemsList(
                inventory = inventory.filter { it.price > 0 },
                currentCredits = pet.bioCredits,
                onBuyItem = onBuyItem
            )
        }
    }
}

@Composable
private fun UpgradesList(
    upgrades: List<UpgradeEntity>,
    currentCredits: Int,
    onBuyUpgrade: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upgrades, key = { it.id }) { upgrade ->
            UpgradeCard(
                upgrade = upgrade,
                canAfford = currentCredits >= upgrade.currentCost && !upgrade.isMaxed,
                onBuy = { onBuyUpgrade(upgrade.id) }
            )
        }
    }
}

@Composable
private fun UpgradeCard(
    upgrade: UpgradeEntity,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upgrade_card_${upgrade.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GardenBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    val icon = when (upgrade.iconKey) {
                        "WATER" -> Icons.Default.WaterDrop
                        "LIGHT" -> Icons.Default.WbSunny
                        "CLEAN" -> Icons.Default.ContentCut
                        "ENERGY" -> Icons.Default.Fence
                        else -> Icons.Default.AutoAwesome
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                            .border(1.5.dp, MeadowGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MeadowGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = upgrade.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = upgrade.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level pips + Buy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(upgrade.maxLevel) { index ->
                        val isFilled = index < upgrade.level
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isFilled) MeadowGreen else Color(0xFFE2E8F0))
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (upgrade.isMaxed) "МАКС" else "${upgrade.level}/${upgrade.maxLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (upgrade.isMaxed) MeadowGreen else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (upgrade.isMaxed) {
                    Text(
                        text = "Куплено",
                        style = MaterialTheme.typography.labelMedium,
                        color = MeadowGreen,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = onBuy,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeadowGreen,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("buy_upgrade_${upgrade.id}")
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${upgrade.currentCost}",
                            color = if (canAfford) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopItemsList(
    inventory: List<InventoryItemEntity>,
    currentCredits: Int,
    onBuyItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(inventory, key = { it.id }) { item ->
            val canAfford = currentCredits >= item.price
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_item_${item.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GardenBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.iconEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "В сарае: ${item.count} шт.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MeadowGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onBuyItem(item.id) },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeadowGreen,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("buy_item_${item.id}")
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.price}",
                            color = if (canAfford) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
