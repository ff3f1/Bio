package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.InventoryItemEntity
import com.example.data.db.PetEntity
import com.example.model.EvolutionStage
import com.example.model.GameBalance
import com.example.model.MutationType
import com.example.model.PetMood
import com.example.ui.components.CareActionPanel
import com.example.ui.components.StatusMetersPanel
import com.example.ui.components.TerrariumCanvas
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.TextPrimaryDark

@Composable
fun MainTerrariumScreen(
    pet: PetEntity,
    mood: PetMood,
    inventory: List<InventoryItemEntity>,
    onPetTouched: () -> Unit,
    onWaterSelect: (String) -> Unit,
    onFeedSelect: (String) -> Unit,
    onCleanClick: () -> Unit,
    onToggleLamp: () -> Unit,
    onToggleSleep: () -> Unit,
    onNavigateToLab: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val stage = EvolutionStage.valueOf(pet.evolutionStage)
    val mutation = MutationType.values().find { it.id == pet.mutationType } ?: MutationType.NONE
    val requiredXp = GameBalance.getRequiredXp(pet.level)
    val xpProgress = (pet.experience.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFE0F2FE))) {
        // 1. Terrarium Viewport
        TerrariumCanvas(
            pet = pet,
            mood = mood,
            modifier = Modifier.fillMaxSize(),
            onPetTouched = onPetTouched
        )

        // 2. Overlay HUD Elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TopStatusHeader(
                    pet = pet,
                    stage = stage,
                    mutation = mutation,
                    mood = mood,
                    xpProgress = xpProgress,
                    requiredXp = requiredXp,
                    onLabClick = onNavigateToLab
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom HUD
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (mood == PetMood.THIRSTY || mood == PetMood.HUNGRY || mood == PetMood.SICK) {
                    AlertBanner(mood = mood)
                }

                StatusMetersPanel(pet = pet)

                CareActionPanel(
                    pet = pet,
                    inventory = inventory,
                    onWaterSelect = onWaterSelect,
                    onFeedSelect = onFeedSelect,
                    onCleanClick = onCleanClick,
                    onToggleLamp = onToggleLamp,
                    onToggleSleep = onToggleSleep,
                    onPetClick = onPetTouched
                )
            }
        }
    }
}

@Composable
private fun TopStatusHeader(
    pet: PetEntity,
    stage: EvolutionStage,
    mutation: MutationType,
    mood: PetMood,
    xpProgress: Float,
    requiredXp: Int,
    onLabClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, GardenBorder, RoundedCornerShape(20.dp)),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Row 1: Name, Stage, Coins
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFEF08A), MeadowGreen)
                                )
                            )
                            .border(1.5.dp, MeadowGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pet.level}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            if (mutation != MutationType.NONE) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFDE047))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = mutation.titleRu,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color(0xFF78350F),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = stage.titleRu,
                            style = MaterialTheme.typography.labelSmall,
                            color = MeadowGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Currency Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.dp, SunnyYellow, RoundedCornerShape(14.dp))
                        .clickable(onClick = onLabClick)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("credits_wallet_chip")
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Монетки",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pet.bioCredits}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: XP Bar & Mood Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Опыт: ${pet.experience} / $requiredXp",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(xpProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MeadowGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MeadowGreen,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }

                // Mood Indicator Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(mood.colorHex).copy(alpha = 0.12f))
                        .border(1.dp, Color(mood.colorHex).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = mood.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mood.titleRu,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(mood.colorHex),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertBanner(mood: PetMood) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = mood.iconEmoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (mood) {
                    PetMood.THIRSTY -> "Цветик хочет пить! Полейте его из леечки 💧"
                    PetMood.HUNGRY -> "Цветик проголодался! Пора дать ягодный нектар 🍓"
                    PetMood.SICK -> "В горшочке выросли сорняки! Пора сделать прополку ✂️"
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
