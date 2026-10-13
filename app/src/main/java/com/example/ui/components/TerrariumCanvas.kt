package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.db.PetEntity
import com.example.model.BiomeTheme
import com.example.model.EvolutionStage
import com.example.model.MutationType
import com.example.model.PetMood
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PollenParticle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val speedX: Float,
    val alpha: Float,
    val color: Color
)

data class HeartEffect(
    val id: Long,
    val startX: Float,
    val startY: Float,
    var currentY: Float,
    var alpha: Float,
    val color: Color
)

@Composable
fun TerrariumCanvas(
    pet: PetEntity,
    mood: PetMood,
    modifier: Modifier = Modifier,
    onPetTouched: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GardenAnim")

    // Cute breathing / bounce
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PetBreath"
    )

    // Gentle wind sway
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -4.5f,
        targetValue = 4.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlowerSway"
    )

    // Sun rays rotation
    val sunRayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRays"
    )

    // Butterfly wing flutter
    val butterflyFlutter by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Butterfly"
    )

    // Blinking logic
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(pet.isSleeping) {
        if (!pet.isSleeping) {
            while (true) {
                delay(Random.nextLong(2200, 4800))
                isBlinking = true
                delay(150)
                isBlinking = false
            }
        }
    }

    // Drifting flower petals & pollen seeds
    val pollenParticles = remember {
        mutableStateListOf<PollenParticle>().apply {
            repeat(20) {
                add(
                    PollenParticle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        radius = Random.nextFloat() * 4f + 2f,
                        speedY = Random.nextFloat() * 0.0012f + 0.0006f,
                        speedX = (Random.nextFloat() - 0.3f) * 0.001f,
                        alpha = Random.nextFloat() * 0.5f + 0.3f,
                        color = listOf(
                            Color(0xFFFEF08A),
                            Color(0xFFFBCFE8),
                            Color(0xFFFFFFFF),
                            Color(0xFFBAE6FD)
                        ).random()
                    )
                )
            }
        }
    }

    // Touch hearts & flower petals
    val heartEffects = remember { mutableStateListOf<HeartEffect>() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            val iterator = heartEffects.iterator()
            while (iterator.hasNext()) {
                val h = iterator.next()
                h.currentY -= 3.5f
                h.alpha -= 0.025f
                if (h.alpha <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onPetTouched()
                    heartEffects.add(
                        HeartEffect(
                            id = System.nanoTime(),
                            startX = offset.x + (Random.nextFloat() * 30 - 15),
                            startY = offset.y - 20,
                            currentY = offset.y - 20,
                            alpha = 1.0f,
                            color = listOf(
                                Color(0xFFF43F5E),
                                Color(0xFFFB7185),
                                Color(0xFFF472B6)
                            ).random()
                        )
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.52f

            // 1. Draw Garden Background (Sky, Sun, Clouds, Green Hills, Daisies)
            drawGardenEnvironment(
                biomeKey = pet.activeBiome,
                isSleeping = pet.isSleeping,
                sunRayAngle = sunRayAngle,
                width = width,
                height = height
            )

            // 2. Floating dandelion fluff & flower pollen in breeze
            for (p in pollenParticles) {
                p.y -= p.speedY
                p.x += p.speedX
                if (p.y < 0f) {
                    p.y = 1.0f
                    p.x = Random.nextFloat()
                }
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(p.x * width, p.y * height)
                )
            }

            // 3. Ceramic Terracotta Flowerpot & Soil
            drawTerracottaFlowerPot(centerX, centerY + 120f)

            // 4. Draw Living Plant-Pet with Spring Physics & Sway
            rotate(
                degrees = if (pet.isSleeping) 0f else swayAngle,
                pivot = Offset(centerX, centerY + 110f)
            ) {
                val stage = EvolutionStage.valueOf(pet.evolutionStage)
                when (stage) {
                    EvolutionStage.SPORE -> drawSproutPet(centerX, centerY, breathProgress, isBlinking, pet.isSleeping, mood)
                    EvolutionStage.BUD -> drawBudPet(centerX, centerY, breathProgress, isBlinking, pet.isSleeping, mood)
                    EvolutionStage.BLOOM -> drawBloomPet(centerX, centerY, breathProgress, isBlinking, pet.isSleeping, mood, pet.mutationType, butterflyFlutter)
                    EvolutionStage.APEX -> drawApexPet(centerX, centerY, breathProgress, isBlinking, pet.isSleeping, mood, pet.mutationType, butterflyFlutter)
                }
            }

            // 5. Render Touch Hearts
            for (h in heartEffects) {
                drawHeart(Offset(h.startX, h.currentY), 16f, h.color.copy(alpha = h.alpha.coerceIn(0f, 1f)))
            }

            // 6. Cozy Twilight overlay if sleeping
            if (pet.isSleeping) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x661E1B4B), Color(0x99312E81)),
                        startY = 0f,
                        endY = height
                    )
                )
            }
        }
    }
}

