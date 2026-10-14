package it.mealmap.domain

import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory.FISH
import it.mealmap.domain.model.Slot.DINNER
import it.mealmap.domain.model.Slot.LUNCH
import it.mealmap.domain.model.Week
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WeekTest {
    @Test
    fun rejectsIncompleteWeek() {
        assertThrows(IllegalArgumentException::class.java) {
            Week(mapOf(slot(MONDAY, LUNCH) to food(FISH)))
        }
    }

    @Test
    fun swapMealsMovesLockWithTheMeal() {
        val swapped = sampleWeek.swapMeals(slot(FRIDAY, DINNER), slot(SATURDAY, LUNCH))

        assertEquals(sampleWeek[slot(FRIDAY, DINNER)], swapped[slot(SATURDAY, LUNCH)])
        assertEquals(sampleWeek[slot(SATURDAY, LUNCH)], swapped[slot(FRIDAY, DINNER)])
        assertEquals(true, (swapped[slot(SATURDAY, LUNCH)] as Meal.Food).locked)
    }

    @Test
    fun swapDaysSwapsLunchAndDinner() {
        val swapped = sampleWeek.swapDays(TUESDAY, THURSDAY)

        for (s in listOf(LUNCH, DINNER)) {
            assertEquals(sampleWeek[slot(TUESDAY, s)], swapped[slot(THURSDAY, s)])
            assertEquals(sampleWeek[slot(THURSDAY, s)], swapped[slot(TUESDAY, s)])
        }
    }

    @Test
    fun swapsDoNotChangeTheBalance() {
        val rules = FrequencyRule.DEFAULTS
        val swapped = sampleWeek.swapMeals(
            slot(MONDAY, LUNCH),
            slot(SATURDAY, DINNER),
        ).swapDays(MONDAY, FRIDAY)

        assertEquals(
            sampleWeek.balance(rules).categories.map { it.count },
            swapped.balance(rules).categories.map { it.count },
        )
    }

    @Test
    fun toggleLockOnlyAffectsFoodMeals() {
        val other = slot(TUESDAY, LUNCH)
        assertEquals(sampleWeek, sampleWeek.toggleLock(other))

        val locked = sampleWeek.toggleLock(slot(TUESDAY, DINNER))
        assertEquals(true, (locked[slot(TUESDAY, DINNER)] as Meal.Food).locked)
        assertEquals(sampleWeek, locked.toggleLock(slot(TUESDAY, DINNER)))
    }

    @Test
    fun replaceSetsAnyMeal() {
        val replaced = sampleWeek.replace(slot(SATURDAY, LUNCH), Meal.Other(OtherKind.EATING_OUT))

        assertEquals(Meal.Other(OtherKind.EATING_OUT), replaced[slot(SATURDAY, LUNCH)])
        assertEquals(MealSlot.ALL.size - 1, MealSlot.ALL.count { replaced[it] == sampleWeek[it] })
    }
}
