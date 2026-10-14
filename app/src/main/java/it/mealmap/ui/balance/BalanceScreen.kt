package it.mealmap.ui.balance

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.ui.components.WaveHeader

@Composable
fun BalanceScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        WaveHeader(
            title = stringResource(R.string.tab_balance),
            subtitle = stringResource(R.string.balance_subtitle),
        )
    }
}