// ---------------- GORGEOUS SUNNY GARDEN ENVIRONMENT ----------------
private fun DrawScope.drawGardenEnvironment(
    biomeKey: String,
    isSleeping: Boolean,
    sunRayAngle: Float,
    width: Float,
    height: Float
) {
    if (isSleeping) {
        // Night sky with cozy crescent moon and fireflies
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF14532D)),
                startY = 0f,
                endY = height
            )
        )
        // Sleepy Crescent Moon
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = 35f,
            center = Offset(width * 0.8f, height * 0.14f)
        )
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 30f,
            center = Offset(width * 0.8f - 14f, height * 0.14f - 6f)
        )
    } else {
        // Bright Sunny Sky
        val skyColors = when (biomeKey) {
            BiomeTheme.CRYSTAL_CAVERN.id -> listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8), Color(0xFFBAE6FD))
            BiomeTheme.COSMIC_DOME.id -> listOf(Color(0xFFFEF3C7), Color(0xFFE0F2FE), Color(0xFFBBF7D0))
            else -> listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFFFEF08A))
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = height * 0.7f
            )
        )

        // Warm Glowing Sun with rotating rays
        val sunCenter = Offset(width * 0.82f, height * 0.12f)
        val sunRadius = 42f

        // Sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x88FDE047), Color(0x33FBBF24), Color.Transparent),
                center = sunCenter,
                radius = 110f
            ),
            radius = 110f,
            center = sunCenter
        )

        // Sun rays
        rotate(degrees = sunRayAngle, pivot = sunCenter) {
            for (i in 0 until 12) {
                val angle = i * (360f / 12f)
                rotate(degrees = angle, pivot = sunCenter) {
                    drawLine(
                        color = Color(0xAAFBBF24),
                        start = Offset(sunCenter.x, sunCenter.y - sunRadius - 8f),
                        end = Offset(sunCenter.x, sunCenter.y - sunRadius - 28f),
                        strokeWidth = 4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // Sun face
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFEF08A), Color(0xFFFBBF24)),
                center = sunCenter,
                radius = sunRadius
            ),
            radius = sunRadius,
            center = sunCenter
        )
        // Cute sun smile
        drawCircle(Color(0xFF92400E), 3f, Offset(sunCenter.x - 12f, sunCenter.y - 4f))
        drawCircle(Color(0xFF92400E), 3f, Offset(sunCenter.x + 12f, sunCenter.y - 4f))
        val sunSmile = Path().apply {
            moveTo(sunCenter.x - 10f, sunCenter.y + 6f)
            cubicTo(sunCenter.x - 5f, sunCenter.y + 14f, sunCenter.x + 5f, sunCenter.y + 14f, sunCenter.x + 10f, sunCenter.y + 6f)
        }
        drawPath(sunSmile, color = Color(0xFF92400E), style = Stroke(width = 2.5f))

        // Fluffy cartoon clouds
        drawFluffyCloud(width * 0.22f, height * 0.11f, 1.0f)
        drawFluffyCloud(width * 0.65f, height * 0.22f, 0.75f)
    }

    // Rolling lush green hills in background
    val hillFar = Path().apply {
        moveTo(0f, height * 0.58f)
        cubicTo(width * 0.3f, height * 0.50f, width * 0.7f, height * 0.64f, width, height * 0.56f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(hillFar, color = if (isSleeping) Color(0xFF14532D) else Color(0xFF4ADE80))

    val hillNear = Path().apply {
        moveTo(0f, height * 0.65f)
        cubicTo(width * 0.4f, height * 0.60f, width * 0.8f, height * 0.68f, width, height * 0.62f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(hillNear, color = if (isSleeping) Color(0xFF0F3E22) else Color(0xFF22C55E))

    // Daisies & Wildflowers on the grass hills
    val flowerColors = listOf(Color.White, Color(0xFFFEF08A), Color(0xFFF472B6), Color(0xFFBAE6FD))
    for (i in 0..14) {
        val fx = (sin(i.toDouble() * 1.8) * 0.46 + 0.5).toFloat() * width
        val fy = (height * 0.65f) + (cos(i.toDouble() * 2.2) * 0.1 + 0.15).toFloat() * (height * 0.25f)
        drawCircle(flowerColors[i % flowerColors.size], 4.5f, Offset(fx, fy))
        drawCircle(Color(0xFFF59E0B), 2f, Offset(fx, fy))
    }

    // Wooden garden table / terrace base where the pot sits
    val tableTopY = height * 0.68f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFD97706), Color(0xFF92400E)),
            startY = tableTopY,
            endY = height
        ),
        topLeft = Offset(width * 0.08f, tableTopY),
        size = Size(width * 0.84f, height - tableTopY),
        cornerRadius = CornerRadius(16f, 16f)
    )
    // Wood plank seams
    drawLine(
        color = Color(0xFF78350F),
        start = Offset(width * 0.08f, tableTopY + 25f),
        end = Offset(width * 0.92f, tableTopY + 25f),
        strokeWidth = 2.5f
    )
}

