package it.mealmap.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.WeekRepository
import it.mealmap.domain.model.Week
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WeekUiState(
    val loading: Boolean = true,
    /** Null when no week has been generated yet. */
    val week: Week? = null,
)

class WeekViewModel(weekRepository: WeekRepository) : ViewModel() {
    val state: StateFlow<WeekUiState> = weekRepository.week
        .map { WeekUiState(loading = false, week = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekUiState())
}
