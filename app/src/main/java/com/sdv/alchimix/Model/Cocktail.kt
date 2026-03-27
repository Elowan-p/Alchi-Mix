package com.sdv.alchimix.model

import com.google.gson.annotations.SerializedName

data class CocktailResponse(
    val drinks: List<CocktailDTO>?
)

data class CocktailDTO(
    val idDrink: String,
    val strDrink: String,
    val strDrinkThumb: String,
    val strCategory: String?,
    val strAlcoholic: String?,
    val strGlass: String?,
    val strInstructions: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?
) {
    fun getIngredients(): List<String> {
        return listOfNotNull(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15
        ).filter { it.isNotBlank() }
    }
}

data class IngredientResponse(
    val ingredients: List<IngredientDTO>?
)

data class IngredientDTO(
    val idIngredient: String?,
    val strIngredient: String,
    val strDescription: String?,
    val strType: String?,
    val strAlcohol: String?,
    val strABV: String?
)

data class IngredientListResponse(
    val drinks: List<IngredientItemDTO>?
)

data class IngredientItemDTO(
    @SerializedName("strIngredient1") val strIngredient: String
)
