package com.sdv.alchimix.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val name: String,
    val isAlcoholic: Boolean
)