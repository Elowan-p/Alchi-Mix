package com.sdv.alchimix.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sdv.alchimix.view.AlambicPurple
import com.sdv.alchimix.view.CardBg

@Composable
fun CocktailResultCard(
    cocktail: CocktailDTO,
    isFavorite: Boolean,
    tagText: String,
    computeRarityByDrinkName: Boolean = true,
    showPreviewIngredients: Boolean = false,
    onToggleFavorite: () -> Unit,
    onShowFullFormula: () -> Unit
) {
    val rarity = remember(cocktail) { 
        if (computeRarityByDrinkName) Rarity.computeCocktailRarity(cocktail.strDrink) 
        else Rarity.computeRarity(cocktail.getIngredients()) 
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, AlambicPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Text(
                text = tagText,
                color = AlambicPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(2.dp, rarity.color.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column {
                Box {
                    AsyncImage(
                        model = cocktail.strDrinkThumb,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        color = rarity.color.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = rarity.name,
                            color = if (rarity == Rarity.LEGENDARY) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = cocktail.strDrink.uppercase(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${cocktail.strCategory} ${if (cocktail.strGlass != null) "· " + cocktail.strGlass else ""}",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (showPreviewIngredients) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOfNotNull(cocktail.strIngredient1, cocktail.strIngredient2, cocktail.strIngredient3)
                                .take(3).forEach {
                                Surface(
                                    color = AlambicPurple.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.border(1.dp, AlambicPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = it, 
                                        color = AlambicPurple, 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = onShowFullFormula,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(AlambicPurple, AlambicCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Voir la Formule Complète", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
