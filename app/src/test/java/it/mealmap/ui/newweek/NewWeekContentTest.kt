package it.mealmap.ui.newweek

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.ui.theme.MealMapTheme
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewWeekContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val friday = MealSlot(DayOfWeek.FRIDAY, Slot.DINNER)
    private val tuesday = MealSlot(DayOfWeek.TUESDAY, Slot.LUNCH)
    private val plans =
        MealSlot.ALL.associateWith<MealSlot, SlotPlan> { SlotPlan.Generate } + mapOf(
            friday to SlotPlan.Keep(Meal.Food(ProteinCategory.FISH, Carb.PASTA, locked = true)),
            tuesday to SlotPlan.Other(OtherKind.CANTEEN),
        )
    private val cycled = mutableListOf<MealSlot>()
    private var generated = false

    private fun show() = compose.setContent {
        MealMapTheme {
            NewWeekContent(
                NewWeekUiState(loading = false, plans = plans),
                onCycle = { cycled += it },
                onGenerate = { generated = true },
                onClose = {},
            )
        }
    }

    @Test
    fun summarizesWhatWillBeGenerated() {
        show()

        compose.onNodeWithText("12 pasti da generare · 1 fisso · 1 senza categoria").assertExists()
        compose.onNodeWithContentDescription("Venerdì cena: Fisso · Pesce").assertExists()
        compose.onNodeWithContentDescription("Martedì pranzo: Mensa").assertExists()
    }

    @Test
    fun tappingASlotCyclesItAndTheButtonGenerates() {
        show()

        compose.onNodeWithContentDescription(
            "Lunedì pranzo: Genera",
        ).performScrollTo().performClick()
        compose.onNodeWithText("Genera settimana").performClick()

        assertEquals(listOf(MealSlot(DayOfWeek.MONDAY, Slot.LUNCH)), cycled)
        assertTrue(generated)
    }
}