// ---------------- FLUFFY CARTOON CLOUD ----------------
private fun DrawScope.drawFluffyCloud(x: Float, y: Float, scale: Float) {
    val cloudColor = Color(0xEEFFFFFF)
    drawCircle(cloudColor, 22f * scale, Offset(x, y))
    drawCircle(cloudColor, 30f * scale, Offset(x + 22f * scale, y - 5f * scale))
    drawCircle(cloudColor, 24f * scale, Offset(x + 48f * scale, y))
    drawCircle(cloudColor, 18f * scale, Offset(x + 65f * scale, y + 4f * scale))
    drawRoundRect(
        cloudColor,
        topLeft = Offset(x - 5f * scale, y),
        size = Size(75f * scale, 18f * scale),
        cornerRadius = CornerRadius(10f * scale, 10f * scale)
    )
}

// ---------------- TERRACOTTA CERAMIC FLOWERPOT ----------------
private fun DrawScope.drawTerracottaFlowerPot(centerX: Float, potY: Float) {
    val rimWidth = 150f
    val rimHeight = 26f
    val baseWidth = 115f
    val potHeight = 90f

    // Pot Shadow
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(centerX - baseWidth / 2f - 10f, potY + potHeight - 5f),
        size = Size(baseWidth + 20f, 22f)
    )

    // Main pot trapezoid
    val potBody = Path().apply {
        moveTo(centerX - rimWidth * 0.44f, potY + rimHeight)
        lineTo(centerX + rimWidth * 0.44f, potY + rimHeight)
        lineTo(centerX + baseWidth / 2f, potY + potHeight)
        lineTo(centerX - baseWidth / 2f, potY + potHeight)
        close()
    }
    drawPath(
        potBody,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFF9A3412)),
            start = Offset(centerX - rimWidth / 2f, potY),
            end = Offset(centerX + rimWidth / 2f, potY + potHeight)
        )
    )

    // Pot Rim
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFB923C), Color(0xFFC2410C)),
            start = Offset(centerX - rimWidth / 2f, potY),
            end = Offset(centerX + rimWidth / 2f, potY + rimHeight)
        ),
        topLeft = Offset(centerX - rimWidth / 2f, potY),
        size = Size(rimWidth, rimHeight),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Soft dark garden soil inside pot
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF78350F), Color(0xFF451A03)),
            center = Offset(centerX, potY + 8f),
            radius = rimWidth * 0.45f
        ),
        topLeft = Offset(centerX - rimWidth * 0.44f, potY + 4f),
        size = Size(rimWidth * 0.88f, 16f)
    )

    // Little cute decorative heart on the pot
    drawHeart(Offset(centerX, potY + 45f), 12f, Color(0xFFFEF3C7))
}

