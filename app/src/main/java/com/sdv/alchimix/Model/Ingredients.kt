package com.sdv.alchimix.model

import com.sdv.alchimix.utils.Rarity

data class Ingredients(
    val name: String,
    val imageUrl: String,
    val rarity: Rarity,
    val isDiscovered: Boolean = false
)
