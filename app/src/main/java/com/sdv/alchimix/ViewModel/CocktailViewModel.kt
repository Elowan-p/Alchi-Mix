package com.sdv.alchimix.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdv.alchimix.data.RetrofitInstance
import com.sdv.alchimix.data.local.CocktailDatabase
import com.sdv.alchimix.data.local.entities.CocktailEntity
import com.sdv.alchimix.data.local.entities.IngredientEntity
import com.sdv.alchimix.model.CocktailDTO
import com.sdv.alchimix.model.IngredientDTO
import com.sdv.alchimix.utils.CocktailLocalState
import com.sdv.alchimix.utils.Rarity
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import com.sdv.alchimix.data.repository.CocktailRepository

sealed interface CocktailState {
    object Idle : CocktailState
    object Loading : CocktailState
    data class Success(val cocktail: CocktailDTO) : CocktailState
    data class Error(val message: String) : CocktailState
}


class CocktailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CocktailDatabase.getDatabase(application)
    private val repository = CocktailRepository(db.cocktailDao(), db.ingredientDao())

    private val _state = MutableStateFlow<CocktailState>(CocktailState.Idle)
    val state: StateFlow<CocktailState> = _state

    private val _localState = MutableStateFlow<CocktailLocalState>(CocktailLocalState.Loading)
    val localState: StateFlow<CocktailLocalState> = _localState

    private val _ingredientDetails = MutableStateFlow<IngredientDTO?>(null)
    val ingredientDetails: StateFlow<IngredientDTO?> = _ingredientDetails.asStateFlow()

    // --- NOUVEAU : États pour le Splash Screen ---
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    private val _initProgress = MutableStateFlow(0f)
    val initProgress: StateFlow<Float> = _initProgress.asStateFlow()

    private val dbIngredients = repository.getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alcoholicBases: StateFlow<List<String>> = dbIngredients.map { list ->
        val bases = list.filter { it.isAlcoholic }.map { it.name }.sorted()
        if (bases.isEmpty()) listOf("Vodka", "Gin", "Rum") else bases
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Vodka", "Gin", "Rum"))

    val nonAlcoholicEssences: StateFlow<List<String>> = dbIngredients.map { list ->
        val essences = list.filter { !it.isAlcoholic }.map { it.name }.sorted()
        if (essences.isEmpty()) listOf("Lemon", "Sugar", "Mint") else essences
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Lemon", "Sugar", "Mint"))


    init {
        viewModelScope.launch {
            repository.getAllVisibleCocktails().collectLatest { cocktails ->
                _localState.value = if (cocktails.isEmpty()) CocktailLocalState.Empty else CocktailLocalState.Success(cocktails)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val countIngredients = repository.getIngredientCount()
            val countCocktails = try { repository.getCocktailCount() } catch (e: Exception) { 0 }

            val totalExpectedIngredients = 488
            val totalExpectedCocktails = 600

            if (countIngredients >= totalExpectedIngredients && countCocktails >= totalExpectedCocktails) {
                Log.d("VM", "Données déjà présentes (Ing: $countIngredients, Cocktails: $countCocktails). Lancement fake loading.")
                for (i in 1..100) {
                    _initProgress.value = i / 100f
                    delay(15)
                }
                _isAppReady.value = true
            } else {
                Log.d("VM", "Téléchargement initial obligatoire.")
                
                // 1. Ingrédients de 0% à 50%
                if (countIngredients < totalExpectedIngredients) {
                    repository.syncAllPossibleIngredients(
                        onProgress = { _initProgress.value = it * 0.5f },
                        isBackground = false
                    )
                } else {
                    _initProgress.value = 0.5f
                }

                // 2. Cocktails de 50% à 100%
                if (countCocktails < totalExpectedCocktails) {
                    repository.syncAllPossibleCocktails(
                        onProgress = { _initProgress.value = 0.5f + (it * 0.5f) },
                        isBackground = false
                    )
                } else {
                    _initProgress.value = 1f
                }

                _isAppReady.value = true
            }
        }
    }

    fun getIngredientDetails(name: String) {
        viewModelScope.launch {
            repository.getIngredientDetails(name).onSuccess {
                _ingredientDetails.value = it
            }.onFailure {
                Log.e("VM", "Error fetching ingredient details", it)
            }
        }
    }

    fun clearIngredientDetails() {
        _ingredientDetails.value = null
    }

    fun resetState() {
        _state.value = CocktailState.Idle
    }

    fun toggleFavorite(cocktail: CocktailEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(cocktail)
        }
    }

    fun archiveCocktail(id: Int) {
        viewModelScope.launch {
            repository.archiveCocktail(id)
        }
    }

    val discoveredIngredients: StateFlow<Set<String>> = localState.map { state ->
        if (state is CocktailLocalState.Success) {
            state.cocktails.flatMap { it.ingredients?.split(",") ?: emptyList() }
                .filter { it.isNotBlank() }
                .map { it.trim().lowercase().replaceFirstChar { c -> c.uppercaseChar() } }
                .toSet()
        } else emptySet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allPossibleIngredients: StateFlow<List<String>> = combine(
        dbIngredients,
        discoveredIngredients
    ) { dbList, discovered ->
        (dbList.map { it.name } + discovered)
            .map { it.trim().lowercase().replaceFirstChar { c -> c.uppercaseChar() } }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val discoveredAlcohols: StateFlow<List<String>> = combine(
        dbIngredients,
        discoveredIngredients
    ) { dbList, discovered ->
        dbList.filter { it.isAlcoholic && discovered.contains(it.name.trim().lowercase().replaceFirstChar { c -> c.uppercaseChar() }) }
            .map { it.name }.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val discoveredEssences: StateFlow<List<String>> = combine(
        dbIngredients,
        discoveredIngredients
    ) { dbList, discovered ->
        dbList.filter { !it.isAlcoholic && discovered.contains(it.name.trim().lowercase().replaceFirstChar { c -> c.uppercaseChar() }) }
            .map { it.name }.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masteryStatus: StateFlow<String> = localState.map { state ->
        val count = if (state is CocktailLocalState.Success) state.cocktails.size else 0
        when {
            count >= 50 -> "Grand Alchimiste 🧙‍♂️"
            count >= 5 -> "Apprenti Alchimiste ⚗️"
            else -> "Novice du Chaos 🧪"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Novice du Chaos 🧪")

    fun transmute(letter: String, mandatoryIngredients: List<String>, targetRarity: Rarity? = null) {
        viewModelScope.launch {
            _state.value = CocktailState.Loading
            repository.transmuteCocktail(letter, mandatoryIngredients, targetRarity).onSuccess {
                _state.value = CocktailState.Success(it)
            }.onFailure {
                _state.value = CocktailState.Error(it.message ?: "Erreur mystique")
            }
        }
    }

    fun addCocktail(name: String, instructions: String) {
        viewModelScope.launch {
            repository.addCustomCocktail(name, instructions)
        }
    }

    fun saveCocktailFromApi(cocktail: CocktailDTO) {
        viewModelScope.launch {
            repository.saveCocktailFromApi(cocktail)
        }
    }

    fun deleteCocktail(cocktail: CocktailEntity) {
        viewModelScope.launch {
            repository.deleteCocktail(cocktail)
        }
    }
}