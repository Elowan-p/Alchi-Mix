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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.utils.Rarity
import com.sdv.alchimix.viewmodel.CocktailState
import com.sdv.alchimix.viewmodel.CocktailViewModel
import com.sdv.alchimix.view.components.CocktailResultCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // RÉCUPÉRATION DES LISTES DEPUIS LE VIEWMODEL
    val baseItemsList by viewModel.alcoholicBases.collectAsStateWithLifecycle()
    val chaosItemsList by viewModel.nonAlcoholicEssences.collectAsStateWithLifecycle()

    // LISTES SÉCURISÉES (Au cas où l'API n'a pas encore fini de charger)
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1B1B1B), Color(0xFF0A0A0A))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SteampunkTag("BASE")
                SteampunkTag("ESSENCE")
                SteampunkTag("RARETÉ")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SteampunkDisplay(currentBase, AlambicCyan, isSpinning)
                SteampunkDisplay(currentChaos, AlambicOrange, isSpinning)
                SteampunkDisplay(currentRaritySlot, AlambicPurple, isSpinning)
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
                        .size(110.dp)
                        .shadow(if (!isSpinning && !isAlchemyLoading) glowSize else 0.dp, CircleShape, spotColor = AlambicCyan),
                    shape = CircleShape,
                    color = SteampunkBlack,
                    border = BorderStroke(4.dp, if (isSpinning || isAlchemyLoading) Color.Gray else AlambicCyan)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Activer\nl'Alambic",
                            color = if (isSpinning || isAlchemyLoading) Color.Gray else AlambicCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
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
    Surface(
        color = Color(0xFF2A2118),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, BrassGold),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            color = BrassGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SteampunkDisplay(text: String, color: Color, isSpinning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, BrassGold.copy(0.5f)),
        modifier = Modifier
            .width(105.dp)
            .height(55.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSpinning) color.copy(alpha = alpha) else color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
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
