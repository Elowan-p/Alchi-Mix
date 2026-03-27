package com.sdv.alchimix.data.repository

import android.util.Log
import com.sdv.alchimix.data.RetrofitInstance
import com.sdv.alchimix.data.local.dao.CocktailDao
import com.sdv.alchimix.data.local.dao.IngredientDao
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.data.local.entities.IngredientEntity
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.model.IngredientDTO
import com.sdv.alchimix.utils.Rarity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date

class CocktailRepository(
    private val cocktailDao: CocktailDao,
    private val ingredientDao: IngredientDao
) {
    fun getAllVisibleCocktails(): Flow<List<CocktailEntity>> = cocktailDao.getAllVisibleCocktails()
    fun getAllIngredients(): Flow<List<IngredientEntity>> = ingredientDao.getAllIngredients()

    suspend fun getIngredientCount(): Int = ingredientDao.getIngredientCount()
    suspend fun getCocktailCount(): Int = cocktailDao.getCocktailCount()

    suspend fun syncAllPossibleIngredients(
        onProgress: (Float) -> Unit,
        isBackground: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            for (id in 1..616) {
                try {
                    val response = RetrofitInstance.api.getIngredientById(id)
                    val ing = response.ingredients?.firstOrNull()

                    if (ing != null) {
                        val name = ing.strIngredient.trim().lowercase().replaceFirstChar { c -> c.uppercaseChar() }
                        val isAlc = ing.strAlcohol.equals("Yes", ignoreCase = true)
                        ingredientDao.insertAll(listOf(IngredientEntity(name = name, isAlcoholic = isAlc)))
                    }
                } catch (e: Exception) {
                }
                onProgress(id.toFloat() / 616f)
                delay(if (isBackground) 300L else 150L)
            }
            Log.d("CocktailRepository", "Synchronisation des ingrédients terminée.")
        } catch (e: Exception) {
            Log.e("CocktailRepository", "Erreur lors du scraping", e)
        }
    }

    suspend fun syncAllPossibleCocktails(
        onProgress: (Float) -> Unit,
        isBackground: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            val total = 11600 - 11000 + 1
            for (id in 11000..11600) {
                try {
                    val response = RetrofitInstance.api.getById(id.toString())
                    val cocktail = response.drinks?.firstOrNull()

                    if (cocktail != null) {
                        val name = cocktail.strDrink.trim()
                        val existing = cocktailDao.getByName(name)
                        if (existing == null) {
                            val ingredientsString = cocktail.getIngredients().joinToString(",")
                            val measuresString = listOfNotNull(
                                cocktail.strMeasure1, cocktail.strMeasure2, cocktail.strMeasure3, cocktail.strMeasure4,
                                cocktail.strMeasure5, cocktail.strMeasure6, cocktail.strMeasure7, cocktail.strMeasure8
                            ).joinToString(",")

                            cocktailDao.insert(
                                CocktailEntity(
                                    name = name,
                                    instructions = cocktail.strInstructions ?: "Aucune instruction",
                                    imageUrl = cocktail.strDrinkThumb,
                                    category = cocktail.strCategory,
                                    ingredients = ingredientsString,
                                    measures = measuresString,
                                    isDiscovered = false
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                }
                onProgress((id - 11000 + 1).toFloat() / total.toFloat())
                delay(if (isBackground) 300L else 150L)
            }
            Log.d("CocktailRepository", "Synchronisation des cocktails terminée.")
        } catch (e: Exception) {
            Log.e("CocktailRepository", "Erreur lors du scraping des cocktails", e)
        }
    }

    suspend fun getIngredientDetails(name: String): Result<IngredientDTO?> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.searchIngredientByName(name)
            Result.success(response.ingredients?.firstOrNull())
        } catch (e: Exception) {
            Log.e("CocktailRepository", "Error fetching ingredient details", e)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(cocktail: CocktailEntity) = withContext(Dispatchers.IO) {
        cocktailDao.update(cocktail.copy(isFavorite = !cocktail.isFavorite, updatedAt = Date()))
    }

    suspend fun archiveCocktail(id: Int) = withContext(Dispatchers.IO) {
        cocktailDao.softDelete(id, Date())
    }
    suspend fun addCustomCocktail(name: String, instructions: String) = withContext(Dispatchers.IO) {
        cocktailDao.insert(CocktailEntity(
            name = name,
            instructions = instructions,
            category = "Custom",
            isDiscovered = true
        ))
    }

    suspend fun discoverCocktail(cocktail: CocktailDTO) = withContext(Dispatchers.IO) {
        val name = cocktail.strDrink.trim()
        val existing = cocktailDao.getByName(name)
        val ingredientsString = cocktail.getIngredients().joinToString(",")
        val measuresString = listOfNotNull(
            cocktail.strMeasure1, cocktail.strMeasure2, cocktail.strMeasure3, cocktail.strMeasure4,
            cocktail.strMeasure5, cocktail.strMeasure6, cocktail.strMeasure7, cocktail.strMeasure8
        ).joinToString(",")

        if (existing == null) {
            cocktailDao.insert(
                CocktailEntity(
                    name = name,
                    instructions = cocktail.strInstructions ?: "Aucune instruction",
                    imageUrl = cocktail.strDrinkThumb,
                    category = cocktail.strCategory,
                    ingredients = ingredientsString,
                    measures = measuresString,
                    isDiscovered = true
                )
            )
        } else {
            cocktailDao.update(
                existing.copy(
                    deletedAt = null,
                    category = cocktail.strCategory,
                    ingredients = ingredientsString,
                    measures = measuresString,
                    isDiscovered = true,
                    updatedAt = Date()
                )
            )
        }
    }

    suspend fun saveCocktailFromApi(cocktail: CocktailDTO) = withContext(Dispatchers.IO) {
        val name = cocktail.strDrink.trim()
        val existing = cocktailDao.getByName(name)
        val ingredientsString = cocktail.getIngredients().joinToString(",")
        val measuresString = listOfNotNull(
            cocktail.strMeasure1, cocktail.strMeasure2, cocktail.strMeasure3, cocktail.strMeasure4,
            cocktail.strMeasure5, cocktail.strMeasure6, cocktail.strMeasure7, cocktail.strMeasure8
        ).joinToString(",")

        if (existing != null) {
            cocktailDao.update(existing.copy(
                isFavorite = !existing.isFavorite,
                deletedAt = null,
                updatedAt = Date(),
                isDiscovered = true,
                ingredients = ingredientsString,
                measures = measuresString
            ))
        } else {
            cocktailDao.insert(CocktailEntity(
                name = name,
                instructions = cocktail.strInstructions ?: "Aucune instruction",
                imageUrl = cocktail.strDrinkThumb,
                isFavorite = true,
                isDiscovered = true,
                category = cocktail.strCategory,
                ingredients = ingredientsString,
                measures = measuresString
            ))
        }
    }

    suspend fun deleteCocktail(cocktail: CocktailEntity) = withContext(Dispatchers.IO) {
        cocktailDao.update(cocktail.copy(deletedAt = Date()))
    }

    private fun getIngredientVariants(ing: String): List<String> {
        return when (ing.lowercase()) {
            "whiskey" -> listOf("Whiskey", "Whisky", "Bourbon", "Scotch")
            "rum" -> listOf("Light rum", "Dark rum", "Rum")
            "vodka" -> listOf("Vodka")
            "gin" -> listOf("Gin")
            "tequila" -> listOf("Tequila")
            "citrus" -> listOf("Lemon juice", "Lime juice", "Orange juice", "Lemon", "Lime")
            "lemon" -> listOf("Lemon juice", "Lemon")
            "lime" -> listOf("Lime juice", "Lime")
            "sweet" -> listOf("Sugar syrup", "Honey", "Grenadine", "Sugar")
            "sugar" -> listOf("Sugar syrup", "Sugar", "Simple Syrup")
            "honey" -> listOf("Honey")
            "mint" -> listOf("Mint")
            "bitter", "bitters" -> listOf("Angostura bitters", "Bitters", "Campari")
            "spicy" -> listOf("Ginger", "Tabasco sauce", "Cayenne pepper")
            "herbal" -> listOf("Mint", "Basil", "Sage")
            "soda" -> listOf("Soda water", "Carbonated water")
            "tonic" -> listOf("Tonic water")
            "ginger" -> listOf("Ginger ale", "Ginger beer", "Ginger")
            "ice" -> listOf("Ice")
            else -> listOf(ing)
        }
    }

    suspend fun transmuteCocktail(letter: String, mandatoryIngredients: List<String>, targetRarity: Rarity? = null): Result<CocktailDTO> = withContext(Dispatchers.IO) {
        try {
            val activeIngredients = mandatoryIngredients.filter { it.isNotBlank() }
            val candidates = mutableSetOf<String>()

            if (activeIngredients.isNotEmpty()) {
                val deferred: List<Deferred<Set<String>>> = activeIngredients.map { ing ->
                    async {
                        val variants = getIngredientVariants(ing)
                        val ids = mutableSetOf<String>()
                        for (v in variants) {
                            try {
                                RetrofitInstance.api.filterByIngredient(v).drinks?.forEach { ids.add(it.idDrink) }
                            } catch (e: Exception) {}
                        }
                        ids
                    }
                }
                val results: List<Set<String>> = deferred.awaitAll()
                if (results.isNotEmpty()) {
                    var currentMatch: Set<String> = results[0]
                    results.drop(1).forEach { res ->
                        val intersect = currentMatch.intersect(res)
                        if (intersect.isNotEmpty()) {
                            currentMatch = intersect
                        }
                    }
                    candidates.addAll(currentMatch)
                }
            }

            if (candidates.isEmpty()) {
                try {
                    RetrofitInstance.api.getByFirstLetter(letter).drinks?.forEach { candidates.add(it.idDrink) }
                } catch (e: Exception) {}
            }

            var bestCocktail: CocktailDTO? = null
            val sampledIds = candidates.toList().shuffled().take(20)

            for (id in sampledIds) {
                try {
                    val details = RetrofitInstance.api.getById(id).drinks?.firstOrNull()
                    if (details != null) {
                        val currentRarity = Rarity.computeCocktailRarity(details.strDrink)
                        if (targetRarity == null || currentRarity == targetRarity) {
                            bestCocktail = details
                            break
                        }
                    }
                } catch (e: Exception) {}
            }

            if (targetRarity != null && (bestCocktail == null || Rarity.computeCocktailRarity(bestCocktail.strDrink) != targetRarity)) {
                val letters = ('a'..'z').toList().shuffled()
                searchLoop@for (l in letters) {
                    try {
                        val resp = RetrofitInstance.api.getByFirstLetter(l.toString())
                        val matches = resp.drinks?.shuffled()?.take(5) ?: emptyList()
                        for (c in matches) {
                            val details = RetrofitInstance.api.getById(c.idDrink).drinks?.firstOrNull()
                            if (details != null && Rarity.computeCocktailRarity(details.strDrink) == targetRarity) {
                                bestCocktail = details
                                break@searchLoop
                            }
                        }
                    } catch (e: Exception) {}
                }
            }

            if (bestCocktail != null) {
                discoverCocktail(bestCocktail)
                Result.success(bestCocktail)
            } else {
                Result.failure(Exception("L'alchimie a échoué. Les étoiles ne sont pas alignées."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erreur occulte : ${e.localizedMessage}"))
        }
    }
}
