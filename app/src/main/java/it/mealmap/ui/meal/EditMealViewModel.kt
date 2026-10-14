package it.mealmap.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.domain.balance
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditMealUiState(
    /** The meal as it is in the week; null while loading. */
    val current: Meal? = null,
    /** The meal being chosen; starts as [current]. */
    val selection: Meal? = null,
    /** Portions per category in the rest of the week, i.e. without this meal. */
    val othersCount: Map<ProteinCategory, Int> = emptyMap(),
    val rules: Map<ProteinCategory, FrequencyRule> = emptyMap(),
    val saved: Boolean = false,
)

/** Changes the category, carb or kind of one meal, previewing the effect on the balance. */
class EditMealViewModel(
    private val slot: MealSlot,
    private val weekRepository: WeekRepository,
    rulesRepository: RulesRepository,
) : ViewModel() {
    /** What the user picked; null until they pick something, meaning "the current meal". */
    private val picked = MutableStateFlow<Meal?>(null)
    private val saved = MutableStateFlow(false)

    /** The last carb picked, kept while a meal without category is selected. */
    private var carb: Carb? = null

    val state: StateFlow<EditMealUiState> = combine(
        weekRepository.week,
        rulesRepository.rules,
        picked,
        saved,
    ) { week, rules, picked, saved ->
        val current = week?.get(slot)
        val others = week?.replace(slot, Meal.Other(OtherKind.FREE_CHOICE))?.balance(rules)
        EditMealUiState(
            current = current,
            selection = picked ?: current,
            othersCount = others?.categories?.associate { it.category to it.count }.orEmpty(),
            rules = rules,
            saved = saved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditMealUiState())

    fun selectProtein(protein: ProteinCategory) {
        val current = state.value.current as? Meal.Food
        val carb = carb ?: current?.carb ?: Carb.PASTA
        // A food meal keeps its lock when only its category or carb changes.
        picked.value = Meal.Food(protein, carb, locked = current?.locked == true)
    }

    fun selectCarb(carb: Carb) {
        this.carb = carb
        val meal = state.value.selection as? Meal.Food ?: return
        picked.value = meal.copy(carb = carb)
    }

    fun selectOther(kind: OtherKind) {
        picked.value = Meal.Other(kind)
    }

    fun save() {
        val meal = state.value.selection ?: return
        viewModelScope.launch {
            weekRepository.update { it.replace(slot, meal) }
            saved.value = true
        }
    }
}
