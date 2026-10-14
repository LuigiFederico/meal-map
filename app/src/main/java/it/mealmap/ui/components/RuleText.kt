package it.mealmap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.domain.model.FrequencyRule

/** A weekly limit in words: "esattamente 2", "1–2", "almeno 3", "al massimo 1" or "libero". */
@Composable
fun ruleText(rule: FrequencyRule): String {
    val (min, max) = rule
    return when {
        min != null && min == max -> stringResource(R.string.rule_exactly, min)
        min != null && max != null -> stringResource(R.string.rule_range, min, max)
        min != null -> stringResource(R.string.rule_at_least, min)
        max != null -> stringResource(R.string.rule_at_most, max)
        else -> stringResource(R.string.rule_free)
    }
}
