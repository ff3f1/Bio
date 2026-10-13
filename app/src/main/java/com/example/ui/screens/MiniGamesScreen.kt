package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GardenBorder
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.TextPrimaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Droplet(
    var x: Float,
    var y: Float,
    val speed: Float,
    val type: DropletType,
    val radius: Float = 18f
)

enum class DropletType(val points: Int, val color: Color, val label: String) {
    WATER(10, Color(0xFF0284C7), "💧"),
    POLLEN(20, Color(0xFFFBBF24), "✨"),
    BERRY(35, Color(0xFFE11D48), "🍓"),
    SLUG(-15, Color(0xFF78350F), "🐌")
}

@Composable
fun MiniGamesScreen(
    onGameFinished: (score: Int, gameName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGameTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .padding(top = 8.dp)
    ) {
        // Tab Header
        Text(
            text = "Садовые Игры",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            text = "Весёлые игры на свежем воздухе для заработка монеток и опыта",
            style = MaterialTheme.typography.bodySmall,
            color = MeadowGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedGameTab,
            containerColor = Color.White,
            contentColor = MeadowGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedGameTab]),
                    color = MeadowGreen
                )
            }
        ) {
            Tab(
                selected = selectedGameTab == 0,
                onClick = { selectedGameTab = 0 },
                text = { Text("Ловец Капель", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_game_droplets")
            )
            Tab(
                selected = selectedGameTab == 1,
                onClick = { selectedGameTab = 1 },
                text = { Text("Цветочная Память", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
                modifier = Modifier.testTag("tab_game_memory")
            )
        }

        if (selectedGameTab == 0) {
            SolarDropletsGame(onFinish = { score -> onGameFinished(score, "Ловец Капель") })
        } else {
            GeneMemoryGame(onFinish = { score -> onGameFinished(score, "Цветочная Память") })
        }
    }
}

// ---------------- MINI-GAME 1: SOLAR DROPLETS ----------------
@Composable
private fun SolarDropletsGame(onFinish: (Int) -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(25) }
    var gameOver by remember { mutableStateOf(false) }

    var basketXFraction by remember { mutableStateOf(0.5f) }
    val droplets = remember { mutableStateListOf<Droplet>() }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        score = 0
        timeLeft = 25
        gameOver = false
        droplets.clear()

        launch {
            while (timeLeft > 0 && isPlaying) {
                delay(1000)
                timeLeft -= 1
            }
            isPlaying = false
            gameOver = true
            onFinish(score.coerceAtLeast(0))
        }

        var tickCount = 0
        while (isPlaying) {
            delay(33)
            tickCount++

            if (tickCount % 22 == 0) {
                val rand = Random.nextFloat()
                val type = when {
                    rand < 0.45f -> DropletType.WATER
                    rand < 0.75f -> DropletType.POLLEN
                    rand < 0.88f -> DropletType.BERRY
                    else -> DropletType.SLUG
                }
                droplets.add(
                    Droplet(
                        x = Random.nextFloat(),
                        y = 0f,
                        speed = Random.nextFloat() * 0.012f + 0.010f,
                        type = type
                    )
                )
            }

            val basketWidth = 0.28f
            val iterator = droplets.iterator()
            while (iterator.hasNext()) {
                val d = iterator.next()
                d.y += d.speed

                if (d.y in 0.84f..0.92f && d.x in (basketXFraction - basketWidth / 2f)..(basketXFraction + basketWidth / 2f)) {
                    score += d.type.points
                    iterator.remove()
                } else if (d.y > 1.0f) {
                    iterator.remove()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0F2FE))
            .border(2.dp, GardenBorder, RoundedCornerShape(20.dp))
    ) {
        if (!isPlaying && !gameOver) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💧 Ловец Садовых Капель",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Двигайте корзинку пальцем влево и вправо!\nЛовите воду 💧 (+10), пыльцу ✨ (+20), спелую земляничку 🍓 (+35).\nНе ловите улитку 🐌 (-15)!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { isPlaying = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("start_droplets_game")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Начать сбор (25 сек)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else if (gameOver) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎉 Время вышло!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Набрано очков: $score",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Награда передана Цветику!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MeadowGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { isPlaying = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Собрать ещё раз", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newFraction = (change.position.x / size.width).coerceIn(0.14f, 0.86f)
                            basketXFraction = newFraction
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Очки: $score",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Время: ${timeLeft}с",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (timeLeft <= 5) Color(0xFFEF4444) else TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Droplets
                    for (d in droplets) {
                        drawCircle(
                            color = d.type.color,
                            radius = d.radius,
                            center = Offset(d.x * w, d.y * h)
                        )
                    }

                    // Draw Wooden Garden Basket
                    val basketWidthPx = w * 0.28f
                    val basketHeightPx = 30f
                    val basketCenterPx = basketXFraction * w
                    val basketTopPx = h * 0.88f

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFB45309))
                        ),
                        topLeft = Offset(basketCenterPx - basketWidthPx / 2f, basketTopPx),
                        size = Size(basketWidthPx, basketHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = Color(0xFF78350F),
                        topLeft = Offset(basketCenterPx - basketWidthPx / 2f, basketTopPx),
                        size = Size(basketWidthPx, basketHeightPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2f)
                    )
                }
            }
        }
    }
}

