package it.mealmap.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.components.label

private val MeatCategories =
    listOf(ProteinCategory.WHITE_MEAT, ProteinCategory.RED_MEAT, ProteinCategory.CURED_MEAT)

/** Adds a food to [group], or edits [food]. Meat also asks its category, carbs what they replace. */
@Composable
fun FoodDialog(group: FoodGroup, food: Food?, onSave: (Food) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(food?.name.orEmpty()) }
    var portion by rememberSaveable { mutableStateOf(food?.portion.orEmpty()) }
    var category by rememberSaveable {
        mutableStateOf(
            food?.category ?: group.categories.firstOrNull(),
        )
    }
    var carbs by rememberSaveable { mutableStateOf(food?.carbs.orEmpty().toList()) }
    val valid = name.isNotBlank() && portion.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (food ==
                        null
                    ) {
                        R.string.food_dialog_new
                    } else {
                        R.string.food_dialog_edit
                    },
                ),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.food_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = portion,
                    onValueChange = { portion = it },
                    label = { Text(stringResource(R.string.food_portion)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (group.categories.size > 1) {
                    Text(
                        stringResource(R.string.food_category),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        group.categories.forEach { option ->
                            FilterChip(
                                selected = option == category,
                                onClick = { category = option },
                                label = { Text(stringResource(option.label)) },
                            )
                        }
                    }
                }
                if (group == FoodGroup.CARBS) {
                    Text(
                        stringResource(R.string.food_carbs),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        stringResource(R.string.food_carbs_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Carb.entries.forEach { carb ->
                            FilterChip(
                                selected = carb in carbs,
                                onClick = {
                                    carbs =
                                        if (carb in carbs) carbs - carb else carbs + carb
                                },
                                label = { Text(stringResource(carb.label)) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        Food(
                            id = food?.id ?: 0,
                            group = group,
                            category = category,
                            name = name.trim(),
                            portion = portion.trim(),
                            carbs = carbs.toSet(),
                        ),
                    )
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
