package it.mealmap.ui.newweek

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.data.inMemoryDatabase
import it.mealmap.domain.BalanceStatus
import it.mealmap.domain.balance
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.Slot
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.MainDispatcherRule
import java.time.DayOfWeek
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewWeekViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val database = inMemoryDatabase()
    private val week = WeekRepository(database.weekDao())
    private val tuesdayLunch = MealSlot(DayOfWeek.TUESDAY, Slot.LUNCH)
    private val fridayDinner = MealSlot(DayOfWeek.FRIDAY, Slot.DINNER)
    private val mondayLunch = MealSlot(DayOfWeek.MONDAY, Slot.LUNCH)

    @After
    fun closeDatabase() = database.close()

    private suspend fun viewModel(): NewWeekViewModel {
        week.save(sampleWeek)
        return NewWeekViewModel(week, RulesRepository(database.rulesDao()), Random(1)).also { vm ->
            vm.state.first { !it.loading }
        }
    }

    @Test
    fun startsFromLockedMealsAndMealsWithoutCategory() = runTest {
        val plans = viewModel().state.value.plans

        assertEquals(SlotPlan.Keep(sampleWeek[fridayDinner] as Meal.Food), plans[fridayDinner])
        assertEquals(SlotPlan.Other(OtherKind.CANTEEN), plans[tuesdayLunch])
        assertEquals(SlotPlan.Generate, plans[mondayLunch])
        assertEquals(11, plans.values.count { it == SlotPlan.Generate })
    }

    @Test
    fun cyclesThroughTheOptionsOfASlot() = runTest {
        val vm = viewModel()

        val seen = (1..5).map {
            vm.cycle(fridayDinner)
            vm.state.value.plans.getValue(fridayDinner)
        }
        vm.cycle(mondayLunch)

        assertEquals(
            listOf(
                SlotPlan.Generate,
                SlotPlan.Other(OtherKind.EATING_OUT),
                SlotPlan.Other(OtherKind.CANTEEN),
                SlotPlan.Other(OtherKind.FREE_CHOICE),
                SlotPlan.Keep(sampleWeek[fridayDinner] as Meal.Food),
            ),
            seen,
        )
        assertEquals(SlotPlan.Other(OtherKind.EATING_OUT), vm.state.value.plans[mondayLunch])
    }

    @Test
    fun generatesAWeekThatKeepsTheConstraints() = runTest {
        val vm = viewModel()
        vm.cycle(mondayLunch)

        vm.generate()
        vm.state.first { it.generated }

        val generated = week.week.first()!!
        assertEquals(sampleWeek[fridayDinner], generated[fridayDinner])
        assertEquals(Meal.Other(OtherKind.CANTEEN), generated[tuesdayLunch])
        assertEquals(Meal.Other(OtherKind.EATING_OUT), generated[mondayLunch])
        val balance = generated.balance(FrequencyRule.DEFAULTS)
        assertTrue(balance.categories.all { it.status == BalanceStatus.OK })
    }
}
