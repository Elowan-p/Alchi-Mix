package com.sdv.alchimix.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.utils.Rarity
import com.sdv.alchimix.viewmodel.CocktailState
import com.sdv.alchimix.viewmodel.CocktailViewModel
import com.sdv.alchimix.view.components.CocktailResultCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

@Composable
fun AlambicMainView(
    viewModel: CocktailViewModel,
    state: CocktailState,
    localState: CocktailLocalState,
    revealedCocktail: CocktailDTO?,
    onSpinStart: () -> Unit,
    onShowFullFormula: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val baseItemsList by viewModel.alcoholicBases.collectAsStateWithLifecycle()
    val chaosItemsList by viewModel.nonAlcoholicEssences.collectAsStateWithLifecycle()

    val safeBaseItems = if (baseItemsList.isNotEmpty()) baseItemsList else listOf("Gin", "Vodka", "Rum")
    val safeChaosItems = if (chaosItemsList.isNotEmpty()) chaosItemsList else listOf("Lemon", "Sugar", "Mint")

    val rarityItems = listOf("COMMUN", "RARE", "ÉPIQUE", "LÉGENDAIRE")

    var currentBase by remember { mutableStateOf("Gin") }
    var currentChaos by remember { mutableStateOf("Lemon") }
    var currentRaritySlot by remember { mutableStateOf("COMMUN") }

    var isSpinning by remember { mutableStateOf(false) }
    var isAlchemyLoading by remember { mutableStateOf(false) }

    LaunchedEffect(revealedCocktail) {
        if (revealedCocktail != null && !isSpinning && !isAlchemyLoading) {
            delay(100)
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(800)
            )
        }
    }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val sensorEventListener = object : SensorEventListener {
            private var lastShakeTime: Long = 0
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH
                
                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
                if (gForce > 2.7f) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 1500) {
                        lastShakeTime = now
                        if (!isSpinning && !isAlchemyLoading) {
                            isSpinning = true
                            onSpinStart()

                            scope.launch {
                                try {
                                    val anim1 = launch { repeat(25) { currentBase = safeBaseItems.random(); delay(80) } }
                                    val anim2 = launch { repeat(30) { currentChaos = safeChaosItems.random(); delay(90) } }
                                    val anim3 = launch { repeat(35) { currentRaritySlot = rarityItems.random(); delay(100) } }

                                    anim1.join()
                                    anim2.join()
                                    anim3.join()

                                    isSpinning = false
                                    isAlchemyLoading = true

                                    val mandatory = listOf(currentBase, currentChaos)

                                    val targetRarity = when(currentRaritySlot) {
                                        "LÉGENDAIRE" -> Rarity.LEGENDARY
                                        "ÉPIQUE" -> Rarity.EPIC
                                        "RARE" -> Rarity.RARE
                                        else -> Rarity.COMMON
                                    }

                                    viewModel.transmute(('a'..'z').random().toString(), mandatory, targetRarity)

                                    delay(5000)
                                    isAlchemyLoading = false

                                } catch (e: Exception) {
                                    isSpinning = false
                                    isAlchemyLoading = false
                                }
                            }
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    Brush.verticalGradient(
                        listOf(SteampunkBlack, DarkBg)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AlchimieFlaskSlot(label = "BASE", value = currentBase, color = AlambicCyan, isSpinning = isSpinning, emoji = "🧪")
                AlchimieFlaskSlot(label = "ESSENCE", value = currentChaos, color = AlambicOrange, isSpinning = isSpinning, emoji = "⚗️")
                AlchimieFlaskSlot(label = "RARETÉ", value = currentRaritySlot, color = AlambicPurple, isSpinning = isSpinning, emoji = "🔮")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp)
                    .background(Color.Black.copy(0.2f), RoundedCornerShape(16.dp))
                    .border(2.dp, BrassGold.copy(0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isAlchemyLoading) {
                    AlchemyExperimentAnimation(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        durationMillis = 5000L
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(
                                        if (isSpinning) listOf(AlambicOrange.copy(0.6f), Color.Transparent)
                                        else listOf(Color.White.copy(0.1f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                                .border(4.dp, BrassGold, CircleShape)
                        ) {
                            if (isSpinning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    color = AlambicOrange,
                                    strokeWidth = 4.dp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (isSpinning) "CHARGEMENT DES ESSENCES..." else "PRÊT À DISTILLER",
                            color = BrassGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                val glowSize by infiniteTransition.animateValue(
                    initialValue = 2.dp,
                    targetValue = 16.dp,
                    typeConverter = Dp.VectorConverter,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Surface(
                    onClick = {
                        if (!isSpinning && !isAlchemyLoading) {
                            isSpinning = true
                            onSpinStart()

                            scope.launch {
                                try {
                                    val anim1 = launch { repeat(25) { currentBase = safeBaseItems.random(); delay(80) } }
                                    val anim2 = launch { repeat(30) { currentChaos = safeChaosItems.random(); delay(90) } }
                                    val anim3 = launch { repeat(35) { currentRaritySlot = rarityItems.random(); delay(100) } }

                                    anim1.join()
                                    anim2.join()
                                    anim3.join()

                                    isSpinning = false
                                    isAlchemyLoading = true

                                    val mandatory = listOf(currentBase, currentChaos)

                                    val targetRarity = when(currentRaritySlot) {
                                        "LÉGENDAIRE" -> Rarity.LEGENDARY
                                        "ÉPIQUE" -> Rarity.EPIC
                                        "RARE" -> Rarity.RARE
                                        else -> Rarity.COMMON
                                    }

                                    viewModel.transmute(('a'..'z').random().toString(), mandatory, targetRarity)

                                    delay(5000)
                                    isAlchemyLoading = false

                                } catch (e: Exception) {
                                    isSpinning = false
                                    isAlchemyLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isSpinning && !isAlchemyLoading,
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(if (!isSpinning && !isAlchemyLoading) glowSize else 0.dp, CircleShape, spotColor = AlambicCyan),
                    shape = CircleShape,
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isSpinning || isAlchemyLoading) Color(0xFF4A3410) else Color(0xFFD3710A),
                                        if (isSpinning || isAlchemyLoading) Color(0xFF1A1A1A) else Color(0xFF6B3000)
                                    )
                                )
                            )
                            .border(6.dp, if (isSpinning || isAlchemyLoading) Color.DarkGray else BrassGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSpinning || isAlchemyLoading) "EN COURS" else "ACTIVER",
                            color = if (isSpinning || isAlchemyLoading) Color.Gray else Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp,
                            style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black, blurRadius = 4f))
                        )
                    }
                }
            }

            if (state is CocktailState.Error && !isAlchemyLoading) {
                Text(text = state.message, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }

            AnimatedVisibility(
                visible = revealedCocktail != null && !isSpinning && !isAlchemyLoading,
                enter = fadeIn() + expandVertically()
            ) {
                revealedCocktail?.let { cocktail ->
                    val isFav = (localState as? CocktailLocalState.Success)?.cocktails?.any {
                        it.name.trim().equals(cocktail.strDrink.trim(), ignoreCase = true) && it.isFavorite
                    } == true

                    Column {
                        Spacer(modifier = Modifier.height(40.dp))
                        CocktailResultCard(
                            cocktail = cocktail,
                            isFavorite = isFav,
                            tagText = "✦ FORMULE RÉVÉLÉE ✦",
                            computeRarityByDrinkName = true,
                            showPreviewIngredients = true,
                            onToggleFavorite = { viewModel.saveCocktailFromApi(cocktail) },
                            onShowFullFormula = onShowFullFormula
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(text = "📱 Sur mobile, secouez l'appareil", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SteampunkTag(text: String) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(Color(0xFF261D15), RoundedCornerShape(8.dp))
            .border(2.dp, BrassGold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(color = BrassGold, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 8.dp.toPx()))
            drawCircle(color = BrassGold, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width - 8.dp.toPx(), 8.dp.toPx()))
            drawCircle(color = BrassGold, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(8.dp.toPx(), size.height - 8.dp.toPx()))
            drawCircle(color = BrassGold, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()))
        }
        Text(
            text = text,
            color = BrassGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SteampunkDisplay(text: String, color: Color, isSpinning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpinning) 50 else 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = Color(0xFF030303),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(2.dp, Color(0xFF333333)),
        modifier = Modifier
            .width(105.dp)
            .height(55.dp)
            .shadow(
                elevation = if (isSpinning) 8.dp else 4.dp,
                spotColor = color,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = color.copy(alpha = alpha),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = color.copy(alpha = 0.8f),
                        blurRadius = 15f
                    )
                )
            )
        }
    }
}


@Composable
fun SlotItem(text: String, color: Color, isSpinning: Boolean) {
    val transition = rememberInfiniteTransition(label = "slot")
    val offsetY by transition.animateValue(initialValue = 0.dp, targetValue = 15.dp, typeConverter = Dp.VectorConverter, animationSpec = infiniteRepeatable(animation = tween(80, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "offset")
    Surface(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.3f)) {
        Box(contentAlignment = Alignment.Center, modifier = if (isSpinning) Modifier.offset(y = offsetY) else Modifier) {
            Text(text = text, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

@Composable
fun AlchimieFlaskSlot(label: String, value: String, color: Color, isSpinning: Boolean, emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "flaskAnim")

    val shake by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpinning) 50 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpinning) 70 else 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val shakeModifier = if (isSpinning) shake * 3f else shake * 0.3f
    val bounceModifier = if (isSpinning) bounce * 4f else bounce * 1.5f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        SteampunkTag(label)
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = shakeModifier.dp, y = bounceModifier.dp)
                .size(60.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val radiusSize = if (isSpinning) size.width else size.width / 1.5f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = if (isSpinning) 0.6f else 0.2f), Color.Transparent),
                        center = center,
                        radius = radiusSize
                    )
                )
            }
            Text(text = emoji, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFF030303),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(2.dp, BrassGold),
            modifier = Modifier
                .width(105.dp)
                .height(45.dp)
                .shadow(
                    elevation = if (isSpinning) 8.dp else 2.dp,
                    spotColor = color,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    color = color.copy(alpha = if (isSpinning) 0.5f else 1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(2.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = color.copy(alpha = 0.8f),
                            blurRadius = if (isSpinning) 15f else 2f
                        )
                    )
                )
            }
        }
    }
}