// ---------------- STAGE 1: РОСТОЧЕК-МАЛЫШ (BABY SPROUT) ----------------
private fun DrawScope.drawSproutPet(
    centerX: Float,
    centerY: Float,
    breath: Float,
    isBlinking: Boolean,
    isSleeping: Boolean,
    mood: PetMood
) {
    val headRadius = 46f * breath

    // Vibrant green curved stem
    val stemPath = Path().apply {
        moveTo(centerX, centerY + headRadius - 8f)
        cubicTo(centerX - 6f, centerY + 65f, centerX + 6f, centerY + 95f, centerX, centerY + 125f)
    }
    drawPath(stemPath, color = Color(0xFF15803D), style = Stroke(width = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

    // Two happy waving baby clover leaves on stem
    drawHappyLeaf(centerX - 8f, centerY + 65f, -35f, 38f)
    drawHappyLeaf(centerX + 8f, centerY + 55f, 35f, 38f)

    // Round cute green head
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFBBF7D0), Color(0xFF4ADE80), Color(0xFF16A34A)),
            center = Offset(centerX - 10f, centerY - 10f),
            radius = headRadius
        ),
        radius = headRadius,
        center = Offset(centerX, centerY)
    )

    // Sprout antenna with golden flower bud on top
    val antenna = Path().apply {
        moveTo(centerX, centerY - headRadius + 4f)
        cubicTo(centerX - 2f, centerY - headRadius - 15f, centerX + 12f, centerY - headRadius - 20f, centerX + 14f, centerY - headRadius - 32f)
    }
    drawPath(antenna, color = Color(0xFF16A34A), style = Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawCircle(Color(0xFFFDE047), 9f, Offset(centerX + 14f, centerY - headRadius - 32f))
    drawCircle(Color(0xFFF59E0B), 5f, Offset(centerX + 14f, centerY - headRadius - 32f))

    // Joyful cartoon eyes & mouth
    drawCuteEyes(centerX, centerY - 4f, isBlinking, isSleeping, 15f)
    drawCuteMouth(centerX, centerY + 16f, mood, isSleeping)
}

// ---------------- STAGE 2: БУТОНЧИК-КОЛОКОЛЬЧИК (TULIP/BLUEBELL BUD) ----------------
private fun DrawScope.drawBudPet(
    centerX: Float,
    centerY: Float,
    breath: Float,
    isBlinking: Boolean,
    isSleeping: Boolean,
    mood: PetMood
) {
    val bodyWidth = 105f * breath
    val bodyHeight = 120f * breath

    // Main Stem
    drawLine(
        color = Color(0xFF15803D),
        start = Offset(centerX, centerY + 45f),
        end = Offset(centerX, centerY + 125f),
        strokeWidth = 11f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Leaf arms that wave
    drawHappyLeaf(centerX - 35f, centerY + 30f, -48f, 55f)
    drawHappyLeaf(centerX + 35f, centerY + 30f, 48f, 55f)

    // Colorful Petal Hood / Bud Body (Tulip/Bell shape)
    val budPath = Path().apply {
        moveTo(centerX, centerY - bodyHeight / 2f - 20f)
        cubicTo(centerX + bodyWidth * 0.65f, centerY - bodyHeight * 0.3f, centerX + bodyWidth * 0.55f, centerY + bodyHeight * 0.5f, centerX, centerY + bodyHeight * 0.5f)
        cubicTo(centerX - bodyWidth * 0.55f, centerY + bodyHeight * 0.5f, centerX - bodyWidth * 0.65f, centerY - bodyHeight * 0.3f, centerX, centerY - bodyHeight / 2f - 20f)
        close()
    }
    drawPath(
        budPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E), Color(0xFFE11D48)),
            startY = centerY - bodyHeight / 2f - 20f,
            endY = centerY + bodyHeight * 0.5f
        )
    )

    // Inner green face opening
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFDCFCE7), Color(0xFF86EFAC)),
            center = Offset(centerX, centerY - 6f),
            radius = bodyWidth * 0.38f
        ),
        topLeft = Offset(centerX - bodyWidth * 0.36f, centerY - bodyHeight * 0.3f),
        size = Size(bodyWidth * 0.72f, bodyHeight * 0.65f)
    )

    // Eyes & Mouth
    drawCuteEyes(centerX, centerY - 6f, isBlinking, isSleeping, 18f)
    drawCuteMouth(centerX, centerY + 18f, mood, isSleeping)
}

