package it.mealmap.ui.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.domain.Balance
import it.mealmap.domain.BalanceStatus
import it.mealmap.domain.CategoryBalance
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.EmptyWeek
import it.mealmap.ui.components.WaveHeader
import it.mealmap.ui.components.containerColor
import it.mealmap.ui.components.contentColor
import it.mealmap.ui.components.icon
import it.mealmap.ui.components.label
import it.mealmap.ui.components.ruleText
import it.mealmap.ui.components.slotPhrase
import it.mealmap.ui.theme.DashedOutline
import it.mealmap.ui.theme.Warning

@Composable
fun BalanceScreen(
    state: BalanceUiState,
    onOpenWeek: () -> Unit,
    onNewWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHealthyPlate by rememberSaveable { mutableStateOf(false) }
    Column(modifier.verticalScroll(rememberScrollState())) {
        WaveHeader(
            title = stringResource(R.string.tab_balance),
            subtitle = stringResource(R.string.balance_subtitle),
        )
        val balance = state.balance
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            when {
                state.loading -> Unit
                balance == null -> EmptyWeek(onNewWeek)
                else -> {
                    if (balance.overLimit.isNotEmpty()) {
                        OverLimitAlert(
                            balance.overLimit,
                            onOpenWeek,
                        )
                    }
                    CategoryList(balance)
                    Footnote(balance)
                }
            }
            if (!state.loading) HealthyPlateCard(onClick = { showHealthyPlate = true })
        }
    }
    if (showHealthyPlate) HealthyPlateSheet(onDismiss = { showHealthyPlate = false })
}

@Composable
private fun OverLimitAlert(overLimit: List<CategoryBalance>, onOpenWeek: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        val color = MaterialTheme.colorScheme.onErrorContainer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                AppIcons.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp),
            )
            Text(
                pluralStringResource(R.plurals.balance_alert_title, overLimit.size, overLimit.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
        overLimit.forEach { category ->
            Text(
                pluralStringResource(
                    R.plurals.balance_alert_line,
                    category.count,
                    stringResource(category.category.label),
                    category.count,
                    checkNotNull(category.rule.max),
                    category.slots.map { slotPhrase(it) }.joinToString(", "),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
        }
        Text(
            stringResource(R.string.balance_alert_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
        Button(
            onClick = onOpenWeek,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(stringResource(R.string.balance_show_in_week))
        }
    }
}

@Composable
private fun CategoryList(balance: Balance) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(22.dp))
            .padding(vertical = 4.dp),
    ) {
        balance.categories.forEach { CategoryRow(it) }
    }
}

@Composable
private fun CategoryRow(category: CategoryBalance) {
    val label = stringResource(category.category.label)
    val rule = ruleText(category.rule)
    val (statusColor, statusIcon, statusText) = when (category.status) {
        BalanceStatus.OVER -> Triple(
            MaterialTheme.colorScheme.error,
            AppIcons.Warning,
            R.string.balance_status_over,
        )
        BalanceStatus.UNDER -> Triple(Warning, AppIcons.Warning, R.string.balance_status_under)
        BalanceStatus.OK -> Triple(
            MaterialTheme.colorScheme.primary,
            AppIcons.Check,
            R.string.balance_status_ok,
        )
    }
    val count = when (category.status) {
        BalanceStatus.OVER -> stringResource(
            R.string.balance_count_of,
            category.count,
            checkNotNull(category.rule.max),
        )
        // Only an excess shows the limit, as in the design.
        BalanceStatus.UNDER, BalanceStatus.OK -> category.count.toString()
    }
    val description = "$label, $rule: $count, ${stringResource(statusText)}"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(category.category.containerColor, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                category.category.icon,
                contentDescription = null,
                tint = category.category.contentColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    rule,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Pips(category, category.category.contentColor)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            modifier = Modifier.widthIn(min = 64.dp),
        ) {
            Icon(
                statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp),
            )
            Text(count, style = MaterialTheme.typography.titleSmall, color = statusColor)
        }
    }
}

/** One pip per portion up to the limit; portions beyond the max are red. */
@Composable
private fun Pips(category: CategoryBalance, filledColor: Color) {
    val max = category.rule.max
    val pips = maxOf(category.count, max ?: category.rule.min ?: 0)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(pips) { index ->
            val shape = RoundedCornerShape(4.dp)
            val pip = Modifier.size(width = 22.dp, height = 8.dp)
            when {
                index >= category.count -> Box(pip.border(1.dp, DashedOutline, shape))
                max != null && index >= max -> Box(
                    pip.background(MaterialTheme.colorScheme.error, shape),
                )
                else -> Box(pip.background(filledColor, shape))
            }
        }
    }
}

@Composable
private fun Footnote(balance: Balance) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(4.dp)) {
        Icon(
            AppIcons.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.balance_footnote, balance.uncountedMeals),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HealthyPlateCard(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSecondaryContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        PlateDrawing(Modifier.size(40.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.healthy_plate_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = color,
            )
            Text(
                stringResource(R.string.healthy_plate_card_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
        Icon(AppIcons.ChevronRight, contentDescription = null, tint = color)
    }
}
