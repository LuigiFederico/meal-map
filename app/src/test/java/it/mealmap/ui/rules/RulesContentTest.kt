package it.mealmap.ui.rules

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val changes = mutableListOf<Pair<ProteinCategory, FrequencyRule?>>()
    private var reset = false
    private var opened: FoodGroup? = null

    private fun show() = compose.setContent {
        MealMapTheme {
            // In the app the section scrolls with the settings screen.
            RulesContent(
                state = RulesUiState(
                    loading = false,
                    rules = FrequencyRule.DEFAULTS,
                    groupCounts = mapOf(FoodGroup.LEGUMES to 26),
                ),
                onRuleChange = { category, step ->
                    changes +=
                        category to step(FrequencyRule.DEFAULTS.getValue(category))
                },
                onReset = { reset = true },
                onOpenGroup = { opened = it },
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        }
    }

    @Test
    fun editsTheLimitsOfACategory() {
        show()

        compose.onNodeWithText("esattamente 2").assertExists()
        compose.onNodeWithContentDescription(
            "Aumenta minimo Uova",
        ).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Aumenta massimo Uova").performClick()
        compose.onNodeWithContentDescription(
            "Diminuisci minimo Legumi",
        ).performScrollTo().performClick()
        compose.onNodeWithText("Ripristina i valori predefiniti").performScrollTo().performClick()
        compose.onNodeWithText("Ripristina").performClick()

        assertEquals(
            listOf(
                ProteinCategory.EGGS to FrequencyRule(min = 2, max = 3),
                ProteinCategory.LEGUMES to FrequencyRule(min = 2),
            ),
            changes,
        )
        assertTrue(reset)
    }

    @Test
    fun opensTheAlternativesOfAGroup() {
        show()

        compose.onNodeWithText("Alternative").performClick()
        compose.onNodeWithText("Carne e affettati").assertExists()
        compose.onNodeWithText("Legumi").performClick()

        assertEquals(FoodGroup.LEGUMES, opened)
    }
}