// ---------------- STAGE 3: ЦВЕТУЩИЙ ФЛОРИК (BLOOMING FLORA) ----------------
private fun DrawScope.drawBloomPet(
    centerX: Float,
    centerY: Float,
    breath: Float,
    isBlinking: Boolean,
    isSleeping: Boolean,
    mood: PetMood,
    mutation: String,
    butterflyFlutter: Float
) {
    // 8 Grand Colorful Petals in a Circle
    val petalCount = 8
    val petalLength = 75f * breath

    val petalColors = when (mutation) {
        MutationType.DRYAD_EMERALD.id -> listOf(Color(0xFF86EFAC), Color(0xFF22C55E))
        MutationType.PLASMA_SOLAR.id -> listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
        MutationType.CRYSTAL_QUARTZ.id -> listOf(Color(0xFFF472B6), Color(0xFFBE185D))
        else -> listOf(Color(0xFFFBCFE8), Color(0xFFF43F5E))
    }

    // Stem
    drawLine(
        color = Color(0xFF15803D),
        start = Offset(centerX, centerY + 30f),
        end = Offset(centerX, centerY + 125f),
        strokeWidth = 12f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Stem leaves
    drawHappyLeaf(centerX - 35f, centerY + 65f, -42f, 52f)
    drawHappyLeaf(centerX + 35f, centerY + 55f, 42f, 52f)

    // Rotating Blooming Petals
    for (i in 0 until petalCount) {
        val angleDeg = i * (360f / petalCount)
        rotate(degrees = angleDeg, pivot = Offset(centerX, centerY)) {
            val petal = Path().apply {
                moveTo(centerX, centerY)
                cubicTo(centerX - 24f, centerY - petalLength * 0.5f, centerX - 18f, centerY - petalLength, centerX, centerY - petalLength - 16f)
                cubicTo(centerX + 18f, centerY - petalLength, centerX + 24f, centerY - petalLength * 0.5f, centerX, centerY)
                close()
            }
            drawPath(
                petal,
                brush = Brush.linearGradient(
                    colors = petalColors,
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - petalLength - 16f)
                )
            )
        }
    }

    // Creature Sunny Central Face Core
    val coreRadius = 56f * breath
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFEF9C3), Color(0xFFFDE047), Color(0xFFF59E0B)),
            center = Offset(centerX - 12f, centerY - 12f),
            radius = coreRadius
        ),
        radius = coreRadius,
        center = Offset(centerX, centerY)
    )

    // Eyes & Smile
    drawCuteEyes(centerX, centerY - 6f, isBlinking, isSleeping, 21f)
    drawCuteMouth(centerX, centerY + 20f, mood, isSleeping)

    // Cute flying companion Butterfly
    drawCuteButterfly(centerX + 85f, centerY - 80f, butterflyFlutter)
}

