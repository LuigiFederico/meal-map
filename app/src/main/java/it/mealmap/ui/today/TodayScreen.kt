package it.mealmap.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.ui.components.WaveHeader

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        WaveHeader(
            title = stringResource(R.string.tab_today),
            subtitle = stringResource(R.string.today_subtitle),
        )
    }
}
