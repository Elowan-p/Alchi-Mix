package com.sdv.alchimix.data

import com.sdv.alchimix.model.CocktailResponse
import com.sdv.alchimix.model.IngredientResponse
import com.sdv.alchimix.model.IngredientListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("search.php")
    suspend fun getByFirstLetter(@Query("f") letter: String): CocktailResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): CocktailResponse

    @GET("lookup.php")
    suspend fun getById(@Query("i") id: String): CocktailResponse

    @GET("random.php")
    suspend fun getRandom(): CocktailResponse

    @GET("search.php")
    suspend fun searchIngredientByName(@Query("i") ingredient: String): IngredientResponse

    @GET("list.php?i=list")
    suspend fun listIngredients(): IngredientListResponse

    @GET("lookup.php")
    suspend fun getIngredientById(@Query("iid") id: Int): IngredientResponse
}
