package it.mealmap.domain.model

import java.time.DayOfWeek

/** A dateless template week: one meal for each of the 14 slots. */
data class Week(private val meals: Map<MealSlot, Meal>) {
    init {
        require(meals.keys == MealSlot.ALL.toSet()) { "a week needs exactly the 14 meal slots" }
    }

    operator fun get(slot: MealSlot): Meal = meals.getValue(slot)

    fun replace(slot: MealSlot, meal: Meal): Week = Week(meals + (slot to meal))

    /** Swaps two meals; a lock travels with its meal. */
    fun swapMeals(a: MealSlot, b: MealSlot): Week = Week(meals + mapOf(a to this[b], b to this[a]))

    fun swapDays(a: DayOfWeek, b: DayOfWeek): Week = Slot.entries.fold(this) { week, slot ->
        week.swapMeals(MealSlot(a, slot), MealSlot(b, slot))
    }

    /** Toggles the lock of a food meal; other meals cannot be locked. */
    fun toggleLock(slot: MealSlot): Week {
        val meal = this[slot] as? Meal.Food ?: return this
        return replace(slot, meal.copy(locked = !meal.locked))
    }
}
