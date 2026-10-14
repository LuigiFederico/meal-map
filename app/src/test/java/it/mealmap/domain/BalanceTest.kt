package it.mealmap.domain

import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot.DINNER
import it.mealmap.domain.model.Slot.LUNCH
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BalanceTest {
    private val balance = sampleWeek.balance(FrequencyRule.DEFAULTS)

    private fun Balance.of(category: ProteinCategory) = categories.single {
        it.category == category
    }

    @Test
    fun countsOnlyFoodMeals() {
        assertEquals(
            listOf(3, 2, 2, 1, 2, 1, 1),
            balance.categories.map { it.count },
        )
        assertEquals(2, balance.uncountedMeals)
    }

    @Test
    fun flagsCategoriesOverTheirMax() {
        val redMeat = balance.of(ProteinCategory.RED_MEAT)

        assertEquals(BalanceStatus.OVER, redMeat.status)
        assertEquals(listOf(slot(TUESDAY, DINNER), slot(SATURDAY, LUNCH)), redMeat.slots)
        assertEquals(listOf(redMeat), balance.overLimit)
    }

    @Test
    fun flagsCategoriesUnderTheirMin() {
        val fewerLegumes = sampleWeek
            .replace(slot(WEDNESDAY, DINNER), food(ProteinCategory.FISH))
            .replace(slot(TUESDAY, DINNER), food(ProteinCategory.FISH))
            .balance(FrequencyRule.DEFAULTS)

        assertEquals(BalanceStatus.UNDER, fewerLegumes.of(ProteinCategory.LEGUMES).status)
        assertEquals(BalanceStatus.OK, fewerLegumes.of(ProteinCategory.RED_MEAT).status)
    }

    @Test
    fun missingRuleMeansNoBounds() {
        val unbounded = sampleWeek.balance(emptyMap())

        assertEquals(true, unbounded.categories.all { it.status == BalanceStatus.OK })
    }

    @Test
    fun ruleRejectsMinAboveMax() {
        assertThrows(IllegalArgumentException::class.java) { FrequencyRule(min = 3, max = 2) }
    }
}
