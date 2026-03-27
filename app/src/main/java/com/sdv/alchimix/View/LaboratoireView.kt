package com.sdv.alchimix.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.viewmodel.CocktailViewModel
import com.sdv.alchimix.view.components.FullFormulaView
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LaboratoireScreen(viewModel: CocktailViewModel) {
    val localState by viewModel.localState.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Toutes") }
    var selectedLocalCocktailId by remember { mutableStateOf<Int?>(null) }

    val selectedLocalCocktail = if (localState is CocktailLocalState.Success) {
        (localState as CocktailLocalState.Success).cocktails.find { it.id == selectedLocalCocktailId }
    } else null

    if (selectedLocalCocktail != null) {
        val ing = selectedLocalCocktail.ingredients?.split(",")?.map { it.trim() } ?: emptyList()
        val meas = selectedLocalCocktail.measures?.split(",")?.map { it.trim() } ?: emptyList()
        val dto = CocktailDTO(
            idDrink = selectedLocalCocktail.id.toString(),
            strDrink = selectedLocalCocktail.name,
            strInstructions = selectedLocalCocktail.instructions,
            strDrinkThumb = selectedLocalCocktail.imageUrl ?: "",
            strCategory = selectedLocalCocktail.category ?: "Cocktail",
            strAlcoholic = null,
            strGlass = "Cristal",
            strIngredient1 = ing.getOrNull(0), strIngredient2 = ing.getOrNull(1), strIngredient3 = ing.getOrNull(2), strIngredient4 = ing.getOrNull(3),
            strIngredient5 = ing.getOrNull(4), strIngredient6 = ing.getOrNull(5), strIngredient7 = ing.getOrNull(6), strIngredient8 = ing.getOrNull(7),
            strIngredient9 = ing.getOrNull(8), strIngredient10 = ing.getOrNull(9), strIngredient11 = ing.getOrNull(10), strIngredient12 = ing.getOrNull(11),
            strIngredient13 = ing.getOrNull(12), strIngredient14 = ing.getOrNull(13), strIngredient15 = ing.getOrNull(14),
            strMeasure1 = meas.getOrNull(0), strMeasure2 = meas.getOrNull(1), strMeasure3 = meas.getOrNull(2), strMeasure4 = meas.getOrNull(3),
            strMeasure5 = meas.getOrNull(4), strMeasure6 = meas.getOrNull(5), strMeasure7 = meas.getOrNull(6), strMeasure8 = meas.getOrNull(7),
            strMeasure9 = meas.getOrNull(8), strMeasure10 = meas.getOrNull(9), strMeasure11 = meas.getOrNull(10), strMeasure12 = meas.getOrNull(11),
            strMeasure13 = meas.getOrNull(12), strMeasure14 = meas.getOrNull(13), strMeasure15 = meas.getOrNull(14)
        )
        FullFormulaView(
            cocktail = dto,
            isFavoriteLocal = selectedLocalCocktail.isFavorite,
            onBack = { selectedLocalCocktailId = null },
            onSave = { viewModel.toggleFavorite(selectedLocalCocktail) }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp)
        ) {
            Text(
                text = "LE LABORATOIRE",
                color = BrassGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Explorez vos formules découvertes",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher dans le grimoire...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AlambicCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrassGold,
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    cursorColor = AlambicCyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filters = listOf(
                "Toutes", "Favoris", "Ordinary Drink", "Cocktail", "Shot", "Punch", 
                "Shake", "Other", "Cocoa", "Coffee", "Homemade Liqueur", "Beer", "Soft Drink"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(if (filter == "Favoris") "♥ $filter" else filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrassGold.copy(alpha = 0.2f),
                            selectedLabelColor = BrassGold,
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Gray.copy(alpha = 0.3f),
                            selectedBorderColor = BrassGold,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (showAddForm) listOf(Color(0xFF333333), Color(0xFF111111))
                                else listOf(Color(0xFF8B5A00), Color(0xFFE69110))
                            )
                        )
                        .border(1.dp, if (showAddForm) Color.Gray else BrassGold, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showAddForm) "ANNULER LA FORMULE" else "GRAVER UNE NOUVELLE FORMULE",
                        color = if (showAddForm) Color.LightGray else Color(0xFFFFD1A3),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black.copy(0.8f), blurRadius = 4f)
                        )
                    )
                }
            }

            if (showAddForm) {
                AddCocktailForm { name, instructions ->
                    viewModel.addCocktail(name, instructions)
                    showAddForm = false
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (val state = localState) {
                    is CocktailLocalState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AlambicCyan)
                    }
                    is CocktailLocalState.Empty -> {
                        EmptyLaboratoryView()
                    }
                    is CocktailLocalState.Success -> {
                        val filteredCocktails = state.cocktails.filter { cocktail ->
                            val matchesSearch = cocktail.name.contains(searchQuery, ignoreCase = true) || 
                                              cocktail.instructions.contains(searchQuery, ignoreCase = true)
                            
                            val matchesFilter = when (selectedFilter) {
                                "Toutes" -> true
                                "Favoris" -> cocktail.isFavorite
                                "Ordinary Drink" -> cocktail.category?.contains("Ordinary", ignoreCase = true) == true
                                "Cocktail" -> cocktail.category?.contains("Cocktail", ignoreCase = true) == true
                                "Shake" -> cocktail.category?.contains("Shake", ignoreCase = true) == true
                                "Other" -> cocktail.category?.contains("Other", ignoreCase = true) == true
                                "Cocoa" -> cocktail.category?.contains("Cocoa", ignoreCase = true) == true
                                "Shot" -> cocktail.category?.contains("Shot", ignoreCase = true) == true
                                "Coffee" -> cocktail.category?.contains("Coffee", ignoreCase = true) == true
                                "Homemade Liqueur" -> cocktail.category?.contains("Liqueur", ignoreCase = true) == true
                                "Punch" -> cocktail.category?.contains("Punch", ignoreCase = true) == true
                                "Beer" -> cocktail.category?.contains("Beer", ignoreCase = true) == true
                                "Soft Drink" -> cocktail.category?.contains("Soft", ignoreCase = true) == true
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }

                        if (filteredCocktails.isEmpty()) {
                            EmptyLaboratoryView()
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(filteredCocktails, key = { it.id }) { cocktail ->
                                    CocktailItem(
                                        cocktail = cocktail,
                                        onClick = { selectedLocalCocktailId = cocktail.id },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onArchive = { viewModel.archiveCocktail(it.id) }
                                    )
                                }
                            }
                        }
                    }
                    is CocktailLocalState.Error -> {
                        Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun AddCocktailForm(onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, AlambicCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du cocktail") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Recette / Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, instructions) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AlambicCyan)
            ) {
                Text("Enregistrer dans le grimoire", color = Color.Black)
            }
        }
    }
}

@Composable
fun CocktailItem(
    cocktail: CocktailEntity,
    onClick: () -> Unit,
    onToggleFavorite: (CocktailEntity) -> Unit,
    onArchive: (CocktailEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (cocktail.isFavorite) BrassGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cocktail.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = AlambicPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, AlambicPurple.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = cocktail.category ?: "Grimoire",
                            color = AlambicPurple,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = cocktail.instructions,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distillé le : ${dateFormat.format(cocktail.createdAt)}",
                    color = AlambicCyan.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleFavorite(cocktail) }) {
                    Icon(
                        imageVector = if (cocktail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (cocktail.isFavorite) Color.Red else Color.Gray
                    )
                }
                IconButton(onClick = { onArchive(cocktail) }) {
                    Icon(imageVector = Icons.Default.Archive, contentDescription = "Archiver", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyLaboratoryView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF121215), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFC9A66B).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🧪", fontSize = 48.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Le Laboratoire est vide", color = BrassGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "Découvrez des essences d'abord.", color = Color.Gray, fontSize = 14.sp)
    }
}
