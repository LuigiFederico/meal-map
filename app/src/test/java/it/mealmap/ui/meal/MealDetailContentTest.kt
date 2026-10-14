package it.mealmap.ui.meal

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
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
class MealDetailContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val slot = MealSlot(DayOfWeek.WEDNESDAY, Slot.DINNER)
    private val legumes = (1..8).map {
        Food(
            group = FoodGroup.LEGUMES,
            category = ProteinCategory.LEGUMES,
            name = "Legume $it",
            portion = "${it}0 g",
        )
    }
    private var lockToggles = 0

    private fun show(meal: Meal) = compose.setContent {
        MealMapTheme {
            MealDetailContent(
                slot = slot,
                state = MealDetailUiState(
                    slot,
                    meal,
                    proteinFoods = legumes,
                    carbFoods = emptyList(),
                ),
                onToggleLock = { lockToggles++ },
                onClose = {},
            )
        }
    }

    @Test
    fun showsTheMealAndItsAlternatives() {
        show(Meal.Food(ProteinCategory.LEGUMES, Carb.PASTA))

        compose.onNodeWithText("Mercoledì · Cena").assertExists()
        compose.onNodeWithText("Legumi, verdure, pasta").assertExists()
        compose.onNodeWithText("Legume 6").assertExists()
        compose.onNodeWithText("Legume 7").assertDoesNotExist()

        compose.onNodeWithText("Mostra tutte le 8 alternative").performScrollTo().performClick()
        compose.onNodeWithText("Legume 8").assertExists()
    }

    @Test
    fun locksTheMeal() {
        show(Meal.Food(ProteinCategory.LEGUMES, Carb.PASTA))

        compose.onNodeWithText("Blocca").assertIsOff().performScrollTo().performClick()

        assertEquals(1, lockToggles)
    }

    @Test
    fun showsLockedState() {
        show(Meal.Food(ProteinCategory.LEGUMES, Carb.PASTA, locked = true))

        compose.onNodeWithText("Bloccato").assertIsOn()
    }

    @Test
    fun remindsTheHealthyPlateForMealsWithoutCategory() {
        show(Meal.Other(OtherKind.CANTEEN))

        compose.onNodeWithText("Mensa").assertExists()
        compose.onNodeWithText("Ricorda il piatto sano", substring = true).assertExists()
    }
}
