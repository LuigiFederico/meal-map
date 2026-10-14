package it.mealmap.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.FoodRepository
import it.mealmap.data.RulesRepository
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RulesUiState(
    val loading: Boolean = true,
    val rules: Map<ProteinCategory, FrequencyRule> = emptyMap(),
    val groupCounts: Map<FoodGroup, Int> = emptyMap(),
)

class RulesViewModel(
    private val rulesRepository: RulesRepository,
    foodRepository: FoodRepository,
) : ViewModel() {
    val state: StateFlow<RulesUiState> = combine(
        rulesRepository.rules,
        foodRepository.groupCounts,
    ) {
            rules,
            counts,
        ->
        RulesUiState(loading = false, rules = rules, groupCounts = counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RulesUiState())

    /** Applies a stepper [step] to the stored rule, not to the possibly stale one on screen. */
    fun update(category: ProteinCategory, step: (FrequencyRule) -> FrequencyRule?) {
        viewModelScope.launch { rulesRepository.update(category, step) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { rulesRepository.resetToDefaults() }
    }
}
