package it.mealmap.ui.meal

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.data.inMemoryDatabase
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.MainDispatcherRule
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditMealViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val week = WeekRepository(database.weekDao())
    private val friday = MealSlot(DayOfWeek.FRIDAY, Slot.DINNER)

    @After
    fun closeDatabase() = database.close()

    private suspend fun viewModel(): EditMealViewModel {
        week.save(sampleWeek)
        return EditMealViewModel(friday, week, RulesRepository(database.rulesDao())).also { vm ->
            vm.state.first { it.selection != null }
        }
    }

    @Test
    fun keepsTheLockWhenOnlyTheCategoryChanges() = runTest {
        val vm = viewModel()

        vm.selectProtein(ProteinCategory.LEGUMES)
        vm.selectCarb(Carb.RICE)
        vm.save()
        vm.state.first { it.saved }

        assertEquals(
            Meal.Food(ProteinCategory.LEGUMES, Carb.RICE, locked = true),
            week.week.first()!![friday],
        )
    }

    @Test
    fun remembersTheCarbAcrossMealsWithoutCategory() = runTest {
        val vm = viewModel()

        vm.selectOther(OtherKind.CANTEEN)
        vm.selectProtein(ProteinCategory.EGGS)

        assertEquals(
            Meal.Food(ProteinCategory.EGGS, Carb.PASTA, locked = true),
            vm.state.first().selection,
        )
    }
}
