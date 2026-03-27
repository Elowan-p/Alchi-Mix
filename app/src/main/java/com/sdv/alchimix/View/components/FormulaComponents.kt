package com.sdv.alchimix.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.utils.Rarity
import com.sdv.alchimix.view.AlambicCyan
import com.sdv.alchimix.view.AlambicOrange
import com.sdv.alchimix.view.AlambicPurple
import com.sdv.alchimix.view.CardBg
import com.sdv.alchimix.view.DarkBg

@Composable
fun FullFormulaView(
    cocktail: CocktailDTO,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isFavoriteLocal: Boolean = false
) {
    val rarity = remember(cocktail) { Rarity.computeCocktailRarity(cocktail.strDrink) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(DarkBg)) {
        Box {
            AsyncImage(
                model = cocktail.strDrinkThumb,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(350.dp),
                contentScale = ContentScale.Crop
            )
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = onSave, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                    Icon(
                        imageVector = if (isFavoriteLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavoriteLocal) Color.Red else Color.White
                    )
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Surface(
                    color = rarity.color.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = rarity.name,
                        color = if (rarity == Rarity.LEGENDARY) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = AlambicPurple.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(cocktail.strAlcoholic ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text(cocktail.strDrink.uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text("📍 ${cocktail.strCategory}", color = Color.Gray, fontSize = 14.sp)
                Text("  ·  ", color = Color.Gray)
                Text("⚗️ ${cocktail.strGlass}", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📖", fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text("INGRÉDIENTS DE LA FORMULE", color = AlambicCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

            val ingredients = listOf(
                cocktail.strIngredient1 to cocktail.strMeasure1,
                cocktail.strIngredient2 to cocktail.strMeasure2,
                cocktail.strIngredient3 to cocktail.strMeasure3,
                cocktail.strIngredient4 to cocktail.strMeasure4,
                cocktail.strIngredient5 to cocktail.strMeasure5,
                cocktail.strIngredient6 to cocktail.strMeasure6,
                cocktail.strIngredient7 to cocktail.strMeasure7,
                cocktail.strIngredient8 to cocktail.strMeasure8,
                cocktail.strIngredient9 to cocktail.strMeasure9,
                cocktail.strIngredient10 to cocktail.strMeasure10,
                cocktail.strIngredient11 to cocktail.strMeasure11,
                cocktail.strIngredient12 to cocktail.strMeasure12,
                cocktail.strIngredient13 to cocktail.strMeasure13,
                cocktail.strIngredient14 to cocktail.strMeasure14,
                cocktail.strIngredient15 to cocktail.strMeasure15
            ).filter { it.first != null }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val chunks = ingredients.chunked(2)
                chunks.forEach { chunk ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        chunk.forEach { (name, measure) ->
                            Box(modifier = Modifier.weight(1f)) {
                                IngredientCard(name ?: "", measure ?: "")
                            }
                        }
                        if (chunk.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🍲", fontSize = 20.sp)
                Spacer(Modifier.width(12.dp))
                Text("INSTRUCTION ALCHIMIQUE", color = AlambicOrange, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.border(1.dp, AlambicOrange.copy(0.3f), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "\"${cocktail.strInstructions}\"",
                    color = AlambicCyan,
                    modifier = Modifier.padding(20.dp),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(AlambicPurple, AlambicCyan))), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFavoriteLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (isFavoriteLocal) "Retirer du Grimoire" else "Sceller dans le Grimoire",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientCard(name: String, measure: String) {
    Surface(
        color = CardBg,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(18.dp))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://www.thecocktaildb.com/images/ingredients/$name-Small.png",
                contentDescription = null,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(measure, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
