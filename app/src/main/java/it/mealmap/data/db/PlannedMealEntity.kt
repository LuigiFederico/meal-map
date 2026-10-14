package it.mealmap.data.db

import androidx.room.Entity
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import java.time.DayOfWeek

/** One meal of the week: either a protein with a carb, or an [OtherKind]. */
@Entity(tableName = "planned_meal", primaryKeys = ["day", "slot"])
data class PlannedMealEntity(
    val day: DayOfWeek,
    val slot: Slot,
    val protein: ProteinCategory?,
    val carb: Carb?,
    val otherKind: OtherKind?,
    val locked: Boolean,
) {
    val mealSlot: MealSlot get() = MealSlot(day, slot)

    fun toMeal(): Meal = if (otherKind != null) {
        Meal.Other(otherKind)
    } else {
        Meal.Food(checkNotNull(protein), checkNotNull(carb), locked)
    }

    companion object {
        fun from(mealSlot: MealSlot, meal: Meal) = when (meal) {
            is Meal.Food -> PlannedMealEntity(
                mealSlot.day,
                mealSlot.slot,
                meal.protein,
                meal.carb,
                otherKind = null,
                meal.locked,
            )
            is Meal.Other -> PlannedMealEntity(
                mealSlot.day,
                mealSlot.slot,
                protein = null,
                carb = null,
                meal.kind,
                locked = false,
            )
        }
    }
}
