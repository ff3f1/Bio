package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.db.PetEntity
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.StatClean
import com.example.ui.theme.StatEnergy
import com.example.ui.theme.StatFood
import com.example.ui.theme.StatHappy
import com.example.ui.theme.StatLight
import com.example.ui.theme.StatWater
import com.example.ui.theme.TextPrimaryDark

@Composable
fun StatusMetersPanel(
    pet: PetEntity,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.5.dp, GardenBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("status_meters_panel")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMeterItem(
                    label = "Водичка",
                    value = pet.hydration,
                    color = StatWater,
                    icon = Icons.Default.WaterDrop,
                    modifier = Modifier.weight(1f),
                    tag = "meter_hydration"
                )
                StatMeterItem(
                    label = "Питание",
                    value = pet.nutrition,
                    color = StatFood,
                    icon = Icons.Default.LocalFlorist,
                    modifier = Modifier.weight(1f),
                    tag = "meter_nutrition"
                )
                StatMeterItem(
                    label = "Солнышко",
                    value = pet.lightLevel,
                    color = StatLight,
                    icon = Icons.Default.WbSunny,
                    modifier = Modifier.weight(1f),
                    tag = "meter_light"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMeterItem(
                    label = "Прополка",
                    value = pet.hygiene,
                    color = StatClean,
                    icon = Icons.Default.ContentCut,
                    modifier = Modifier.weight(1f),
                    tag = "meter_hygiene"
                )
                StatMeterItem(
                    label = "Энергия",
                    value = pet.energy,
                    color = StatEnergy,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f),
                    tag = "meter_energy"
                )
                StatMeterItem(
                    label = "Счастье",
                    value = pet.happiness,
                    color = StatHappy,
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f),
                    tag = "meter_happiness"
                )
            }
        }
    }
}

@Composable
private fun StatMeterItem(
    label: String,
    value: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tag: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "MeterProgress"
    )

    val isCritical = value < 0.25f
    val barColor by animateColorAsState(
        targetValue = if (isCritical) Color(0xFFEF4444) else color,
        animationSpec = tween(300),
        label = "MeterColor"
    )

    Column(
        modifier = modifier.testTag(tag),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = barColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isCritical) Color(0xFFEF4444) else Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.8f), barColor)
                        )
                    )
            )
        }
    }
}
