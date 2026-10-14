package it.mealmap.ui.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.label
import it.mealmap.ui.components.mealTitle

/** Bottom sheet with the alternatives and portions of the meal selected in [viewModel]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailSheet(
    viewModel: MealDetailViewModel,
    onEdit: (MealSlot) -> Unit,
    onSwap: (MealSlot) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val slot = state.slot ?: return
    // Open only once the meal is loaded, so the sheet does not jump while it fills in.
    if (state.meal == null) return
    ModalBottomSheet(onDismissRequest = viewModel::hide) {
        MealDetailContent(
            slot,
            state,
            onToggleLock = viewModel::toggleLock,
            onEdit = {
                viewModel.hide()
                onEdit(slot)
            },
            onSwap = {
                viewModel.hide()
                onSwap(slot)
            },
            onClose = viewModel::hide,
        )
    }
}

/** The meal detail ViewModel of the current screen. */
@Composable
fun mealDetailViewModel(): MealDetailViewModel =
    appViewModel { MealDetailViewModel(it.weekRepository, it.foodRepository) }

@Composable
fun MealDetailContent(
    slot: MealSlot,
    state: MealDetailUiState,
    onToggleLock: () -> Unit,
    onEdit: () -> Unit,
    onSwap: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val meal = state.meal ?: return
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 12.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(
                        R.string.detail_kicker,
                        dayName(slot.day),
                        stringResource(slot.slot.label),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    mealTitle(meal),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Actions(meal, onEdit = onEdit, onSwap = onSwap, onToggleLock = onToggleLock)
            when (meal) {
                is Meal.Food -> {
                    FoodSection(
                        title = stringResource(meal.protein.label),
                        hint = stringResource(R.string.detail_protein_hint),
                        foods = state.proteinFoods,
                    )
                    FoodSection(
                        title = stringResource(meal.carb.label),
                        hint = stringResource(R.string.detail_carb_hint),
                        foods = state.carbFoods,
                    )
                    VegetablesCard()
                }
                is Meal.Other -> Text(
                    stringResource(R.string.detail_healthy_plate),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

/** Cambia, Scambia and, for food meals, Blocca and Fuori…, which also opens the editor. */
@Composable
private fun Actions(meal: Meal, onEdit: () -> Unit, onSwap: () -> Unit, onToggleLock: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val tile = Modifier.weight(1f)
        ActionTile(
            AppIcons.Edit,
            stringResource(R.string.detail_change),
            onEdit,
            tile,
            primary = true,
        )
        ActionTile(AppIcons.Swap, stringResource(R.string.detail_swap), onSwap, tile)
        if (meal is Meal.Food) {
            val lockLabel = if (meal.locked) R.string.meal_locked_label else R.string.detail_lock
            ActionTile(
                icon = if (meal.locked) AppIcons.Lock else AppIcons.Unlock,
                label = stringResource(lockLabel),
                onClick = onToggleLock,
                modifier = tile,
                toggledOn = meal.locked,
            )
            ActionTile(AppIcons.EatingOut, stringResource(R.string.detail_other), onEdit, tile)
        }
    }
}

/**
 * A square action button with the icon above the label. With [toggledOn] it is a switch,
 * filled while on; [primary] gives a plain button a tonal fill.
 */
@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    toggledOn: Boolean? = null,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when {
        toggledOn == true -> colors.primary to colors.onPrimary
        primary -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHigh to colors.onSurface
    }
    val action = if (toggledOn != null) {
        Modifier.toggleable(value = toggledOn, role = Role.Switch, onValueChange = { onClick() })
    } else {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = modifier
            .heightIn(min = 72.dp)
            .background(container, RoundedCornerShape(16.dp))
            .then(action)
            .padding(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

private const val COLLAPSED_FOODS = 6

@Composable
private fun FoodSection(title: String, hint: String, foods: List<Food>) {
    if (foods.isEmpty()) return
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val shown = if (expanded) foods else foods.take(COLLAPSED_FOODS)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(16.dp),
                )
                .padding(vertical = 4.dp),
        ) {
            shown.forEach { FoodRow(it) }
            if (foods.size > shown.size) {
                TextButton(onClick = {
                    expanded = true
                }, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(pluralStringResource(R.plurals.detail_show_all, foods.size, foods.size))
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(food.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            food.portion,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun VegetablesCard() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Icon(
            AppIcons.Vegetables,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(
                stringResource(R.string.detail_vegetables_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(R.string.detail_vegetables_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
