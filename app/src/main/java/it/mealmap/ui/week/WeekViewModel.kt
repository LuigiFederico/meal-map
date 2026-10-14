package it.mealmap.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.WeekRepository
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Week
import java.time.DayOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeekUiState(
    val loading: Boolean = true,
    /** Null when no week has been generated yet. */
    val week: Week? = null,
)

/** A swap of two meals or two whole days; doing it again undoes it. */
sealed interface Swap {
    data class Meals(val a: MealSlot, val b: MealSlot) : Swap

    data class Days(val a: DayOfWeek, val b: DayOfWeek) : Swap
}

class WeekViewModel(private val weekRepository: WeekRepository) : ViewModel() {
    val state: StateFlow<WeekUiState> = weekRepository.week
        .map { WeekUiState(loading = false, week = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekUiState())

    fun swap(swap: Swap) {
        viewModelScope.launch {
            weekRepository.update { week ->
                when (swap) {
                    is Swap.Meals -> week.swapMeals(swap.a, swap.b)
                    is Swap.Days -> week.swapDays(swap.a, swap.b)
                }
            }
        }
    }
}
