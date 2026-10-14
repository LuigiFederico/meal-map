package it.mealmap.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.domain.model.Week
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.EmptyWeek
import it.mealmap.ui.components.MealTile
import it.mealmap.ui.components.WaveHeader
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.dayShortName
import it.mealmap.ui.components.label
import it.mealmap.ui.components.mealDescription
import it.mealmap.ui.components.rememberToday
import it.mealmap.ui.components.slotPhrase
import it.mealmap.ui.meal.MealDetailSheet
import it.mealmap.ui.meal.mealDetailViewModel
import java.time.DayOfWeek

@Composable
fun WeekScreen(
    overLimit: List<ProteinCategory>,
    onOpenBalance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = appViewModel { WeekViewModel(it.weekRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = mealDetailViewModel()
    WeekContent(
        state,
        overLimit,
        today = rememberToday(),
        onOpenBalance = onOpenBalance,
        onOpenMeal = detail::show,
        modifier = modifier,
    )
    MealDetailSheet(detail)
}

@Composable
fun WeekContent(
    state: WeekUiState,
    overLimit: List<ProteinCategory>,
    today: DayOfWeek,
    onOpenBalance: () -> Unit,
    onOpenMeal: (MealSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        WaveHeader(
            title = stringResource(R.string.week_title),
            subtitle = stringResource(R.string.week_subtitle),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            val week = state.week
            when {
                state.loading -> Unit
                week == null -> EmptyWeek()
                else -> {
                    if (overLimit.isNotEmpty()) OverLimitChip(overLimit, onOpenBalance)
                    WeekGrid(week, today, onOpenMeal)
                }
            }
        }
    }
}

@Composable
private fun OverLimitChip(overLimit: List<ProteinCategory>, onClick: () -> Unit) {
    val text = overLimit.singleOrNull()
        ?.let { stringResource(R.string.week_over_limit_one, stringResource(it.label)) }
        ?: pluralStringResource(
            R.plurals.week_over_limit_many,
            overLimit.size,
            overLimit.size,
        )
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(AppIcons.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            Icon(AppIcons.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
            trailingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    )
}

@Composable
private fun WeekGrid(week: Week, today: DayOfWeek, onOpenMeal: (MealSlot) -> Unit) {
    val openLabel = stringResource(R.string.open_detail)
    val gridDescription = stringResource(R.string.week_grid_description)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics { contentDescription = gridDescription },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.width(DayColumnWidth))
            ColumnHeader(stringResource(R.string.week_column_lunch), Modifier.weight(1f))
            ColumnHeader(stringResource(R.string.week_column_dinner), Modifier.weight(1f))
        }
        DayOfWeek.entries.forEach { day ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                DayLabel(day, isToday = day == today, Modifier.fillMaxHeight())
                Slot.entries.forEach { slot ->
                    val meal = week[MealSlot(day, slot)]
                    val mealSlot = MealSlot(day, slot)
                    val description =
                        "${slotPhrase(mealSlot, capitalized = true)}: ${mealDescription(meal)}"
                    MealTile(
                        meal = meal,
                        highlighted = day == today,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            // The semantics replace the inner clickable's, keeping one node per tile.
                            .clearAndSetSemantics {
                                contentDescription = description
                                role = Role.Button
                                onClick(openLabel) {
                                    onOpenMeal(mealSlot)
                                    true
                                }
                            }
                            .clickable { onOpenMeal(mealSlot) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp),
    )
}

@Composable
private fun DayLabel(day: DayOfWeek, isToday: Boolean, modifier: Modifier = Modifier) {
    val name = dayName(day)
    val colors = MaterialTheme.colorScheme
    val description = if (isToday) stringResource(R.string.day_today, name) else name
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(DayColumnWidth)
            .then(
                if (isToday) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Text(
            dayShortName(day),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isToday) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}

private val DayColumnWidth = 44.dp
