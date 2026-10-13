package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.InventoryItemEntity
import com.example.data.db.PetEntity
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.TextPrimaryDark

@Composable
fun CareActionPanel(
    pet: PetEntity,
    inventory: List<InventoryItemEntity>,
    onWaterSelect: (String) -> Unit,
    onFeedSelect: (String) -> Unit,
    onCleanClick: () -> Unit,
    onToggleLamp: () -> Unit,
    onToggleSleep: () -> Unit,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubMenu by remember { mutableStateOf<String?>(null) } // "WATER", "FOOD", null

    Column(modifier = modifier.fillMaxWidth()) {
        // Sub-menu for item selection
        AnimatedVisibility(
            visible = activeSubMenu != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            val isWater = activeSubMenu == "WATER"
            val categoryItems = inventory.filter { it.category == activeSubMenu }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, MeadowGreen, RoundedCornerShape(18.dp)),
                color = Color.White.copy(alpha = 0.98f),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWater) "💧 Чем польём Цветика?" else "🍓 Какое угощение дадим?",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { activeSubMenu = null },
                            modifier = Modifier.size(28.dp).testTag("close_submenu_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryItems.forEach { item ->
                            val canUse = item.count > 0 || item.id == "pure_water" || item.id == "bio_compost"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (canUse) Color(0xFFF0FDF4) else Color(0xFFF1F5F9))
                                    .border(
                                        1.dp,
                                        if (canUse) MeadowGreen.copy(alpha = 0.5f) else Color(0xFFCBD5E1),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = canUse) {
                                        if (isWater) onWaterSelect(item.id) else onFeedSelect(item.id)
                                        activeSubMenu = null
                                    }
                                    .padding(8.dp)
                                    .testTag("item_select_${item.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = item.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (canUse) TextPrimaryDark else Color.Gray,
                                        maxLines = 1,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (item.id == "pure_water" || item.id == "bio_compost") "∞" else "x${item.count}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MeadowGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary care buttons row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, GardenBorder, RoundedCornerShape(24.dp)),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Water
                CareButton(
                    icon = Icons.Default.WaterDrop,
                    label = "Полить",
                    color = Color(0xFF0284C7),
                    tag = "action_water_button",
                    onClick = { activeSubMenu = if (activeSubMenu == "WATER") null else "WATER" }
                )

                // 2. Feed
                CareButton(
                    icon = Icons.Default.LocalFlorist,
                    label = "Покормить",
                    color = Color(0xFF16A34A),
                    tag = "action_feed_button",
                    onClick = { activeSubMenu = if (activeSubMenu == "FOOD") null else "FOOD" }
                )

                // 3. Clean
                CareButton(
                    icon = Icons.Default.ContentCut,
                    label = "Прополка",
                    color = Color(0xFF0D9488),
                    tag = "action_clean_button",
                    badgeText = if (pet.hygiene < 0.35f) "!" else null,
                    onClick = onCleanClick
                )

                // 4. Pet
                CareButton(
                    icon = Icons.Default.Favorite,
                    label = "Ласка",
                    color = Color(0xFFE11D48),
                    tag = "action_pet_button",
                    onClick = onPetClick
                )

                // 5. Sun toggle
                CareButton(
                    icon = Icons.Default.WbSunny,
                    label = if (pet.isLampOn) "Солнце" else "Тень",
                    color = if (pet.isLampOn) Color(0xFFF59E0B) else Color.Gray,
                    tag = "action_lamp_button",
                    onClick = onToggleLamp
                )

                // 6. Sleep
                CareButton(
                    icon = Icons.Default.Bedtime,
                    label = if (pet.isSleeping) "Разбудить" else "Сон",
                    color = if (pet.isSleeping) Color(0xFF7C3AED) else Color(0xFF64748B),
                    tag = "action_sleep_button",
                    onClick = onToggleSleep
                )
            }
        }
    }
}

@Composable
private fun CareButton(
    icon: ImageVector,
    label: String,
    color: Color,
    tag: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag(tag)
    ) {
        BadgedBox(
            badge = {
                if (badgeText != null) {
                    Badge(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ) {
                        Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.2f), Color.White)
                        )
                    )
                    .border(1.5.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
