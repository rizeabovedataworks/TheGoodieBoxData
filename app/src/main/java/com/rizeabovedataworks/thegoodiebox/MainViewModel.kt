package com.rizeabovedataworks.thegoodiebox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel managing application state, recipe search filtering, category streams,
 * and automatic background Over-The-Air (OTA) cloud dataset syncing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    private val _selectedCategory = MutableStateFlow("Nuts and Snacks")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Reactive StateFlow stream of distinct categories discovered in database.
     */
    val categoriesState: StateFlow<List<String>> = repository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Favorites", "Nuts and Snacks", "Cookies", "Bars", "Candies", "Fudge", "Bread", "Frostings", "Main Dishes")
        )

    /**
     * Reactive StateFlow stream of recipes filtered by selected category and search query.
     */
    val recipesState: StateFlow<List<Recipe>> = combine(_selectedCategory, _searchQuery) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        repository.getRecipesFlow(category, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            // Seed local database from assets on first launch or asset version bump
            repository.ensureDatabaseSeeded()

            // Perform silent background OTA auto-sync on app startup
            try {
                repository.syncOta()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, isFavorite)
        }
    }

    /**
     * User-triggered manual OTA sync callback.
     */
    fun syncOta(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.syncOta()
            onResult(result.first, result.second)
        }
    }
}
