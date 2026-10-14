package it.mealmap.ui.week

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.theme.MealMapTheme
import java.time.DayOfWeek
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeekContentTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(
        state: WeekUiState,
        overLimit: List<ProteinCategory> = emptyList(),
        onOpenBalance: () -> Unit = {},
    ) = compose.setContent {
        MealMapTheme {
            WeekContent(
                state,
                overLimit,
                today = DayOfWeek.WEDNESDAY,
                onOpenBalance = onOpenBalance,
            )
        }
    }

    @Test
    fun describesEveryMealWithProteinCarbAndVegetables() {
        show(WeekUiState(loading = false, week = sampleWeek))

        compose.onNodeWithContentDescription(
            "Venerdì cena: Pesce, pasta, verdure, bloccato",
        ).assertExists()
        compose.onNodeWithContentDescription("Martedì pranzo: Mensa").assertExists()
        compose.onNodeWithContentDescription("Mercoledì (oggi)").assertExists()
    }

    @Test
    fun overLimitChipOpensTheBalance() {
        var opened = false
        show(WeekUiState(loading = false, week = sampleWeek), listOf(ProteinCategory.RED_MEAT)) {
            opened = true
        }

        compose.onNodeWithText("Carne rossa oltre il limite").performClick()

        assertTrue(opened)
    }

    @Test
    fun showsAnEmptyStateBeforeTheFirstWeek() {
        show(WeekUiState(loading = false, week = null))

        compose.onNodeWithText("Nessuna settimana pianificata").assertExists()
    }
}
