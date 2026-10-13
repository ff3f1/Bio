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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PetEntity
import com.example.model.BiomeTheme
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.TextPrimaryDark

@Composable
fun BiomesScreen(
    pet: PetEntity,
    onSelectBiome: (String) -> Unit,
    onUnlockBiome: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedSet = pet.unlockedBiomes.split(",").toSet()
    val biomes = BiomeTheme.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .padding(16.dp)
    ) {
        Text(
            text = "Садовые Уголки",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Выберите красивое место и фон для вашего цветочка",
            style = MaterialTheme.typography.bodySmall,
            color = MeadowGreen,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(biomes) { biome ->
                val isUnlocked = unlockedSet.contains(biome.id)
                val isActive = pet.activeBiome == biome.id
                val canUnlock = pet.level >= biome.unlockLevel && pet.bioCredits >= biome.unlockCost

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("biome_card_${biome.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFFFEFCE8) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (isActive) MeadowGreen else GardenBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = biome.titleRu,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MeadowGreen)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "АКТИВЕН",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = biome.descriptionRu,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Нужен ${biome.unlockLevel} ур.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Button(
                                    onClick = { onUnlockBiome(biome.id) },
                                    enabled = canUnlock,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MeadowGreen,
                                        disabledContainerColor = Color(0xFFE2E8F0)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("unlock_biome_${biome.id}")
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFEF08A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${biome.unlockCost}",
                                        color = if (canUnlock) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (!isActive) {
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = { onSelectBiome(biome.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("select_biome_${biome.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Выбрать сад", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
