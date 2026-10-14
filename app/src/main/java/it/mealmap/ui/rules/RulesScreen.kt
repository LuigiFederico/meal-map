package it.mealmap.ui.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.ui.components.WaveHeader

@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        WaveHeader(
            title = stringResource(R.string.tab_rules),
            subtitle = stringResource(R.string.rules_subtitle),
        )
    }
}
