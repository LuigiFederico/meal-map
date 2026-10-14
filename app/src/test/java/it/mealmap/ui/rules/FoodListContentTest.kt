package it.mealmap.ui.rules

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodListContentTest {
    @get:Rule
    val compose = createComposeRule()

    private fun legume(id: Long, name: String) =
        Food(id, FoodGroup.LEGUMES, ProteinCategory.LEGUMES, name, "60 g")

    private val foods =
        listOf(legume(1, "Ceci secchi"), legume(2, "Lenticchie secche"), legume(3, "Tofu"))
    private val deleted = mutableListOf<Food>()
    private val edited = mutableListOf<Food>()

    private fun show() = compose.setContent {
        MealMapTheme {
            FoodListContent(
                group = FoodGroup.LEGUMES,
                state = FoodListUiState(loading = false, foods = foods),
                onBack = {},
                onEdit = { edited += it },
                onDelete = { deleted += it },
            )
        }
    }

    @Test
    fun searchesByName() {
        show()

        compose.onNodeWithText("3 alternative · porzione per un pasto").assertExists()
        compose.onNodeWithText("Cerca un alimento").performTextInput("secch")

        compose.onNodeWithText("Ceci secchi").assertExists()
        compose.onNodeWithText("Tofu").assertDoesNotExist()
    }

    @Test
    fun editsAndDeletesFoods() {
        show()

        compose.onNodeWithContentDescription("Modifica Tofu").performClick()
        compose.onNodeWithContentDescription("Rimuovi Ceci secchi").performClick()

        assertEquals(listOf(foods[2]), edited)
        assertEquals(listOf(foods[0]), deleted)
    }
}
