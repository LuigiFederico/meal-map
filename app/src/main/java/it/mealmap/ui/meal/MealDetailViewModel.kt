package it.mealmap.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mealmap.data.FoodRepository
import it.mealmap.data.WeekRepository
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the detail sheet shows; [meal] is null while the sheet is closed or loading. */
data class MealDetailUiState(
    val slot: MealSlot? = null,
    val meal: Meal? = null,
    val proteinFoods: List<Food> = emptyList(),
    val carbFoods: List<Food> = emptyList(),
)

/** Backs the meal detail sheet of a screen: [show] a slot, then [hide] it. */
@OptIn(ExperimentalCoroutinesApi::class)
class MealDetailViewModel(
    private val weekRepository: WeekRepository,
    foodRepository: FoodRepository,
) : ViewModel() {
    private val slot = MutableStateFlow<MealSlot?>(null)

    val state: StateFlow<MealDetailUiState> = slot
        .flatMapLatest { slot ->
            if (slot == null) return@flatMapLatest flowOf(MealDetailUiState())
            val meal = weekRepository.week.map { it?.get(slot) }.distinctUntilChanged()
            // Alternatives only depend on protein and carb, not on the lock.
            val foods = meal
                .map { (it as? Meal.Food)?.let { food -> food.protein to food.carb } }
                .distinctUntilChanged()
                .flatMapLatest { key ->
                    val (protein, carb) = key
                        ?: return@flatMapLatest flowOf(emptyList<Food>() to emptyList())
                    combine(
                        foodRepository.forCategory(protein),
                        foodRepository.inGroup(FoodGroup.CARBS),
                    ) { proteinFoods, carbFoods ->
                        proteinFoods to
                            carbFoods.filter { carb in it.carbs }
                    }
                }
            combine(meal, foods) { current, (proteinFoods, carbFoods) ->
                MealDetailUiState(slot, current, proteinFoods, carbFoods)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealDetailUiState())

    fun show(slot: MealSlot) {
        this.slot.value = slot
    }

    fun hide() {
        slot.value = null
    }

    fun toggleLock() {
        val slot = slot.value ?: return
        viewModelScope.launch { weekRepository.update { it.toggleLock(slot) } }
    }
}
