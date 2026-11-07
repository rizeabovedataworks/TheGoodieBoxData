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

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    private val _selectedCategory = MutableStateFlow("Nuts and Snacks")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categoriesState: StateFlow<List<String>> = repository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Favorites", "Nuts and Snacks", "Cookies", "Bars", "Candies", "Fudge", "Bread", "Frostings", "Main Dishes")
        )

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
            repository.ensureDatabaseSeeded()
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

    fun syncOta(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.syncOta()
            onResult(result.first, result.second)
        }
    }
}
