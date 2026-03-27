package com.sdv.alchimix.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.model.IngredientDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.utils.Rarity
import com.sdv.alchimix.viewmodel.CocktailViewModel

@Composable
fun CodexScreen(
    viewModel: CocktailViewModel,
    onNavigateToFormula: (CocktailEntity) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Formules", "Essences")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val rank by viewModel.masteryStatus.collectAsStateWithLifecycle()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CODEX INGREDIA",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrassGold,
                    fontWeight = FontWeight.Bold
                )
                val isUltimate = rank.contains("DIEU")
                
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "glowAlpha"
                )

                Surface(
                    color = if (isUltimate) Color(0xFFFFD700).copy(alpha = 0.2f) else AlambicPurple.copy(alpha = 0.2f),
                    border = BorderStroke(
                        if (isUltimate) 2.dp else 1.dp, 
                        if (isUltimate) Color(0xFFFFD700).copy(alpha = glowAlpha) else AlambicPurple.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = if (isUltimate) 8.dp else 0.dp
                ) {
                    Text(
                        text = rank,
                        color = if (isUltimate) Color(0xFFFFD700) else AlambicPurple,
                        fontSize = if (isUltimate) 11.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = if (isUltimate) androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF8B5A00), blurRadius = 10f)
                        ) else androidx.compose.ui.text.TextStyle()
                    )
                }
            }
            Text(
                text = "Votre encyclopédie des éléments découverts",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = BrassGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = BrassGold
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedTabIndex) {
            0 -> FormulesTab(viewModel, onNavigateToFormula)
            1 -> EssencesTab(viewModel)
        }
    }
}

@Composable
fun FormulesTab(viewModel: CocktailViewModel, onNavigateToFormula: (CocktailEntity) -> Unit) {
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    val totalCocktails by viewModel.totalCocktailsCount.collectAsStateWithLifecycle()
    var selectedRarity by remember { mutableStateOf<Rarity?>(null) }
    
    val allCocktails = (localState as? CocktailLocalState.Success)?.cocktails ?: emptyList()
    val filteredCocktails = remember(allCocktails, selectedRarity) {
        if (selectedRarity == null) allCocktails
        else allCocktails.filter {
            Rarity.computeCocktailRarity(it.name) == selectedRarity
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                ProgressBarCard(
                    label = "Grimoire des Formules",
                    current = allCocktails.size,
                    total = totalCocktails,
                    color = AlambicCyan
                )
                Spacer(modifier = Modifier.height(16.dp))
                RarityFilterRow(selectedRarity) { selectedRarity = it }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (localState is CocktailLocalState.Empty) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyStateView("Aucune formule découverte")
            }
        } else {
            items(filteredCocktails) { cocktail ->
                val rarity = remember(cocktail.name) {
                    Rarity.computeCocktailRarity(cocktail.name)
                }
                CocktailCard(cocktail, rarity) { onNavigateToFormula(cocktail) }
            }
        }
    }
}

