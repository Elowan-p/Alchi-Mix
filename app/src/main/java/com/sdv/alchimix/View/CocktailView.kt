package com.sdv.alchimix.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import kotlinx.coroutines.launch
import com.sdv.alchimix.view.components.FullFormulaView

enum class AlchiTab { ALAMBIC, TRANSMUTATION, LABORATOIRE, CODEX }

@Composable
fun AlchiMixScreen(viewModel: CocktailViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(AlchiTab.ALAMBIC) }
    var revealedCocktail by remember { mutableStateOf<CocktailDTO?>(null) }
    var showFullFormula by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is CocktailState.Success) {
            revealedCocktail = (state as CocktailState.Success).cocktail
        }
    }

    Scaffold(
        bottomBar = {
            AlchiMixBottomBar(currentTab) {
                currentTab = it
                if (it != AlchiTab.LABORATOIRE) {
                    showFullFormula = false
                }
                viewModel.resetState()
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (showFullFormula && revealedCocktail != null) {
                val isFav = (localState as? CocktailLocalState.Success)?.cocktails?.any {
                    it.name.trim().equals(revealedCocktail!!.strDrink.trim(), ignoreCase = true) && it.isFavorite
                } ?: false

                FullFormulaView(
                    cocktail = revealedCocktail!!,
                    isFavoriteLocal = isFav,
                    onBack = { showFullFormula = false },
                    onSave = { viewModel.saveCocktailFromApi(revealedCocktail!!) }
                )
            } else {
                when (currentTab) {
                    AlchiTab.ALAMBIC -> {
                        AlambicMainView(
                            viewModel = viewModel,
                            state = state,
                            localState = localState,
                            revealedCocktail = revealedCocktail,
                            onSpinStart = { revealedCocktail = null },
                            onShowFullFormula = { showFullFormula = true }
                        )
                    }
                    AlchiTab.TRANSMUTATION -> {
                        TransmutationScreen(
                            viewModel = viewModel,
                            onShowFullFormula = { cocktail ->
                                revealedCocktail = cocktail
                                showFullFormula = true
                            }
                        )
                    }
                    AlchiTab.LABORATOIRE -> {
                        LaboratoireScreen(viewModel = viewModel)
                    }
                    AlchiTab.CODEX -> {
                        CodexScreen(
                            viewModel = viewModel,
                            onNavigateToFormula = { entity ->
                                val measures = entity.measures?.split(",") ?: emptyList()
                                revealedCocktail = CocktailDTO(
                                    idDrink = entity.id.toString(),
                                    strDrink = entity.name,
                                    strInstructions = entity.instructions,
                                    strDrinkThumb = entity.imageUrl ?: "",
                                    strCategory = entity.category,
                                    strAlcoholic = null,
                                    strGlass = null,
                                    strIngredient1 = entity.ingredients?.split(",")?.getOrNull(0),
                                    strIngredient2 = entity.ingredients?.split(",")?.getOrNull(1),
                                    strIngredient3 = entity.ingredients?.split(",")?.getOrNull(2),
                                    strIngredient4 = entity.ingredients?.split(",")?.getOrNull(3),
                                    strIngredient5 = entity.ingredients?.split(",")?.getOrNull(4),
                                    strIngredient6 = entity.ingredients?.split(",")?.getOrNull(5),
                                    strIngredient7 = entity.ingredients?.split(",")?.getOrNull(6),
                                    strIngredient8 = entity.ingredients?.split(",")?.getOrNull(7),
                                    strIngredient9 = entity.ingredients?.split(",")?.getOrNull(8),
                                    strIngredient10 = entity.ingredients?.split(",")?.getOrNull(9),
                                    strIngredient11 = entity.ingredients?.split(",")?.getOrNull(10),
                                    strIngredient12 = entity.ingredients?.split(",")?.getOrNull(11),
                                    strIngredient13 = entity.ingredients?.split(",")?.getOrNull(12),
                                    strIngredient14 = entity.ingredients?.split(",")?.getOrNull(13),
                                    strIngredient15 = entity.ingredients?.split(",")?.getOrNull(14),
                                    strMeasure1 = measures.getOrNull(0),
                                    strMeasure2 = measures.getOrNull(1),
                                    strMeasure3 = measures.getOrNull(2),
                                    strMeasure4 = measures.getOrNull(3),
                                    strMeasure5 = measures.getOrNull(4),
                                    strMeasure6 = measures.getOrNull(5),
                                    strMeasure7 = measures.getOrNull(6),
                                    strMeasure8 = measures.getOrNull(7),
                                    strMeasure9 = null, strMeasure10 = null, strMeasure11 = null, strMeasure12 = null,
                                    strMeasure13 = null, strMeasure14 = null, strMeasure15 = null
                                )
                                showFullFormula = true
                            }
                        )
                    }
                }
            }
        }
    }
}

fun tabIcon(tab: AlchiTab): String = when(tab) {
    AlchiTab.ALAMBIC -> "⚗️"
    AlchiTab.TRANSMUTATION -> "⚡"
    AlchiTab.LABORATOIRE -> "🧪"
    AlchiTab.CODEX -> "📖"
}

@Composable
fun AlchiMixBottomBar(currentTab: AlchiTab, onTabSelected: (AlchiTab) -> Unit) {
    NavigationBar(containerColor = DarkBg, tonalElevation = 0.dp) {
        AlchiTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isSelected) {
                            Box(Modifier.width(24.dp).height(2.dp).background(AlambicCyan))
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(when(tab) {
                            AlchiTab.ALAMBIC -> "⚗️"
                            AlchiTab.TRANSMUTATION -> "⚡"
                            AlchiTab.LABORATOIRE -> "🧪"
                            AlchiTab.CODEX -> "📖"
                        }, fontSize = 20.sp)
                    }
                },
                label = { Text(tab.name, fontSize = 9.sp, color = if (isSelected) Color.White else Color.Gray) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}