// ---------------- STAGE 4: КОРОЛЕВСКИЙ САДОВЫЙ ДУХ (ROYAL APEX) ----------------
private fun DrawScope.drawApexPet(
    centerX: Float,
    centerY: Float,
    breath: Float,
    isBlinking: Boolean,
    isSleeping: Boolean,
    mood: PetMood,
    mutation: String,
    butterflyFlutter: Float
) {
    // Golden Floral Crown & Halo
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFDE047),
                Color(0xFFF472B6),
                Color(0xFF6EE7B7),
                Color(0xFF60A5FA),
                Color(0xFFFDE047)
            ),
            center = Offset(centerX, centerY - 45f)
        ),
        radius = 95f * breath,
        center = Offset(centerX, centerY - 45f),
        style = Stroke(width = 4.5f)
    )

    // Royal Blossom Crown Points
    for (i in -2..2) {
        val crownX = centerX + (i * 28f)
        val crownTopY = centerY - 115f - (if (i == 0) 24f else 0f)
        drawCircle(Color(0xFFFDE047), 9f, Offset(crownX, crownTopY))
        drawCircle(Color(0xFFF59E0B), 5f, Offset(crownX, crownTopY))
    }

    // 10 Magnificent Rainbow Petals
    val petalCount = 10
    val petalLength = 85f * breath
    for (i in 0 until petalCount) {
        val angleDeg = i * (360f / petalCount)
        rotate(degrees = angleDeg, pivot = Offset(centerX, centerY)) {
            val petal = Path().apply {
                moveTo(centerX, centerY)
                cubicTo(centerX - 22f, centerY - petalLength * 0.5f, centerX - 18f, centerY - petalLength, centerX, centerY - petalLength - 18f)
                cubicTo(centerX + 18f, centerY - petalLength, centerX + 22f, centerY - petalLength * 0.5f, centerX, centerY)
                close()
            }
            drawPath(
                petal,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFDE047), Color(0xFFF43F5E), Color(0xFF9333EA)),
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - petalLength - 18f)
                )
            )
        }
    }

    // Main Golden-Floral Core
    val coreRadius = 65f * breath
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFEF08A), Color(0xFFF59E0B)),
            center = Offset(centerX - 15f, centerY - 15f),
            radius = coreRadius
        ),
        radius = coreRadius,
        center = Offset(centerX, centerY)
    )

    // Eyes & Smile
    drawCuteEyes(centerX, centerY - 6f, isBlinking, isSleeping, 23f)
    drawCuteMouth(centerX, centerY + 24f, mood, isSleeping)

    // Two dancing butterflies
    drawCuteButterfly(centerX - 95f, centerY - 75f, butterflyFlutter)
    drawCuteButterfly(centerX + 95f, centerY - 75f, 1.3f - butterflyFlutter)
}

// ---------------- CUTE CARTOON EYES ----------------
private fun DrawScope.drawCuteEyes(
    centerX: Float,
    centerY: Float,
    isBlinking: Boolean,
    isSleeping: Boolean,
    eyeSpacing: Float
) {
    if (isSleeping) {
        // Closed happy sleepy curves ^^
        val left = Path().apply {
            moveTo(centerX - eyeSpacing - 12f, centerY)
            cubicTo(centerX - eyeSpacing - 6f, centerY - 8f, centerX - eyeSpacing + 6f, centerY - 8f, centerX - eyeSpacing + 12f, centerY)
        }
        val right = Path().apply {
            moveTo(centerX + eyeSpacing - 12f, centerY)
            cubicTo(centerX + eyeSpacing - 6f, centerY - 8f, centerX + eyeSpacing + 6f, centerY - 8f, centerX + eyeSpacing + 12f, centerY)
        }
        drawPath(left, color = Color(0xFF1E293B), style = Stroke(width = 3.5f))
        drawPath(right, color = Color(0xFF1E293B), style = Stroke(width = 3.5f))
        return
    }

    if (isBlinking) {
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(centerX - eyeSpacing - 10f, centerY),
            end = Offset(centerX - eyeSpacing + 10f, centerY),
            strokeWidth = 3.5f
        )
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(centerX + eyeSpacing - 10f, centerY),
            end = Offset(centerX + eyeSpacing + 10f, centerY),
            strokeWidth = 3.5f
        )
        return
    }

    val eyeWidth = 12f
    val eyeHeight = 16f

    // Big shiny anime eyes
    drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerX - eyeSpacing - eyeWidth, centerY - eyeHeight),
        size = Size(eyeWidth * 2f, eyeHeight * 2f)
    )
    drawCircle(Color.White, 5f, Offset(centerX - eyeSpacing - 3f, centerY - 6f))
    drawCircle(Color(0xFFBAE6FD), 2.5f, Offset(centerX - eyeSpacing + 4f, centerY + 4f))

    drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerX + eyeSpacing - eyeWidth, centerY - eyeHeight),
        size = Size(eyeWidth * 2f, eyeHeight * 2f)
    )
    drawCircle(Color.White, 5f, Offset(centerX + eyeSpacing - 3f, centerY - 6f))
    drawCircle(Color(0xFFBAE6FD), 2.5f, Offset(centerX + eyeSpacing + 4f, centerY + 4f))

    // Rosy Pink Cheeks
    drawCircle(Color(0x77FB7185), 9f, Offset(centerX - eyeSpacing - 14f, centerY + 9f))
    drawCircle(Color(0x77FB7185), 9f, Offset(centerX + eyeSpacing + 14f, centerY + 9f))
}

