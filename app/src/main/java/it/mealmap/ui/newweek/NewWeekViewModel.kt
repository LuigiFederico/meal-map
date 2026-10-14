package it.mealmap.ui.newweek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.domain.generateWeek
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What the new week does with one slot. */
sealed interface SlotPlan {
    /** The app picks protein and carb. */
    data object Generate : SlotPlan

    /** A meal locked in the current week, kept as it is. */
    data class Keep(val meal: Meal.Food) : SlotPlan

    data class Other(val kind: OtherKind) : SlotPlan
}

/** The order in which a slot cycles through meals without category, as in the design. */
internal val OtherKindsInOrder =
    listOf(OtherKind.EATING_OUT, OtherKind.CANTEEN, OtherKind.FREE_CHOICE)

data class NewWeekUiState(
    val loading: Boolean = true,
    val plans: Map<MealSlot, SlotPlan> = emptyMap(),
    val generating: Boolean = false,
    val generated: Boolean = false,
)

/** The constraints step before generating a new week, which then replaces the current one. */
class NewWeekViewModel(
    private val weekRepository: WeekRepository,
    private val rulesRepository: RulesRepository,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val _state = MutableStateFlow(NewWeekUiState())
    val state: StateFlow<NewWeekUiState> = _state.asStateFlow()

    /** Meals locked in the current week: only these can be kept. */
    private var locked: Map<MealSlot, Meal.Food> = emptyMap()

    init {
        viewModelScope.launch {
            val week = weekRepository.week.first()
            // Locked meals stay fixed and meals without category are likely to repeat.
            val plans = MealSlot.ALL.associateWith { slot ->
                when (val meal = week?.get(slot)) {
                    is Meal.Food -> if (meal.locked) SlotPlan.Keep(meal) else SlotPlan.Generate
                    is Meal.Other -> SlotPlan.Other(meal.kind)
                    null -> SlotPlan.Generate
                }
            }
            locked =
                plans.mapNotNull { (slot, plan) ->
                    (plan as? SlotPlan.Keep)?.let { slot to it.meal }
                }.toMap()
            _state.update { it.copy(loading = false, plans = plans) }
        }
    }

    /** Moves a slot to the next option: (Fisso →) Genera → Fuori casa → Mensa → A scelta. */
    fun cycle(slot: MealSlot) {
        val options = listOfNotNull(locked[slot]?.let { SlotPlan.Keep(it) }, SlotPlan.Generate) +
            OtherKindsInOrder.map { SlotPlan.Other(it) }
        _state.update { state ->
            val current = state.plans.getValue(slot)
            val next = options[(options.indexOf(current) + 1) % options.size]
            state.copy(plans = state.plans + (slot to next))
        }
    }

    fun generate() {
        val state = _state.value
        if (state.loading || state.generating || state.generated) return
        val plans = state.plans
        _state.update { it.copy(generating = true) }
        viewModelScope.launch {
            val rules = rulesRepository.rules.first()
            val fixed = plans.mapNotNull { (slot, plan) ->
                when (plan) {
                    SlotPlan.Generate -> null
                    is SlotPlan.Keep -> slot to plan.meal
                    is SlotPlan.Other -> slot to Meal.Other(plan.kind)
                }
            }.toMap()
            val week = withContext(Dispatchers.Default) { generateWeek(rules, fixed, random) }
            weekRepository.save(week)
            _state.update { it.copy(generating = false, generated = true) }
        }
    }
}
