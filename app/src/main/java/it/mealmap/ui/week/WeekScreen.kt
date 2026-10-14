package it.mealmap.ui.week

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.ui.components.WaveHeader

@Composable
fun WeekScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        WaveHeader(
            title = stringResource(R.string.week_title),
            subtitle = stringResource(R.string.week_subtitle),
        )
    }
}
