package it.mealmap.ui.navigation

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

        tab("Impostazioni").performClick()
        tab("Impostazioni").assertIsSelected()
        compose.onNodeWithText("Piano della nutrizionista e piramide alimentare").assertExists()
        compose.onNodeWithText("Controlla aggiornamenti").performScrollTo().assertExists()

        tab("Oggi").performClick()
        tab("Oggi").assertIsSelected()
        compose.onNodeWithText("Oggi · settimana tipo").assertExists()
    }

    @Test
    fun generatesTheFirstWeekFromTheEmptyState() {
        compose.setContent { MealMapTheme { MealMapApp() } }

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(
                "Nessuna settimana pianificata",
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Nuova settimana").performScrollTo().performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(
                "14 pasti da generare",
                substring = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Genera settimana").performClick()

        compose.waitUntil(5_000) {
            compose.onAllNodes(
                hasContentDescription("Pranzo: ", substring = true),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        tab("Oggi").assertIsSelected()
    }

    @Test
    fun browsesTheSeededAlternatives() {
        compose.setContent { MealMapTheme { MealMapApp() } }

        tab("Impostazioni").performClick()
        compose.onNodeWithText("Alternative").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("26").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Legumi").performClick()

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText(
                "26 alternative · porzione per un pasto",
            ).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Indietro").performClick()
        compose.onNodeWithText("Carne e affettati").assertExists()
    }
}
