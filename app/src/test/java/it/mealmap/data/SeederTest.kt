package it.mealmap.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun mapsMealSizeCarbPortionsToTheirCarbs() = runTest {
        val carbs = FoodRepository(database.foodDao()).inGroup(FoodGroup.CARBS).first()

        assertEquals(
            listOf(
                "Agnolotti",
                "Pasta all'uovo",
                "Riso / pasta / orzo / bulgur ecc.",
                "Tortellini",
                "Udon",
            ),
            carbs.filter { Carb.PASTA in it.carbs }.map { it.name },
        )
        assertEquals(
            listOf("Pane · invece della pasta"),
            carbs.filter { Carb.BREAD in it.carbs && it.name.startsWith("Pane") }.map { it.name },
        )
        assertTrue(Carb.entries.all { carb -> carbs.any { carb in it.carbs } })
    }

    @Test
    fun leavesTheWeekToBeGenerated() = runTest {
        assertNull(WeekRepository(database.weekDao()).week.first())
    }
}
