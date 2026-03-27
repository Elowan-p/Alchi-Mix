package com.sdv.alchimix.data.local.dao

import androidx.room.*
import com.sdv.alchimix.data.local.entities.CocktailEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CocktailDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cocktail: CocktailEntity)

    @Update
    suspend fun update(cocktail: CocktailEntity)

    @Query("SELECT * FROM cocktails WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CocktailEntity?

    @Query("UPDATE cocktails SET deletedAt = :date WHERE id = :id")
    suspend fun softDelete(id: Int, date: Date)

    @Query("SELECT COUNT(*) FROM cocktails")
    suspend fun getCocktailCount(): Int

    @Query("SELECT * FROM cocktails WHERE isDiscovered = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllVisibleCocktails(): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM cocktails WHERE isFavorite = 1 AND isDiscovered = 1 AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getFavoriteCocktails(): Flow<List<CocktailEntity>>
}
