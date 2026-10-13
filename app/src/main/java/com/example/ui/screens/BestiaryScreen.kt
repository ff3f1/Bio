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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import com.example.data.db.JournalEntryEntity
import com.example.data.db.PetEntity
import com.example.model.EvolutionStage
import com.example.model.MutationType
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.TextPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BestiaryScreen(
    pet: PetEntity,
    journalEntries: List<JournalEntryEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Садовый Гербарий",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            text = "Стадии цветения, редкие сорта и дневник садовода",
            style = MaterialTheme.typography.bodySmall,
            color = MeadowGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                text = { Text("Стадии Цветения", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_stages")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Сорта", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_mutations")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Дневник (${journalEntries.size})", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_journal")
            )
        }

        when (selectedTab) {
            0 -> EvolutionStagesList(currentLevel = pet.level)
            1 -> MutationsList(activeMutationId = pet.mutationType)
            2 -> JournalList(entries = journalEntries)
        }
    }
}

@Composable
private fun EvolutionStagesList(currentLevel: Int) {
    val stages = EvolutionStage.values()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stages) { stage ->
            val isUnlocked = currentLevel >= stage.minLevel
            Card(
                modifier = Modifier.fillMaxWidth().testTag("stage_card_${stage.name}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color.White else Color(0xFFF1F5F9)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isUnlocked) GardenBorder else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .background(if (isUnlocked) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked) MeadowGreen else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stage.titleRu,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isUnlocked) TextPrimaryDark else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stage.subtitleRu,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUnlocked) MeadowGreen else Color.DarkGray
                                )
                            }
                        }

                        Text(
                            text = if (isUnlocked) "Выращено!" else "С ${stage.minLevel} ур.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnlocked) MeadowGreen else Color(0xFFD97706),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stage.descriptionRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUnlocked) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun MutationsList(activeMutationId: String) {
    val mutations = MutationType.values().filter { it != MutationType.NONE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mutations) { mutation ->
            val isActive = activeMutationId == mutation.id
            Card(
                modifier = Modifier.fillMaxWidth().testTag("mutation_card_${mutation.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFFFEFCE8) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isActive) Color(0xFFF59E0B) else GardenBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mutation.titleRu,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF59E0B))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "АКТИВЕН",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Особенность: ${mutation.bonusRu}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MeadowGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mutation.descriptionRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalList(entries: List<JournalEntryEntity>) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GardenBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(entry.timestamp)),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
