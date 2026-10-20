package it.mealmap.domain

import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot.DINNER
import it.mealmap.domain.model.Slot.LUNCH
import it.mealmap.domain.model.Week
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekGeneratorTest {
    private val rules = FrequencyRule.DEFAULTS
    private val seeds = 0 until 1000

    private fun generate(fixed: Map<MealSlot, Meal> = emptyMap(), seed: Int) =
        generateWeek(rules, fixed, Random(seed))

    private fun Week.proteins(day: DayOfWeek) =
        listOf(LUNCH, DINNER).mapNotNull { (this[MealSlot(day, it)] as? Meal.Food)?.protein }

    /** Proteins eaten on a day and again the next one, Sunday to Monday included. */
    private fun Week.consecutiveDayRepeats() = DayOfWeek.entries.sumOf { day ->
        proteins(day).distinct().count { it in proteins(day.plus(1)) }
    }

    @Test
    fun respectsEveryLimit() {
        for (seed in seeds) {
            val balance = generate(seed = seed).balance(rules)
            assertTrue(
                "seed $seed: $balance",
                balance.categories.all {
                    it.status ==
                        BalanceStatus.OK
                },
            )
        }
    }

    @Test
    fun neverRepeatsAProteinWithinADay() {
        for (seed in seeds) {
            val week = generate(seed = seed)
            for (day in DayOfWeek.entries) {
                val proteins = week.proteins(day)
                assertEquals("seed $seed, $day", proteins.distinct(), proteins)
            }
        }
    }

    @Test
    fun spreadsCarbsAndNeverRepeatsThemWithinADay() {
        for (seed in seeds) {
            val week = generate(seed = seed)
            val carbs = MealSlot.ALL.map { (week[it] as Meal.Food).carb }
            assertTrue("seed $seed", Carb.entries.all { carb -> carbs.count { it == carb } <= 2 })
            carbs.chunked(2).forEach { (lunch, dinner) ->
                assertTrue("seed $seed", lunch != dinner)
            }
        }
    }

    @Test
    fun repeatsAProteinOnConsecutiveDaysAtMostOnce() {
        for (seed in seeds) {
            val week = generate(seed = seed)
            assertTrue("seed $seed: $week", week.consecutiveDayRepeats() <= 1)
        }
    }

    @Test
    fun repeatsAtMostOnceAroundFixedMeals() {
        val fixed = mapOf(
            slot(TUESDAY, LUNCH) to Meal.Other(OtherKind.CANTEEN),
            slot(FRIDAY, DINNER) to food(ProteinCategory.RED_MEAT, Carb.PASTA, locked = true),
        )
        for (seed in seeds) {
            val week = generate(fixed, seed)
            assertTrue("seed $seed: $week", week.consecutiveDayRepeats() <= 1)
        }
    }

    @Test
    fun repeatsAtMostOnceWhenSomeDaysHaveASingleFreeSlot() {
        // Categories that each fit on non-consecutive days compete for Monday's single slot.
        val fixed = mapOf(
            slot(MONDAY, DINNER) to Meal.Other(OtherKind.CANTEEN),
            slot(FRIDAY, DINNER) to food(ProteinCategory.FISH, locked = true),
            slot(SUNDAY, LUNCH) to Meal.Other(OtherKind.CANTEEN),
            slot(SUNDAY, DINNER) to Meal.Other(OtherKind.CANTEEN),
        )
        for (seed in seeds) {
            val week = generate(fixed, seed)
            assertTrue("seed $seed: $week", week.consecutiveDayRepeats() <= 1)
        }
    }

    @Test
    fun repeatsCausedByFixedMealsDoNotUseUpTheAllowedOne() {
        // Legumes from Monday to Friday already repeat four times.
        val fixed = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .associate { day -> slot(day, LUNCH) to food(ProteinCategory.LEGUMES, locked = true) }
        for (seed in seeds) {
            val week = generate(fixed, seed)
            assertTrue("seed $seed: $week", week.consecutiveDayRepeats() <= 5)
        }
    }

    @Test
    fun keepsFixedMealsAndCountsThemTowardsLimits() {
        val fixed = mapOf(
            slot(TUESDAY, LUNCH) to Meal.Other(OtherKind.CANTEEN),
            slot(FRIDAY, DINNER) to food(ProteinCategory.RED_MEAT, Carb.PASTA, locked = true),
        )
        for (seed in seeds) {
            val week = generate(fixed, seed)
            fixed.forEach { (slot, meal) -> assertEquals(meal, week[slot]) }
            val balance = week.balance(rules)
            assertTrue("seed $seed", balance.categories.all { it.status == BalanceStatus.OK })
            assertEquals(1, balance.uncountedMeals)
        }
    }

    @Test
    fun avoidsSameDayRepeatsAroundFixedMeals() {
        val fixed = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .associate { day -> slot(day, LUNCH) to food(ProteinCategory.LEGUMES, locked = true) }
        for (seed in seeds) {
            val week = generate(fixed, seed)
            for (day in DayOfWeek.entries) {
                val proteins = week.proteins(day)
                assertEquals("seed $seed, $day", proteins.distinct(), proteins)
            }
        }
    }

    @Test
    fun overshootsTheCategoryLeastOverItsMaxWhenEveryMaxIsReached() {
        val tightRules = ProteinCategory.entries.associateWith { FrequencyRule(max = 1) } +
            (ProteinCategory.RED_MEAT to FrequencyRule(max = 0))

        val balance = generateWeek(tightRules, emptyMap(), Random(3)).balance(tightRules)

        assertEquals(0, balance.categories.single { it.category == ProteinCategory.RED_MEAT }.count)
        assertEquals(14, balance.categories.sumOf { it.count })
    }

    @Test
    fun isDeterministicForTheSameSeed() {
        assertEquals(generate(seed = 42), generate(seed = 42))
    }

    @Test
    fun fillsWhatItCanWhenMinimumsCannotBeMet() {
        val fixed = MealSlot.ALL.take(10).associateWith { Meal.Other(OtherKind.EATING_OUT) }

        val balance = generate(fixed, seed = 1).balance(rules)

        assertEquals(4, balance.categories.sumOf { it.count })
        assertTrue(balance.categories.any { it.status == BalanceStatus.UNDER })
        assertTrue(balance.overLimit.isEmpty())
    }

    @Test
    fun generatedMealsAreUnlocked() {
        val week = generate(seed = 7)

        assertTrue(MealSlot.ALL.none { (week[it] as Meal.Food).locked })
    }
}
