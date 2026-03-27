package com.sdv.alchimix.utils

import androidx.compose.ui.graphics.Color

sealed interface Rarity : Comparable<Rarity> {
    val name: String
    val color: Color
    val rank: Int

    override fun compareTo(other: Rarity): Int = this.rank.compareTo(other.rank)

    object LEGENDARY : Rarity {
        override val name = "LÉGENDAIRE"
        override val color = Color(0xFFFFD700) // Or
        override val rank = 3
    }

    object EPIC : Rarity {
        override val name = "ÉPIQUE"
        override val color = Color(0xFF9400D3) // Violet
        override val rank = 2
    }

    object RARE : Rarity {
        override val name = "RARE"
        override val color = Color(0xFF1E90FF) // Bleu
        override val rank = 1
    }

    object COMMON : Rarity {
        override val name = "COMMUN"
        override val color = Color(0xFF808080) // Gris
        override val rank = 0
    }

    companion object {

        fun computeRarity(ingredients: List<String?>): Rarity {
            val validIngredients = ingredients.filter { !it.isNullOrBlank() }
            val count = validIngredients.size
            val names = validIngredients.map { it?.lowercase() ?: "" }

            return when {
                count > 6 || names.any { it.contains("champagne") || it.contains("cognac") || it.contains("saffron") } -> LEGENDARY
                count >= 5 || names.any { it.contains("tequila") || it.contains("whiskey") || it.contains("absinthe") } -> EPIC
                count >= 3 -> RARE
                else -> COMMON
            }
        }

        fun computeCocktailRarity(cocktailName: String?): Rarity {
            if (cocktailName == null) return COMMON

            val hashScore = cocktailName.uppercase().hashCode() % 100
            val normalizedScore = if (hashScore < 0) -hashScore else hashScore

            return when {
                normalizedScore > 94 -> LEGENDARY
                normalizedScore > 79 -> EPIC
                normalizedScore > 49 -> RARE
                else -> COMMON
            }
        }

        fun getIngredientRarity(ingredientName: String?): Rarity {
            if (ingredientName.isNullOrBlank()) return COMMON
            val name = ingredientName.lowercase()

            return when {
                name.contains("champagne") || name.contains("cognac") ||
                        name.contains("saffron") || name.contains("gold") ||
                        name.contains("truffle") || name.contains("caviar") ||
                        name.contains("orchid") || name.contains("lotus") -> LEGENDARY

                name.contains("tequila") || name.contains("whiskey") ||
                        name.contains("absinthe") || name.contains("bourbon") ||
                        name.contains("scotch") || name.contains("mezcal") ||
                        name.contains("passion fruit") || name.contains("agave") ||
                        name.contains("elderflower") || name.contains("lavender") ||
                        name.contains("cardamom") || name.contains("hibiscus") ||
                        name.contains("matcha") || name.contains("yuzu") ||
                        name.contains("rose") || name.contains("dragon fruit") -> EPIC

                name.contains("vodka") || name.contains("gin") ||
                        name.contains("rum") || name.contains("brandy") ||
                        name.contains("vermouth") || name.contains("liqueur") ||
                        name.contains("wine") || name.contains("sake") ||
                        name.contains("pisco") || name.contains("mint") ||
                        name.contains("basil") || name.contains("rosemary") ||
                        name.contains("cinnamon") || name.contains("nutmeg") ||
                        name.contains("blackberry") || name.contains("blueberry") ||
                        name.contains("honey") || name.contains("maple") ||
                        name.contains("espresso") || name.contains("tonic") ||
                        name.contains("ginger beer") -> RARE

                else -> COMMON
            }
        }
    }
}