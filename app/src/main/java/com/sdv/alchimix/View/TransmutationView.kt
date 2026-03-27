package com.sdv.alchimix.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.utils.Rarity
import com.sdv.alchimix.viewmodel.CocktailState
import com.sdv.alchimix.viewmodel.CocktailViewModel
import kotlinx.coroutines.delay

@Composable
fun TransmutationScreen(viewModel: CocktailViewModel, onShowFullFormula: (CocktailDTO) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    
    val discoveredAlcohols by viewModel.discoveredAlcohols.collectAsStateWithLifecycle()
    val discoveredEssences by viewModel.discoveredEssences.collectAsStateWithLifecycle()

    var selectedBase by remember { mutableStateOf("") }
    var selectedFlavor by remember { mutableStateOf("") }
    
    var revealedCocktail by remember { mutableStateOf<CocktailDTO?>(null) }
    var isManuallyLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(discoveredAlcohols, discoveredEssences) {
        if (selectedBase.isBlank() && discoveredAlcohols.isNotEmpty()) {
            selectedBase = discoveredAlcohols.first()
        }
        if (selectedFlavor.isBlank() && discoveredEssences.isNotEmpty()) {
            selectedFlavor = discoveredEssences.first()
        }
    }

    LaunchedEffect(state) {
        if (state is CocktailState.Success && isManuallyLoading) {
            revealedCocktail = (state as CocktailState.Success).cocktail
            delay(500)
            isManuallyLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        AlchemyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRANSMUTATION",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = BrassGold
                ),
                modifier = Modifier.padding(top = 40.dp)
            )

            Text(
                text = "Le Laboratoire de l'Inconscient",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            if (discoveredAlcohols.isEmpty() || discoveredEssences.isEmpty()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = "Retournez à l'alambic pour découvrir de nouveaux ingrédients",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                AlchimiserButton(
                    onTransmute = {
                        revealedCocktail = null
                        isManuallyLoading = true
                        val ingredients = listOf(selectedBase, selectedFlavor)
                        val randomRarity = listOf(Rarity.COMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY).random()
                        viewModel.transmute(('A'..'Z').random().toString(), ingredients, randomRarity)
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                LaboratoireStation(
                    base = selectedBase,
                    flavor = selectedFlavor,
                    baseOptions = discoveredAlcohols,
                    flavorOptions = discoveredEssences,
                    onBaseChange = { selectedBase = it },
                    onFlavorChange = { selectedFlavor = it },
                    isLoading = state is CocktailState.Loading || isManuallyLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = revealedCocktail != null && !isManuallyLoading,
                enter = fadeIn() + expandVertically()
            ) {
                revealedCocktail?.let { cocktail ->
                    val isFav = (localState as? CocktailLocalState.Success)?.cocktails?.any {
                        it.name.trim().equals(cocktail.strDrink.trim(), ignoreCase = true) && it.isFavorite 
                    } == true

                    com.sdv.alchimix.view.components.CocktailResultCard(
                        cocktail = cocktail,
                        isFavorite = isFav,
                        tagText = "✦ FORMULE DÉCOUVERTE ✦",
                        computeRarityByDrinkName = true,
                        showPreviewIngredients = false,
                        onToggleFavorite = { viewModel.saveCocktailFromApi(cocktail) },
                        onShowFullFormula = { onShowFullFormula(cocktail) }
                    )
                }
            }

            if (state is CocktailState.Error && !isManuallyLoading) {
                Text(
                    text = (state as CocktailState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        AnimatedVisibility(
            visible = state is CocktailState.Loading || isManuallyLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AlchemyExperimentAnimation()
        }
    }
}

@Composable
fun AlchemyBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height * 0.35f)
        
        for (i in 1..5) {
            drawCircle(
                color = BrassGold.copy(alpha = 0.05f * i),
                radius = 80.dp.toPx() + (i * 25.dp.toPx()),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun LaboratoireStation(
    base: String,
    flavor: String,
    baseOptions: List<String>,
    flavorOptions: List<String>,
    onBaseChange: (String) -> Unit,
    onFlavorChange: (String) -> Unit,
    isLoading: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Configurez vos essences",
            color = BrassGold.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF140F0A), RoundedCornerShape(20.dp))
                .border(2.dp, BrassGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "flask")
                    val bounce by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = if (isLoading) -25f else -10f,
                        animationSpec = infiniteRepeatable(
                            tween(if (isLoading) 300 else 1500),
                            RepeatMode.Reverse
                        ),
                        label = "bounce"
                    )
                    Text(
                        text = if (isLoading) "🫧" else "🧪", 
                        fontSize = 50.sp, 
                        modifier = Modifier.offset(y = bounce.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IngredientChip(if (base.isNotBlank()) base else "...", AlambicCyan, baseOptions, onBaseChange)
                    IngredientChip(if (flavor.isNotBlank()) flavor else "...", AlambicOrange, flavorOptions, onFlavorChange)
                }
            }
        }
    }
}

@Composable
fun IngredientChip(current: String, color: Color, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = current,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.clickable { expanded = true }.padding(vertical = 2.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBg).border(1.dp, BrassGold, RoundedCornerShape(8.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AlchimiserButton(onTransmute: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer(scaleX = glowScale, scaleY = glowScale)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(AlambicOrange.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                }
        )

        Surface(
            onClick = onTransmute,
            modifier = Modifier
                .size(140.dp)
                .shadow(40.dp, CircleShape, spotColor = AlambicOrange),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFE67300), Color(0xFF6B2400))
                        )
                    )
                    .border(6.dp, BrassGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp).border(1.dp, Color(0xFFFFB366).copy(0.3f), CircleShape)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFFFE0B2),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "ALCHIMISER",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD1A3),
                            letterSpacing = 2.sp,
                            fontSize = 14.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black.copy(0.8f), blurRadius = 10f)
                        )
                    )
                }
            }
        }
    }
}


