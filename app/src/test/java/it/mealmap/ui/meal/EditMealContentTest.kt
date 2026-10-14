package it.mealmap.ui.meal

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.ui.theme.MealMapTheme
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditMealContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val current = Meal.Food(ProteinCategory.LEGUMES, Carb.PASTA)
    private val picked = mutableListOf<Any>()

    // The sample week without Saturday lunch.
    private val othersCount = mapOf(
        ProteinCategory.LEGUMES to 3,
        ProteinCategory.FISH to 2,
        ProteinCategory.EGGS to 2,
        ProteinCategory.WHITE_MEAT to 1,
        ProteinCategory.RED_MEAT to 1,
        ProteinCategory.CURED_MEAT to 1,
        ProteinCategory.CHEESE to 1,
    )

    private fun show(selection: Meal) = compose.setContent {
        MealMapTheme {
            EditMealContent(
                slot = MealSlot(DayOfWeek.SATURDAY, Slot.LUNCH),
                state = EditMealUiState(current, selection, othersCount, FrequencyRule.DEFAULTS),
                onSelectProtein = { picked += it },
                onSelectCarb = { picked += it },
                onSelectOther = { picked += it },
                onSave = {},
                onClose = {},
            )
        }
    }

    @Test
    fun previewsTheBalanceOfEveryCategory() {
        show(current)

        compose.onNodeWithText("Sabato · Pranzo").assertExists()
        compose.onNodeWithText("Ora: Legumi, verdure, pasta").assertExists()
        compose.onNodeWithText("attuale · 4 · min 3").assertExists()
        compose.onNodeWithText("diventa 2 · max 1").assertExists()
        compose.onNodeWithText("arriverebbe", substring = true).assertDoesNotExist()
    }

    @Test
    fun warnsButAllowsGoingOverALimit() {
        show(Meal.Food(ProteinCategory.RED_MEAT, Carb.PASTA))

        compose.onNodeWithText(
            "Carne rossa arriverebbe a 2 su un massimo di 1. Puoi salvare comunque: lo vedrai segnalato nel Bilancio.",
        ).assertExists()
        compose.onNodeWithText("Salva").performClick()
    }

    @Test
    fun picksProteinCarbOrNoCategory() {
        show(current)

        compose.onNodeWithText("Pesce").performScrollTo().performClick()
        compose.onNodeWithText("Riso").performScrollTo().performClick()
        compose.onNodeWithText("Mensa").performScrollTo().performClick()

        assertEquals(listOf(ProteinCategory.FISH, Carb.RICE, OtherKind.CANTEEN), picked)
    }

    @Test
    fun hidesCarbsForMealsWithoutCategory() {
        show(Meal.Other(OtherKind.EATING_OUT))

        compose.onNodeWithText("Fuori casa").performScrollTo().assertIsSelected()
        compose.onNodeWithText("Cereale").assertDoesNotExist()
    }
}