// ---------------- MINI-GAME 2: GENE MEMORY PUZZLE ----------------
@Composable
private fun GeneMemoryGame(onFinish: (Int) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userStep by remember { mutableIntStateOf(0) }
    var activeNodeIndex by remember { mutableStateOf<Int?>(null) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var totalScore by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }

    fun playNextRound(currentSeq: List<Int>) {
        val nextSeq = currentSeq + Random.nextInt(0, 4)
        sequence = nextSeq
        userStep = 0
        round = nextSeq.size
        isShowingSequence = true

        coroutineScope.launch {
            delay(700)
            for (node in nextSeq) {
                activeNodeIndex = node
                delay(450)
                activeNodeIndex = null
                delay(200)
            }
            isShowingSequence = false
        }
    }

    fun startNewGame() {
        gameStarted = true
        gameOver = false
        totalScore = 0
        playNextRound(emptyList())
    }

    fun onUserTap(nodeIndex: Int) {
        if (isShowingSequence || gameOver || !gameStarted) return

        activeNodeIndex = nodeIndex
        coroutineScope.launch {
            delay(200)
            activeNodeIndex = null
        }

        if (sequence[userStep] == nodeIndex) {
            userStep += 1
            totalScore += 15 * round
            if (userStep == sequence.size) {
                coroutineScope.launch {
                    delay(500)
                    playNextRound(sequence)
                }
            }
        } else {
            gameOver = true
            onFinish(totalScore)
        }
    }

    val nodes = listOf(
        Pair("Ромашка", Color(0xFF22C55E)),
        Pair("Василёк", Color(0xFF0284C7)),
        Pair("Одуванчик", Color(0xFFF59E0B)),
        Pair("Розочка", Color(0xFFF43F5E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(2.dp, GardenBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!gameStarted) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🌸 Цветочная Память",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Смотрите, какие цветочки подмигивают, и повторяйте мелодию лепестков!\nС каждым раундом цепочка удлиняется.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { startNewGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("start_memory_game")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Начать игру", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else if (gameOver) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🌺 Ой, лепесток перепутан!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Пройдено раундов: ${round - 1}\nОчки: $totalScore",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { startNewGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Сыграть снова", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isShowingSequence) "Запоминайте цветочки..." else "Ваш ход: нажимайте лепестки!",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isShowingSequence) Color(0xFFD97706) else MeadowGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Раунд $round | Очки: $totalScore",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FlowerNodeButton(
                            index = 0,
                            name = nodes[0].first,
                            color = nodes[0].second,
                            isActive = activeNodeIndex == 0,
                            onTap = { onUserTap(0) }
                        )
                        FlowerNodeButton(
                            index = 1,
                            name = nodes[1].first,
                            color = nodes[1].second,
                            isActive = activeNodeIndex == 1,
                            onTap = { onUserTap(1) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FlowerNodeButton(
                            index = 2,
                            name = nodes[2].first,
                            color = nodes[2].second,
                            isActive = activeNodeIndex == 2,
                            onTap = { onUserTap(2) }
                        )
                        FlowerNodeButton(
                            index = 3,
                            name = nodes[3].first,
                            color = nodes[3].second,
                            isActive = activeNodeIndex == 3,
                            onTap = { onUserTap(3) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowerNodeButton(
    index: Int,
    name: String,
    color: Color,
    isActive: Boolean,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isActive) color else color.copy(alpha = 0.15f))
            .border(
                2.5.dp,
                if (isActive) Color(0xFFFDE047) else color,
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onTap)
            .testTag("flower_node_$index"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color.White else color)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) Color.White else TextPrimaryDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
