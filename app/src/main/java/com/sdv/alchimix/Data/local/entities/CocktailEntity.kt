package com.sdv.alchimix.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "cocktails",
    indices = [Index(value = ["name"], unique = true)]
)
data class CocktailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val instructions: String,
    val imageUrl: String? = null,
    val category: String? = "Cocktail",
    val isFavorite: Boolean = false,
    val isDiscovered: Boolean = false,
    val ingredients: String? = null,
    val measures: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date? = null,
    val deletedAt: Date? = null
)
