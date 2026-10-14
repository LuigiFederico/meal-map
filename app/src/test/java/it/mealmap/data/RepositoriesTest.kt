package it.mealmap.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.sampleWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoriesTest {
    private val database = inMemoryDatabase(seeded = false)

    @After
    fun closeDatabase() = database.close()

    @Test
    fun weekRoundTripsThroughTheDatabase() = runTest {
        val repository = WeekRepository(database.weekDao())
        assertNull(repository.week.first())

        repository.save(sampleWeek)

        assertEquals(sampleWeek, repository.week.first())
    }

    @Test
    fun rulesCanBeUpdatedAndReset() = runTest {
        val repository = RulesRepository(database.rulesDao())
        repository.resetToDefaults()

        repository.update(ProteinCategory.CHEESE) { FrequencyRule(min = 1, max = 3) }
        assertEquals(
            FrequencyRule(min = 1, max = 3),
            repository.rules.first()[ProteinCategory.CHEESE],
        )

        repository.resetToDefaults()
        assertEquals(FrequencyRule.DEFAULTS, repository.rules.first())
    }

    @Test
    fun foodsCanBeAddedEditedDeletedAndRestored() = runTest {
        val repository = FoodRepository(database.foodDao())
        val tofu =
            Food(
                group = FoodGroup.LEGUMES,
                category = ProteinCategory.LEGUMES,
                name = "Tofu",
                portion = "150 g",
            )

        repository.save(tofu)
        val saved = repository.inGroup(FoodGroup.LEGUMES).first().single()
        repository.save(saved.copy(portion = "120 g"))
        assertEquals(
            listOf(saved.copy(portion = "120 g")),
            repository.forCategory(ProteinCategory.LEGUMES).first(),
        )

        repository.delete(saved)
        assertEquals(emptyMap<FoodGroup, Int>(), repository.groupCounts.first())

        repository.save(saved)
        assertEquals(listOf(saved), repository.inGroup(FoodGroup.LEGUMES).first())
    }
}
