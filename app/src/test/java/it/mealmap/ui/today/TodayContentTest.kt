package it.mealmap.ui.today

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.data.ReminderMoment
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Slot
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.theme.MealMapTheme
import it.mealmap.ui.week.WeekUiState
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayContentTest {
    @get:Rule
    val compose = createComposeRule()

    private var openedMeal: MealSlot? = null
    private var openedReminder: ReminderMoment? = null
    private var newWeekRequested = false

    private fun show(state: WeekUiState = WeekUiState(loading = false, week = sampleWeek)) =
        compose.setContent {
            MealMapTheme {
                TodayContent(
                    state = state,
                    today = DayOfWeek.WEDNESDAY,
                    onOpenMeal = { openedMeal = it },
                    onOpenReminder = { openedReminder = it },
                    onNewWeek = { newWeekRequested = true },
                )
            }
        }

    @Test
    fun opensOnTodayWithItsMeals() {
        show()

        compose.onNodeWithText("Mercoledì").assertExists()
        compose.onNodeWithText("Oggi · settimana tipo").assertExists()
        compose.onNodeWithContentDescription("Mercoledì (oggi)").assertIsSelected()
        compose.onNodeWithContentDescription("Pranzo: Affettati, piadina, verdure").assertExists()
        compose.onNodeWithContentDescription(
            "Cena: Legumi, pasta, verdure",
        ).performScrollTo().performClick()

        assertEquals(MealSlot(DayOfWeek.WEDNESDAY, Slot.DINNER), openedMeal)
    }

    @Test
    fun browsesOtherDaysAndComesBack() {
        show()

        compose.onNodeWithContentDescription("Martedì").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Pranzo: Mensa").assertExists()
        compose.onNodeWithText("Settimana tipo").assertExists()

        compose.onNodeWithText("Torna a oggi").performScrollTo().performClick()
        compose.onNodeWithText("Mercoledì").assertExists()
    }

    @Test
    fun remindersOpenTheirMoment() {
        show()

        compose.onNodeWithText("Merenda").performScrollTo().performClick()

        assertEquals(ReminderMoment.AFTERNOON_SNACK, openedReminder)
    }

    @Test
    fun showsRemindersEvenWithoutAWeek() {
        show(WeekUiState(loading = false, week = null))

        compose.onNodeWithText("Colazione").assertExists()
        compose.onNodeWithText("Nessuna settimana pianificata").assertExists()
        compose.onNodeWithText("Nuova settimana").performScrollTo().performClick()
        assertTrue(newWeekRequested)
    }
}
