package it.mealmap.ui.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.IconTile
import it.mealmap.ui.components.containerColor
import it.mealmap.ui.components.contentColor
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.icon
import it.mealmap.ui.components.label
import it.mealmap.ui.components.mealTitle

@Composable
fun EditMealScreen(slot: MealSlot, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = appViewModel { EditMealViewModel(slot, it.weekRepository, it.rulesRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    EditMealContent(
        slot = slot,
        state = state,
        onSelectProtein = viewModel::selectProtein,
        onSelectCarb = viewModel::selectCarb,
        onSelectOther = viewModel::selectOther,
        onSave = viewModel::save,
        onClose = onDone,
        modifier = modifier,
    )
}

@Composable
fun EditMealContent(
    slot: MealSlot,
    state: EditMealUiState,
    onSelectProtein: (ProteinCategory) -> Unit,
    onSelectCarb: (Carb) -> Unit,
    onSelectOther: (OtherKind) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 4.dp, end = 12.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.edit_close))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.edit_kicker),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(
                        R.string.detail_kicker,
                        dayName(slot.day),
                        stringResource(slot.slot.label),
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Button(onClick = onSave, enabled = state.selection != null) {
                Text(stringResource(R.string.save))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        val current = state.current ?: return@Column
        val selection = state.selection ?: return@Column
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                "${stringResource(R.string.edit_now)} ${mealTitle(current)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ProteinChoices(current, selection, state, onSelectProtein)
            OverLimitWarning(selection, state)
            if (selection is Meal.Food) CarbChoices(selection.carb, onSelectCarb)
            OtherChoices(selection, onSelectOther)
        }
    }
}

@Composable
private fun ProteinChoices(
    current: Meal,
    selection: Meal,
    state: EditMealUiState,
    onSelect: (ProteinCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.edit_protein))
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(20.dp),
                )
                .padding(vertical = 4.dp)
                .selectableGroup(),
        ) {
            val colors = MaterialTheme.colorScheme
            ProteinCategory.entries.forEach { protein ->
                val selected = (selection as? Meal.Food)?.protein == protein
                val isCurrent = (current as? Meal.Food)?.protein == protein
                val count = state.othersCount[protein].orZero() + 1
                val rule = state.rules[protein] ?: FrequencyRule()
                val over = rule.max != null && count > rule.max
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .selectable(selected = selected, role = Role.RadioButton) {
                            onSelect(protein)
                        }
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    RadioButton(selected = selected, onClick = null)
                    IconTile(protein.icon, protein.containerColor, protein.contentColor)
                    Text(
                        stringResource(protein.label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        projection(count, rule, isCurrent),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (over) colors.error else colors.onSurfaceVariant,
                        fontWeight = if (over) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

/** "diventa 2 · max 1": the category's weekly count if this meal is (or stays) in it. */
@Composable
private fun projection(count: Int, rule: FrequencyRule, isCurrent: Boolean): String {
    val text = if (isCurrent) R.string.edit_projection_current else R.string.edit_projection_new
    val limit = when {
        rule.max != null -> stringResource(R.string.edit_limit_max, rule.max)
        rule.min != null -> stringResource(R.string.edit_limit_min, rule.min)
        else -> null
    }
    return listOfNotNull(stringResource(text, count), limit).joinToString(" · ")
}

@Composable
private fun OverLimitWarning(selection: Meal, state: EditMealUiState) {
    val protein = (selection as? Meal.Food)?.protein ?: return
    val max = state.rules[protein]?.max ?: return
    val count = state.othersCount[protein].orZero() + 1
    if (count <= max) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        val color = MaterialTheme.colorScheme.onErrorContainer
        Icon(
            AppIcons.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Text(
            stringResource(R.string.edit_over_limit, stringResource(protein.label), count, max),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
    }
}

@Composable
private fun CarbChoices(selected: Carb, onSelect: (Carb) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.edit_carb))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Carb.entries.forEach { carb ->
                FilterChip(
                    selected = carb == selected,
                    onClick = { onSelect(carb) },
                    label = { Text(stringResource(carb.label)) },
                )
            }
        }
    }
}

@Composable
private fun OtherChoices(selection: Meal, onSelect: (OtherKind) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.edit_other))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OtherKind.entries.forEach { kind ->
                FilterChip(
                    selected = (selection as? Meal.Other)?.kind == kind,
                    onClick = { onSelect(kind) },
                    label = { Text(stringResource(kind.label)) },
                    leadingIcon = {
                        Icon(kind.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics {
            heading()
        },
    )
}

private fun Int?.orZero() = this ?: 0