@Composable
fun EssencesTab(viewModel: CocktailViewModel) {
    val allPossibleIngredients by viewModel.allPossibleIngredients.collectAsStateWithLifecycle()
    val discoveredIngredients by viewModel.discoveredIngredients.collectAsStateWithLifecycle()
    val ingredientDetails by viewModel.ingredientDetails.collectAsStateWithLifecycle()

    // NOUVEAU : Récupération des listes séparées pour le filtrage
    val alcoholicBases by viewModel.alcoholicBases.collectAsStateWithLifecycle()
    val nonAlcoholicEssences by viewModel.nonAlcoholicEssences.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRarity by remember { mutableStateOf<Rarity?>(null) }
    var selectedType by remember { mutableStateOf("TOUT") } // "TOUT", "ALCOOL", "ESSENCE"

    var showDialog by remember { mutableStateOf(false) }
    var pageSize by remember { mutableIntStateOf(30) }

    val filteredIngredients = remember(allPossibleIngredients, discoveredIngredients, searchQuery, selectedRarity, selectedType, alcoholicBases, nonAlcoholicEssences) {
        allPossibleIngredients
            .filter { name ->
                val matchesSearch = name.contains(searchQuery, ignoreCase = true)
                val matchesRarity = selectedRarity == null || Rarity.getIngredientRarity(name) == selectedRarity

                // NOUVEAU : Filtre par type d'ingrédient
                val matchesType = when (selectedType) {
                    "ALCOOL" -> alcoholicBases.contains(name)
                    "ESSENCE" -> nonAlcoholicEssences.contains(name)
                    else -> true
                }

                matchesSearch && matchesRarity && matchesType
            }
            .sortedWith(compareByDescending<String> { discoveredIngredients.contains(it) }.thenBy { it })
    }

    val pagedIngredients = filteredIngredients.take(pageSize)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                ProgressBarCard(
                    label = "Essences de la Nature",
                    current = discoveredIngredients.size,
                    total = allPossibleIngredients.size,
                    color = AlambicOrange
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Rechercher une essence...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassGold,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // NOUVEAU : Boutons de filtre par Type
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TOUT", "ALCOOL", "ESSENCE").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AlambicCyan.copy(alpha = 0.2f),
                                selectedLabelColor = AlambicCyan,
                                labelColor = Color.Gray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Gray.copy(alpha = 0.3f),
                                selectedBorderColor = AlambicCyan,
                                enabled = true,
                                selected = selectedType == type
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtre de Rareté
                RarityFilterRow(selectedRarity) { selectedRarity = it }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(modifier = Modifier.weight(1f), count = discoveredIngredients.size, label = "DÉCOUVERTES", color = AlambicPurple)
                    StatCard(modifier = Modifier.weight(1f), count = allPossibleIngredients.size - discoveredIngredients.size, label = "À TROUVER", color = Color.Gray)
                }
            }
        }

        items(pagedIngredients) { name ->
            val isDiscovered = discoveredIngredients.contains(name)
            val rarity = Rarity.getIngredientRarity(name)
            IngredientItem(name, isDiscovered, rarity) {
                viewModel.getIngredientDetails(name)
                showDialog = true
            }
        }

        if (pageSize < filteredIngredients.size) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Button(
                    onClick = { pageSize += 30 },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.3f))
                ) {
                    Text("CHARGER PLUS D'ESSENCES", color = BrassGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    val currentDetails = ingredientDetails
    if (showDialog && currentDetails != null) {
        IngredientDetailDialog(currentDetails) {
            showDialog = false
            viewModel.clearIngredientDetails()
        }
    }
}

@Composable
fun ProgressBarCard(label: String, current: Int, total: Int, color: Color) {
    val progress = if (total > 0) current.toFloat() / total else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Medium)
                Text("$current / $total", color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = color,
                trackColor = Color.Black.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun RarityFilterRow(selectedRarity: Rarity?, onSelect: (Rarity?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedRarity == null,
            onClick = { onSelect(null) },
            label = { Text("TOUT") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrassGold.copy(alpha = 0.2f),
                selectedLabelColor = BrassGold
            )
        )
        listOf(Rarity.COMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY).forEach { rarity ->
            FilterChip(
                selected = selectedRarity == rarity,
                onClick = { onSelect(rarity) },
                label = { Text(rarity.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = rarity.color.copy(alpha = 0.2f),
                    selectedLabelColor = rarity.color,
                    labelColor = rarity.color.copy(alpha = 0.6f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = rarity.color.copy(alpha = 0.3f),
                    selectedBorderColor = rarity.color,
                    enabled = true,
                    selected = selectedRarity == rarity
                )
            )
        }
    }
}

@Composable
fun CocktailCard(cocktail: CocktailEntity, rarity: Rarity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .shadow(
                elevation = if (rarity == Rarity.LEGENDARY || rarity == Rarity.EPIC) 16.dp else 4.dp,
                spotColor = rarity.color,
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, rarity.color.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Column {
                AsyncImage(
                    model = cocktail.imageUrl,
                    contentDescription = cocktail.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = cocktail.name,
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = rarity.color.copy(alpha = 0.9f),
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = rarity.name,
                    color = if (rarity == Rarity.LEGENDARY) Color.Black else Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun IngredientItem(name: String, isDiscovered: Boolean, rarity: Rarity, onClick: () -> Unit) {
    val grayMatrix = remember { ColorMatrix().apply { setToSaturation(0f) } }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg.copy(alpha = 0.5f))
            .clickable(enabled = isDiscovered) { onClick() }
            .border(
                width = 1.dp,
                color = if (isDiscovered) rarity.color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://www.thecocktaildb.com/images/ingredients/$name-Small.png",
                contentDescription = name,
                modifier = Modifier.fillMaxSize().alpha(if (isDiscovered) 1f else 0.4f),
                contentScale = ContentScale.Fit,
                colorFilter = if (isDiscovered) null else ColorFilter.colorMatrix(grayMatrix)
            )
            if (!isDiscovered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                listOf(Color(0xFF333333).copy(alpha = 0.7f), Color(0xFF111111).copy(alpha = 0.9f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BrassGold.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        Text(
            text = if (isDiscovered) name else "???",
            fontSize = 10.sp,
            color = if (isDiscovered) Color.White else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun IngredientDetailDialog(details: IngredientDTO, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(details.strIngredient, color = BrassGold, fontWeight = FontWeight.Bold) },
        text = { 
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = details.strDescription ?: "Cette essence garde ses secrets bien cachés...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("REVENIR AU CODEX", color = AlambicCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF151925),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EmptyStateView(message: String) {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚗️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, count: Int, label: String, color: Color) {
    Card(
        modifier = modifier.height(70.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Medium)
        }
    }
}
