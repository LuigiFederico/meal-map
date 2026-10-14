package it.mealmap.ui.week

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.theme.MealMapTheme
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w400dp-h1200dp")
class WeekContentTest {
    @get:Rule
    val compose = createComposeRule()

    private var openedMeal: MealSlot? = null
    private val swaps = mutableListOf<Swap>()

    private fun show(
        state: WeekUiState = WeekUiState(loading = false, week = sampleWeek),
        overLimit: List<ProteinCategory> = emptyList(),
        swapSource: MealSlot? = null,
        onOpenBalance: () -> Unit = {},
    ) = compose.setContent {
        MealMapTheme {
            WeekContent(
                state = state,
                overLimit = overLimit,
                today = DayOfWeek.WEDNESDAY,
                swapSource = swapSource,
                onCancelSwap = {},
                onOpenBalance = onOpenBalance,
                onOpenMeal = { openedMeal = it },
                onNewWeek = {},
                onSwap = { swaps += it },
            )
        }
    }

    private fun center(description: String): Offset =
        compose.onNodeWithContentDescription(description).fetchSemanticsNode().boundsInRoot.center

    @Test
    fun describesEveryMealWithProteinCarbAndVegetables() {
        show()

        compose.onNodeWithContentDescription(
            "Venerdì cena: Pesce, pasta, verdure, bloccato",
        ).assertExists()
        compose.onNodeWithContentDescription("Martedì pranzo: Mensa").assertExists()
        compose.onNodeWithContentDescription("Mercoledì (oggi)").assertExists()
    }

    @Test
    fun tappingAMealOpensItsDetail() {
        show()

        compose.onNodeWithContentDescription(
            "Sabato pranzo: Carne rossa, pasta, verdure",
        ).performClick()

        assertEquals(MealSlot(DayOfWeek.SATURDAY, Slot.LUNCH), openedMeal)
    }

    @Test
    fun inSwapModeTappingAMealOrADaySwaps() {
        val source = MealSlot(DayOfWeek.SATURDAY, Slot.LUNCH)
        show(swapSource = source)

        compose.onNodeWithText(
            "Tocca il pasto da scambiare con sabato pranzo",
            substring = true,
        ).assertExists()
        compose.onNodeWithContentDescription(
            "Giovedì cena: Carne bianca, pane, verdure",
        ).performClick()
        compose.onNodeWithContentDescription("Lunedì").performClick()

        assertEquals(
            listOf(
                Swap.Meals(source, MealSlot(DayOfWeek.THURSDAY, Slot.DINNER)),
                Swap.Days(DayOfWeek.SATURDAY, DayOfWeek.MONDAY),
            ),
            swaps,
        )
        assertEquals(null, openedMeal)
    }

    @Test
    fun dragAndDropSwapsMealsAndDays() {
        show()
        val saturdayLunch = center("Sabato pranzo: Carne rossa, pasta, verdure")
        val thursdayDinner = center("Giovedì cena: Carne bianca, pane, verdure")
        val monday = center("Lunedì")

        compose.onRoot().performTouchInput {
            down(saturdayLunch)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            moveTo(thursdayDinner)
            up()
        }
        compose.onRoot().performTouchInput {
            down(saturdayLunch)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            moveTo(monday)
            up()
        }

        assertEquals(
            listOf(
                Swap.Meals(
                    MealSlot(DayOfWeek.SATURDAY, Slot.LUNCH),
                    MealSlot(DayOfWeek.THURSDAY, Slot.DINNER),
                ),
                Swap.Days(DayOfWeek.SATURDAY, DayOfWeek.MONDAY),
            ),
            swaps,
        )
    }

    @Test
    fun releasingALongPressWithoutMovingDoesNothing() {
        show()
        val saturdayLunch = center("Sabato pranzo: Carne rossa, pasta, verdure")

        compose.onRoot().performTouchInput {
            down(saturdayLunch)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            up()
        }

        assertEquals(null, openedMeal)
        assertEquals(emptyList<Swap>(), swaps)
    }

    @Test
    fun overLimitChipOpensTheBalance() {
        var opened = false
        show(overLimit = listOf(ProteinCategory.RED_MEAT), onOpenBalance = { opened = true })

        compose.onNodeWithText("Carne rossa oltre il limite").performScrollTo().performClick()

        assertTrue(opened)
    }

    @Test
    fun showsAnEmptyStateBeforeTheFirstWeek() {
        show(WeekUiState(loading = false, week = null))

        compose.onNodeWithText("Nessuna settimana pianificata").assertExists()
    }
}
