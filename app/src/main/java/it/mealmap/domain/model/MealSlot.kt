package it.mealmap.domain.model

import java.time.DayOfWeek

enum class Slot {
    LUNCH,
    DINNER,
}

/** One of the 14 lunches and dinners of the week. */
data class MealSlot(val day: DayOfWeek, val slot: Slot) {
    companion object {
        /** Monday lunch to Sunday dinner, in eating order. */
        val ALL: List<MealSlot> = DayOfWeek.entries.flatMap { day ->
            Slot.entries.map { slot -> MealSlot(day, slot) }
        }
    }
}
