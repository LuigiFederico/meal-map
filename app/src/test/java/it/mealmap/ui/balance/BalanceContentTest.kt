package it.mealmap.ui.balance

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.balance
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.sampleWeek
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BalanceContentTest {
    @get:Rule
    val compose = createComposeRule()

    private var openedWeek = false

    private fun show() = compose.setContent {
        MealMapTheme {
            BalanceScreen(
                BalanceUiState(
                    loading = false,
                    balance = sampleWeek.balance(FrequencyRule.DEFAULTS),
                ),
                onOpenWeek = { openedWeek = true },
                onNewWeek = {},
            )
        }
    }

    @Test
    fun explainsWhichMealsGoOverTheLimit() {
        show()

        compose.onNodeWithText("1 categoria fuori limite").assertExists()
        compose.onNodeWithText(
            "Carne rossa: 2 pasti su un massimo di 1 (martedì cena, sabato pranzo).",
        )
            .assertExists()
        compose.onNodeWithText("Vedi nella settimana").performClick()
        assertTrue(openedWeek)
    }

    @Test
    fun listsEveryCategoryWithItsRuleAndStatus() {
        show()

        compose.onNodeWithContentDescription(
            "Carne rossa, al massimo 1: 2 / 1, sopra il limite",
        ).assertExists()
        compose.onNodeWithContentDescription("Uova, esattamente 2: 2, nei limiti").assertExists()
        compose.onNodeWithContentDescription("Carne bianca, 1–2: 1, nei limiti").assertExists()
        compose.onNodeWithContentDescription("Legumi, almeno 3: 3, nei limiti").performScrollTo()
        compose.onNodeWithText("I pasti senza categoria (2)", substring = true).assertExists()
    }
}
