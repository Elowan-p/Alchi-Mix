package com.sdv.alchimix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sdv.alchimix.data.local.dao.CocktailDao
import com.sdv.alchimix.data.local.dao.IngredientDao
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.data.local.entities.IngredientEntity

@Database(entities = [CocktailEntity::class, IngredientEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CocktailDatabase : RoomDatabase() {
    abstract fun cocktailDao(): CocktailDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: CocktailDatabase? = null

        fun getDatabase(context: Context): CocktailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CocktailDatabase::class.java,
                    "cocktail_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}