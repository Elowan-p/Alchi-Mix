package com.sdv.alchimix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sdv.alchimix.data.local.dao.CocktailDao
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.sdv.alchimix.data.local.dao.IngredientDao
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.data.local.entities.IngredientEntity

@Database(entities = [CocktailEntity::class, IngredientEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CocktailDatabase : RoomDatabase() {
    abstract fun cocktailDao(): CocktailDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: CocktailDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cocktails ADD COLUMN isDiscovered INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): CocktailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CocktailDatabase::class.java,
                    "cocktail_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}