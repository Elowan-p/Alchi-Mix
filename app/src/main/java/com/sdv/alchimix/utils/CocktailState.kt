package com.sdv.alchimix.utils

import com.sdv.alchimix.data.local.entities.CocktailEntity

sealed interface CocktailLocalState {
    object Loading : CocktailLocalState
    object Empty : CocktailLocalState
    data class Success(val cocktails: List<CocktailEntity>) : CocktailLocalState
    data class Error(val message: String) : CocktailLocalState
}
