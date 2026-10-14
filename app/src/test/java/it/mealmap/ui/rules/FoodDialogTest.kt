package it.mealmap.ui.rules

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodDialogTest {
    @get:Rule
    val compose = createComposeRule()

    private var saved: Food? = null

    private fun show(group: FoodGroup, food: Food? = null) = compose.setContent {
        MealMapTheme { FoodDialog(group, food, onSave = { saved = it }, onDismiss = {}) }
    }

    @Test
    fun addsAMeatWithItsCategory() {
        show(FoodGroup.MEAT)

        compose.onNodeWithText("Salva").assertIsNotEnabled()
        compose.onNodeWithText("Nome").performTextInput("Tacchino arrosto")
        compose.onNodeWithText("Porzione").performTextInput("100 g")
        compose.onNodeWithText("Affettati").performClick()
        compose.onNodeWithText("Salva").performClick()

        assertEquals(
            Food(
                group = FoodGroup.MEAT,
                category = ProteinCategory.CURED_MEAT,
                name = "Tacchino arrosto",
                portion = "100 g",
            ),
            saved,
        )
    }

    @Test
    fun editsTheCarbsACarbPortionReplaces() {
        val pasta = Food(7, FoodGroup.CARBS, null, "Pasta integrale", "80 g", setOf(Carb.PASTA))
        show(FoodGroup.CARBS, pasta)

        compose.onNodeWithText("Modifica alimento").assertExists()
        compose.onNodeWithText("Riso").performClick()
        compose.onNodeWithText("Salva").performClick()

        assertEquals(pasta.copy(carbs = setOf(Carb.PASTA, Carb.RICE)), saved)
    }
}
