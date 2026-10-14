package it.mealmap.ui.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.domain.Balance
import it.mealmap.domain.balance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BalanceUiState(
    val loading: Boolean = true,
    /** Null when no week has been generated yet. */
    val balance: Balance? = null,
)

class BalanceViewModel(weekRepository: WeekRepository, rulesRepository: RulesRepository) :
    ViewModel() {
    val state: StateFlow<BalanceUiState> = combine(weekRepository.week, rulesRepository.rules) {
            week,
            rules,
        ->
        BalanceUiState(loading = false, balance = week?.balance(rules))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BalanceUiState())
}
