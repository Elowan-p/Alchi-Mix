package com.sdv.alchimix.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

val DarkBg = Color(0xFF08080A)
val CardBg = Color(0xFF121215)
val AlambicCyan = Color(0xFF00F2FF)
val AlambicOrange = Color(0xFFFF5E00)
val AlambicPurple = Color(0xFFA200FF)
val SteampunkBlack = Color(0xFF1B1B1B)
val BrassGold = Color(0xFFC9A66B)

@Composable
fun AlchemyExperimentAnimation(
    modifier: Modifier = Modifier.fillMaxSize().background(DarkBg.copy(alpha = 0.95f)),
    durationMillis: Long = 5000L,
    onAnimationEnd: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alchemy_fx")
    val steps = listOf(
        "DISTILLATION DES ESSENCES...",
        "PURIFICATION DU MÉLANGE...",
        "STABILISATION MOLÉCULAIRE...",
        "CRISTALLISATION DU DESTIN...",
        "EXTRACTION FINALE..."
    )
    
    var currentStepIdx by remember { mutableIntStateOf(0) }
    val stepDuration = durationMillis / steps.size
    
    LaunchedEffect(Unit) {
        repeat(steps.size) { i ->
            currentStepIdx = i
            delay(stepDuration)
        }
        onAnimationEnd()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2 - 50.dp.toPx())
            for (i in 1..4) {
                drawCircle(
                    color = BrassGold.copy(alpha = 0.05f * i),
                    radius = 120.dp.toPx() + (i * 30.dp.toPx()),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        repeat(35) { index ->
            BubbleFXComponent(index, infiniteTransition)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                val rotation1 by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
                    label = "aura1"
                )
                val rotation2 by infiniteTransition.animateFloat(
                    initialValue = 360f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                    label = "aura2"
                )
                val rotation3 by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
                    label = "aura3"
                )
                
                Canvas(modifier = Modifier.size(160.dp).rotate(rotation1)) {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(AlambicCyan, AlambicPurple, AlambicCyan)),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                Canvas(modifier = Modifier.size(130.dp).rotate(rotation2)) {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(AlambicOrange, BrassGold, AlambicOrange)),
                        style = Stroke(width = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 10f)))
                    )
                }
                Canvas(modifier = Modifier.size(180.dp).rotate(rotation3)) {
                    drawCircle(
                        color = AlambicPurple.copy(alpha = 0.5f),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 15f)))
                    )
                }
                
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
                    label = "scale"
                )
                
                Text("⚗️", fontSize = 65.sp, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = steps[currentStepIdx],
                color = AlambicCyan,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NE PAS INTERROMPRE LE FLUX",
                color = AlambicOrange.copy(alpha = 0.7f),
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp)
            )
        }
    }
}

@Composable
fun BubbleFXComponent(index: Int, transition: InfiniteTransition) {
    val startX = remember { Random.nextFloat() }
    val duration = remember { Random.nextInt(1000, 3000) }
    val delay = remember { Random.nextInt(0, 1000) }
    
    val posY by transition.animateFloat(
        initialValue = 1.2f,
        targetValue = -0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, delayMillis = delay, easing = LinearEasing)
        ),
        label = "bubble_y"
    )
    
    val alphaAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                0f at 0
                0.6f at duration / 2
                0f at duration
            }
        ),
        label = "bubble_alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(AlambicCyan, AlambicPurple, AlambicOrange)
        drawCircle(
            color = colors.random(Random(index)).copy(alpha = alphaAnim),
            radius = (3 + index % 5).dp.toPx(),
            center = Offset(size.width * startX, size.height * posY)
        )
    }
}
