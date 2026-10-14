package it.mealmap.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeederTest {
    private val database = inMemoryDatabase()

    @After
    fun closeDatabase() = database.close()

    @Test
    fun seedsThePlanRulesAndFoods() = runTest {
        val foods = FoodRepository(database.foodDao())

        assertEquals(FrequencyRule.DEFAULTS, RulesRepository(database.rulesDao()).rules.first())
        assertEquals(
            mapOf(
                FoodGroup.LEGUMES to 26,
                FoodGroup.FISH to 40,
                FoodGroup.MEAT to 27,
                FoodGroup.EGGS to 2,
                FoodGroup.CHEESE to 30,
                FoodGroup.CARBS to 30,
                FoodGroup.BREAKFAST to 79,
                FoodGroup.SNACKS to 52,
            ),
            foods.groupCounts.first(),
        )
        assertEquals(
            listOf(7, 11, 9),
            listOf(ProteinCategory.WHITE_MEAT, ProteinCategory.RED_MEAT, ProteinCategory.CURED_MEAT)
                .map { foods.forCategory(it).first().size },
        )
    }

    @Test
    fun leavesTheWeekToBeGenerated() = runTest {
        assertNull(WeekRepository(database.weekDao()).week.first())
    }
}