// ---------------- CUTE CARTOON MOUTH ----------------
private fun DrawScope.drawCuteMouth(
    centerX: Float,
    centerY: Float,
    mood: PetMood,
    isSleeping: Boolean
) {
    if (isSleeping) {
        drawCircle(Color(0xFF1E293B), 3.5f, Offset(centerX, centerY))
        return
    }

    val mouth = Path()
    when (mood) {
        PetMood.THRIVING, PetMood.HAPPY -> {
            // Big joyous open laugh
            mouth.moveTo(centerX - 10f, centerY - 2f)
            mouth.cubicTo(centerX - 5f, centerY + 10f, centerX + 5f, centerY + 10f, centerX + 10f, centerY - 2f)
            mouth.close()
            drawPath(mouth, color = Color(0xFFF43F5E))
            // Tongue
            drawCircle(Color(0xFFFDA4AF), 4f, Offset(centerX, centerY + 4f))
        }
        PetMood.THIRSTY, PetMood.HUNGRY, PetMood.SICK -> {
            // Sad wobbly pout
            mouth.moveTo(centerX - 9f, centerY + 5f)
            mouth.cubicTo(centerX - 4f, centerY - 4f, centerX + 4f, centerY - 4f, centerX + 9f, centerY + 5f)
            drawPath(mouth, color = Color(0xFF1E293B), style = Stroke(width = 2.5f))
        }
        else -> {
            // Gentle sweet smile
            mouth.moveTo(centerX - 8f, centerY)
            mouth.cubicTo(centerX - 4f, centerY + 6f, centerX + 4f, centerY + 6f, centerX + 8f, centerY)
            drawPath(mouth, color = Color(0xFF1E293B), style = Stroke(width = 2.5f))
        }
    }
}

// ---------------- HAPPY GREEN LEAF ----------------
private fun DrawScope.drawHappyLeaf(startX: Float, startY: Float, angleDeg: Float, length: Float) {
    rotate(degrees = angleDeg, pivot = Offset(startX, startY)) {
        val leaf = Path().apply {
            moveTo(startX, startY)
            cubicTo(startX - 18f, startY - length * 0.5f, startX - 10f, startY - length, startX, startY - length)
            cubicTo(startX + 10f, startY - length, startX + 18f, startY - length * 0.5f, startX, startY)
            close()
        }
        drawPath(
            leaf,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                start = Offset(startX, startY),
                end = Offset(startX, startY - length)
            )
        )
    }
}

// ---------------- BUTTERFLY COMPANION ----------------
private fun DrawScope.drawCuteButterfly(x: Float, y: Float, flutter: Float) {
    val wingWidth = 14f * flutter
    // Left Wing
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFFDE047), Color(0xFFF472B6))),
        topLeft = Offset(x - wingWidth - 3f, y - 10f),
        size = Size(wingWidth, 18f)
    )
    // Right Wing
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFFDE047), Color(0xFF60A5FA))),
        topLeft = Offset(x + 3f, y - 10f),
        size = Size(wingWidth, 18f)
    )
    // Body
    drawRoundRect(
        color = Color(0xFF78350F),
        topLeft = Offset(x - 2.5f, y - 6f),
        size = Size(5f, 14f),
        cornerRadius = CornerRadius(2.5f, 2.5f)
    )
}

// ---------------- FLOATING HEART DRAW ----------------
private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.35f)
        cubicTo(center.x - size, center.y - size * 0.5f, center.x - size * 0.5f, center.y - size, center.x, center.y - size * 0.3f)
        cubicTo(center.x + size * 0.5f, center.y - size, center.x + size, center.y - size * 0.5f, center.x, center.y + size * 0.35f)
        close()
    }
    drawPath(path, color = color)
}
