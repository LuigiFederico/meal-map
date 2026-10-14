package it.mealmap.ui.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.data.ReminderMoment
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Slot
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.EmptyWeek
import it.mealmap.ui.components.WaveHeader
import it.mealmap.ui.components.containerColor
import it.mealmap.ui.components.contentColor
import it.mealmap.ui.components.dashedBorder
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.icon
import it.mealmap.ui.components.label
import it.mealmap.ui.components.mealDescription
import it.mealmap.ui.components.mealTitle
import it.mealmap.ui.components.rememberToday
import it.mealmap.ui.meal.MealDetailSheet
import it.mealmap.ui.meal.mealDetailViewModel
import it.mealmap.ui.reminders.RemindersSheet
import it.mealmap.ui.reminders.label
import it.mealmap.ui.theme.DashedOutline
import it.mealmap.ui.theme.HeaderSubtitle
import it.mealmap.ui.week.WeekUiState
import it.mealmap.ui.week.WeekViewModel
import java.time.DayOfWeek

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val viewModel = appViewModel { WeekViewModel(it.weekRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = mealDetailViewModel()
    var reminder by rememberSaveable { mutableStateOf<ReminderMoment?>(null) }

    TodayContent(
        state = state,
        today = rememberToday(),
        onOpenMeal = detail::show,
        onOpenReminder = { reminder = it },
        modifier = modifier,
    )
    MealDetailSheet(detail)
    reminder?.let { RemindersSheet(it, onDismiss = { reminder = null }) }
}

@Composable
fun TodayContent(
    state: WeekUiState,
    today: DayOfWeek,
    onOpenMeal: (MealSlot) -> Unit,
    onOpenReminder: (ReminderMoment) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDay by rememberSaveable(today) { mutableStateOf(today) }
    val isToday = selectedDay == today
    Column(modifier.verticalScroll(rememberScrollState())) {
        WaveHeader(
            title = dayName(selectedDay),
            subtitle = stringResource(
                if (isToday) R.string.today_subtitle else R.string.today_subtitle_other_day,
            ),
            height = 168.dp,
        ) {
            if (!isToday) {
                OutlinedButton(
                    onClick = { selectedDay = today },
                    border = BorderStroke(1.dp, HeaderSubtitle),
                ) {
                    Text(stringResource(R.string.today_back_to_today), color = Color.White)
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            DayStrip(selectedDay, today, onSelect = { selectedDay = it })
            val week = state.week
            ReminderRow(
                ReminderMoment.BREAKFAST,
                R.string.reminder_breakfast_hint,
                AppIcons.Breakfast,
                onOpenReminder,
            )
            ReminderRow(
                ReminderMoment.SNACK,
                R.string.reminder_snack_hint,
                AppIcons.Snack,
                onOpenReminder,
            )
            if (week == null) {
                if (!state.loading) EmptyWeek()
            } else {
                val lunch = MealSlot(selectedDay, Slot.LUNCH)
                MealCard(Slot.LUNCH, week[lunch], onClick = { onOpenMeal(lunch) })
            }
            ReminderRow(
                ReminderMoment.AFTERNOON_SNACK,
                R.string.reminder_afternoon_snack_hint,
                AppIcons.AfternoonSnack,
                onOpenReminder,
            )
            if (week != null) {
                val dinner = MealSlot(selectedDay, Slot.DINNER)
                MealCard(Slot.DINNER, week[dinner], onClick = { onOpenMeal(dinner) })
            }
        }
    }
}

@Composable
private fun DayStrip(selected: DayOfWeek, today: DayOfWeek, onSelect: (DayOfWeek) -> Unit) {
    val letters = stringArrayResource(R.array.days_letter)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .selectableGroup(),
    ) {
        DayOfWeek.entries.forEach { day ->
            val isSelected = day == selected
            val colors = MaterialTheme.colorScheme
            val name = dayName(day)
            val dayDescription = if (day ==
                today
            ) {
                stringResource(R.string.day_today, name)
            } else {
                name
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isSelected) colors.primary else Color.Transparent, CircleShape)
                    .then(
                        if (day == today && !isSelected) {
                            Modifier.border(1.5.dp, colors.primary, CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(day) })
                    .semantics { contentDescription = dayDescription },
            ) {
                Text(
                    letters[day.ordinal],
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) colors.onPrimary else colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ReminderRow(
    moment: ReminderMoment,
    hint: Int,
    icon: ImageVector,
    onOpen: (ReminderMoment) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onOpen(moment) }
            .padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(stringResource(moment.label), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            AppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MealCard(slot: Slot, meal: Meal, onClick: () -> Unit) {
    val description = "${stringResource(slot.label)}: ${mealDescription(meal)}"
    val openLabel = stringResource(R.string.open_detail)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(20.dp))
            // One spoken sentence instead of the title followed by the same words in the chips.
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick(openLabel) {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(slot.label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (meal is Meal.Food && meal.locked) {
                Icon(
                    AppIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    stringResource(R.string.meal_locked_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        when (meal) {
            is Meal.Food -> {
                Text(mealTitle(meal), style = MaterialTheme.typography.titleLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MealChip(
                        meal.protein.icon,
                        stringResource(meal.protein.label),
                        meal.protein.containerColor,
                        meal.protein.contentColor,
                    )
                    MealChip(AppIcons.Vegetables, stringResource(R.string.vegetables))
                    MealChip(AppIcons.Carb, stringResource(meal.carb.label))
                }
            }
            is Meal.Other -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .dashedBorder(DashedOutline, cornerRadius = 12.dp),
                ) {
                    Icon(
                        meal.kind.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column {
                    Text(
                        stringResource(meal.kind.label),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.meal_no_category),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** A small label with an icon; filled with [container] when given, otherwise outlined. */
@Composable
private fun MealChip(
    icon: ImageVector,
    text: String,
    container: Color? = null,
    content: Color? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val color = content ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .then(
                if (container != null) {
                    Modifier.background(container, shape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                },
            )
            .padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
