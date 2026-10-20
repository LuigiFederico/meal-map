package it.mealmap.ui.balance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthyPlateContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsEverySection() {
        compose.setContent { MealMapTheme { HealthyPlateContent(onClose = {}) } }

        compose.onNodeWithContentDescription(
            "Piatto diviso in tre: metà verdura e frutta, un quarto cereali, un quarto proteine",
        ).assertIsDisplayed()
        listOf(
            "Il piatto sano",
            "Quante volte a settimana",
            "Organizzare la giornata",
            "In numeri",
        ).forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
    }
}
