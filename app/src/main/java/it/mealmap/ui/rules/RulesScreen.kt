package it.mealmap.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.IconTile
import it.mealmap.ui.components.colorCategory
import it.mealmap.ui.components.containerColor
import it.mealmap.ui.components.contentColor
import it.mealmap.ui.components.icon
import it.mealmap.ui.components.label
import it.mealmap.ui.components.ruleText
import java.util.Locale

/** The rules section of the settings: weekly frequencies and food alternatives. */
@Composable
fun RulesContent(
    state: RulesUiState,
    onRuleChange: (ProteinCategory, RuleStep) -> Unit,
    onReset: () -> Unit,
    onOpenGroup: (FoodGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
    ) {
        Column {
            Text(
                stringResource(R.string.rules_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Intro(stringResource(R.string.rules_subtitle))
        }
        val tabs = listOf(R.string.rules_tab_frequencies, R.string.rules_tab_alternatives)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = tab == index,
                    onClick = { tab = index },
                    shape = SegmentedButtonDefaults.itemShape(index, tabs.size),
                    icon = {},
                ) { Text(stringResource(label)) }
            }
        }
        if (state.loading) return@Column
        when (tab) {
            0 -> Frequencies(state.rules, onRuleChange, onReset)
            else -> Groups(state.groupCounts, onOpenGroup)
        }
    }
}

@Composable
private fun Intro(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Frequencies(
    rules: Map<ProteinCategory, FrequencyRule>,
    onRuleChange: (ProteinCategory, RuleStep) -> Unit,
    onReset: () -> Unit,
) {
    Intro(stringResource(R.string.rules_frequencies_intro))
    ProteinCategory.entries.forEach { category ->
        val rule = rules[category] ?: FrequencyRule()
        FrequencyCard(category, rule) { onRuleChange(category, it) }
    }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(AppIcons.Reset, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.rules_reset), modifier = Modifier.padding(start = 8.dp))
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.rules_reset)) },
            text = { Text(stringResource(R.string.rules_reset_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) { Text(stringResource(R.string.rules_reset_action)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmReset = false
                }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun FrequencyCard(
    category: ProteinCategory,
    rule: FrequencyRule,
    onChange: (RuleStep) -> Unit,
) {
    val label = stringResource(category.label)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(20.dp))
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(category.icon, category.containerColor, category.contentColor)
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                ruleText(rule),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stepper(
                name = stringResource(R.string.rules_min),
                category = label,
                value = rule.min,
                onDecrease = rule.action(FrequencyRule::decreaseMin, onChange),
                onIncrease = rule.action(FrequencyRule::increaseMin, onChange),
                modifier = Modifier.weight(1f),
            )
            Stepper(
                name = stringResource(R.string.rules_max),
                category = label,
                value = rule.max,
                onDecrease = rule.action(FrequencyRule::decreaseMax, onChange),
                onIncrease = rule.action(FrequencyRule::increaseMax, onChange),
                modifier = Modifier.weight(1f),
            )
        }
        rule.action(FrequencyRule::removeMax, onChange)?.let { removeMax ->
            TextButton(onClick = removeMax, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.rules_remove_max))
            }
        }
    }
}

/** A stepper step: the new rule, or null when the step is not allowed. */
typealias RuleStep = (FrequencyRule) -> FrequencyRule?

/** The button action for [step], or null (disabled) when it does not apply to this rule. */
private fun FrequencyRule.action(step: RuleStep, onChange: (RuleStep) -> Unit): (() -> Unit)? =
    if (step(this) != null) ({ onChange(step) }) else null

/** "Minimo  − 3 +"; a null action disables its button. */
@Composable
private fun Stepper(
    name: String,
    category: String,
    value: Int?,
    onDecrease: (() -> Unit)?,
    onIncrease: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val lowerName = name.lowercase(Locale.ITALIAN)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(24.dp),
            ),
        ) {
            IconButton(onClick = { onDecrease?.invoke() }, enabled = onDecrease != null) {
                Icon(
                    AppIcons.Minus,
                    contentDescription = stringResource(
                        R.string.rules_decrease,
                        lowerName,
                        category,
                    ),
                )
            }
            val shown = value?.toString() ?: stringResource(R.string.rules_unset)
            Text(
                shown,
                style = if (value !=
                    null
                ) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.labelMedium
                },
                color = if (value !=
                    null
                ) {
                    Color.Unspecified
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = "$name $category: $shown" },
            )
            IconButton(onClick = { onIncrease?.invoke() }, enabled = onIncrease != null) {
                Icon(
                    AppIcons.Plus,
                    contentDescription = stringResource(
                        R.string.rules_increase,
                        lowerName,
                        category,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Groups(counts: Map<FoodGroup, Int>, onOpenGroup: (FoodGroup) -> Unit) {
    Intro(stringResource(R.string.rules_alternatives_intro))
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp),
    ) {
        FoodGroup.entries.forEach { group ->
            val colors = group.colorCategory
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(role = Role.Button) { onOpenGroup(group) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                IconTile(
                    group.icon,
                    colors?.containerColor ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                    colors?.contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(group.label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    (counts[group] ?: 0).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.End,
                )
                Icon(
                    AppIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
