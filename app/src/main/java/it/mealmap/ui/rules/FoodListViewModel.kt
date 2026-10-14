package it.mealmap.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.FoodRepository
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodListUiState(val loading: Boolean = true, val foods: List<Food> = emptyList())

/** The editable list of alternatives of one [FoodGroup]. */
class FoodListViewModel(group: FoodGroup, private val foodRepository: FoodRepository) :
    ViewModel() {
    val state: StateFlow<FoodListUiState> = foodRepository.inGroup(group)
        .map { FoodListUiState(loading = false, foods = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodListUiState())

    /** Adds, edits or restores a deleted food (same id). */
    fun save(food: Food) {
        viewModelScope.launch { foodRepository.save(food) }
    }

    fun delete(food: Food) {
        viewModelScope.launch { foodRepository.delete(food) }
    }
}
