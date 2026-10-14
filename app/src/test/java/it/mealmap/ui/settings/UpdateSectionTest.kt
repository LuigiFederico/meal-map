package it.mealmap.ui.settings

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.mealmap.ui.theme.MealMapTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateSectionTest {
    @get:Rule
    val compose = createComposeRule()

    private var checks = 0

    private fun show(status: UpdateStatus) = compose.setContent {
        MealMapTheme { UpdateSection(version = "1.0.7", status = status, onCheck = { checks++ }) }
    }

    @Test
    fun showsTheInstalledVersionAndChecks() {
        show(UpdateStatus.UpToDate)

        compose.onNodeWithText("Versione installata: 1.0.7").assertExists()
        compose.onNodeWithText("Hai già l'ultima versione.").assertExists()
        compose.onNodeWithText("Controlla aggiornamenti").assertIsEnabled().performClick()

        assertEquals(1, checks)
    }

    @Test
    fun disablesTheButtonWhileDownloading() {
        show(UpdateStatus.Downloading("1.0.8"))

        compose.onNodeWithText("Scarico la versione 1.0.8…").assertExists()
        compose.onNodeWithText("Controlla aggiornamenti").assertIsNotEnabled()
    }
}
