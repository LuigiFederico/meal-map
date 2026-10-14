package it.mealmap.ui.newweek

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Slot
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.dashedBorder
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.icon
import it.mealmap.ui.components.label
import it.mealmap.ui.components.slotPhrase
import it.mealmap.ui.theme.DashedOutline
import java.time.DayOfWeek

@Composable
fun NewWeekScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = appViewModel { NewWeekViewModel(it.weekRepository, it.rulesRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.generated) { if (state.generated) onDone() }
    NewWeekContent(
        state,
        onCycle = viewModel::cycle,
        onGenerate = viewModel::generate,
        onClose = onDone,
        modifier,
    )
}

@Composable
fun NewWeekContent(
    state: NewWeekUiState,
    onCycle: (MealSlot) -> Unit,
    onGenerate: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(start = 4.dp, end = 12.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.close))
            }
            Text(
                stringResource(R.string.new_week_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        ) {
            Text(
                stringResource(R.string.new_week_question),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.new_week_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Legend()
            if (!state.loading) SlotList(state.plans, onCycle)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    AppIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    stringResource(R.string.new_week_lock_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Footer(state, onGenerate)
    }
}

@Composable
private fun Legend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendItem(AppIcons.Generate, stringResource(R.string.new_week_generate_slot))
        LegendItem(AppIcons.Lock, stringResource(R.string.new_week_fixed_legend))
        OtherKindsInOrder.forEach { LegendItem(it.icon, stringResource(it.label)) }
    }
}

@Composable
private fun LegendItem(icon: ImageVector, text: String) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun SlotList(plans: Map<MealSlot, SlotPlan>, onCycle: (MealSlot) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp),
    ) {
        DayOfWeek.entries.forEach { day ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            ) {
                Text(
                    dayName(day),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.width(84.dp),
                )
                Slot.entries.forEach { slot ->
                    val mealSlot = MealSlot(day, slot)
                    SlotButton(mealSlot, plans.getValue(mealSlot), {
                        onCycle(mealSlot)
                    }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SlotButton(
    slot: MealSlot,
    plan: SlotPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val (icon, label) = when (plan) {
        SlotPlan.Generate -> AppIcons.Generate to stringResource(R.string.new_week_generate_slot)
        // The lock icon already says "fixed": the label keeps only the protein, to fit.
        is SlotPlan.Keep -> AppIcons.Lock to stringResource(plan.meal.protein.label)
        is SlotPlan.Other -> plan.kind.icon to stringResource(plan.kind.label)
    }
    val (content, look) = when (plan) {
        SlotPlan.Generate ->
            colors.primary to Modifier
                .background(colors.surface, shape)
                .border(BorderStroke(1.dp, colors.primaryContainer), shape)
        is SlotPlan.Keep -> colors.onPrimary to Modifier.background(colors.primary, shape)
        is SlotPlan.Other ->
            colors.onSurface to Modifier
                .background(colors.surfaceContainerHigh, shape)
                .dashedBorder(DashedOutline, cornerRadius = 12.dp)
    }
    val spoken = if (plan is SlotPlan.Keep) {
        stringResource(
            R.string.new_week_fixed_meal,
            label,
        )
    } else {
        label
    }
    val description = "${slotPhrase(slot, capitalized = true)}: $spoken"
    val changeLabel = stringResource(R.string.new_week_change_hint)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(look)
            .clip(shape)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick(changeLabel) {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Column {
            Text(
                stringResource(slot.slot.label),
                style = MaterialTheme.typography.labelSmall,
                color = content,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Footer(state: NewWeekUiState, onGenerate: () -> Unit) {
    val plans = state.plans.values
    val toGenerate = plans.count { it == SlotPlan.Generate }
    val fixed = plans.count { it is SlotPlan.Keep }
    val uncounted = plans.count { it is SlotPlan.Other }
    val summary = listOf(
        pluralStringResource(R.plurals.new_week_to_generate, toGenerate, toGenerate),
        pluralStringResource(R.plurals.new_week_fixed_count, fixed, fixed),
        pluralStringResource(R.plurals.new_week_uncounted, uncounted, uncounted),
    ).joinToString(" · ")
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
    ) {
        if (!state.loading) Text(summary, style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = onGenerate,
            enabled = !state.loading && !state.generating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(AppIcons.Generate, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(stringResource(R.string.new_week_cta), modifier = Modifier.padding(start = 10.dp))
        }
        Text(
            stringResource(R.string.new_week_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
