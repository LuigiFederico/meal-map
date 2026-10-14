package it.mealmap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.BuildConfig
import it.mealmap.R
import it.mealmap.domain.model.FoodGroup
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.WaveHeader
import it.mealmap.ui.rules.RulesContent
import it.mealmap.ui.rules.RulesViewModel

/** The rules of the plan, then the installed version and the update button at the bottom. */
@Composable
fun SettingsScreen(onOpenGroup: (FoodGroup) -> Unit, modifier: Modifier = Modifier) {
    val rulesViewModel = appViewModel { RulesViewModel(it.rulesRepository, it.foodRepository) }
    val rulesState by rulesViewModel.state.collectAsStateWithLifecycle()
    val updateViewModel = appViewModel { UpdateViewModel(it.appUpdater, BuildConfig.VERSION_CODE) }
    val updateStatus by updateViewModel.status.collectAsStateWithLifecycle()
    Column(modifier.verticalScroll(rememberScrollState())) {
        WaveHeader(
            title = stringResource(R.string.tab_settings),
            subtitle = stringResource(R.string.settings_subtitle),
        )
        RulesContent(
            rulesState,
            rulesViewModel::update,
            rulesViewModel::resetToDefaults,
            onOpenGroup,
        )
        UpdateSection(BuildConfig.VERSION_NAME, updateStatus, updateViewModel::checkForUpdates)
    }
}

@Composable
fun UpdateSection(
    version: String,
    status: UpdateStatus,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
    ) {
        Text(
            stringResource(R.string.update_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(20.dp),
                )
                .padding(14.dp),
        ) {
            Text(
                stringResource(R.string.update_version, version),
                style = MaterialTheme.typography.titleSmall,
            )
            val message = when (status) {
                UpdateStatus.Idle -> null
                UpdateStatus.Checking -> stringResource(R.string.update_checking)
                UpdateStatus.UpToDate -> stringResource(R.string.update_up_to_date)
                is UpdateStatus.Downloading ->
                    stringResource(R.string.update_downloading, status.version)
                UpdateStatus.Failed -> stringResource(R.string.update_failed)
            }
            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = onCheck,
                enabled = status != UpdateStatus.Checking && status !is UpdateStatus.Downloading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.update_check)) }
        }
    }
}
