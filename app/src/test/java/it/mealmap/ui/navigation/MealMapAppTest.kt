package it.mealmap.ui.navigation

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealMapAppTest {
    @get:Rule
    val compose = createComposeRule()

    private fun tab(label: String) = compose.onNode(hasText(label) and isSelectable())

    @Test
    fun startsOnTodayAndSwitchesTabs() {
        compose.setContent { MealMapTheme { MealMapApp() } }

        tab("Oggi").assertIsSelected()

        tab("Bilancio").performClick()
        tab("Bilancio").assertIsSelected()
        compose.onNodeWithText("Settimana tipo pianificata").assertExists()

        tab("Settimana").performClick()
        tab("Settimana").assertIsSelected()
        compose.onNodeWithText("Lunedì – domenica · senza date").assertExists()

        tab("Regole").performClick()
        tab("Regole").assertIsSelected()
        compose.onNodeWithText("Dal piano della nutrizionista · nov 2024").assertExists()

        tab("Oggi").performClick()
        tab("Oggi").assertIsSelected()
        compose.onNodeWithText("Oggi · settimana tipo").assertExists()
    }
}
