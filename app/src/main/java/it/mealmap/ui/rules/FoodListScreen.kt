package it.mealmap.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.label

/** A food that was just deleted; [id] tells apart two deletions of the same food. */
private data class Deletion(val food: Food, val id: Long)

@Composable
fun FoodListScreen(group: FoodGroup, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = appViewModel { FoodListViewModel(group, it.foodRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var deletion by remember { mutableStateOf<Deletion?>(null) }
    // The dialog edits the food with `editingId`, or adds a new one while `adding`.
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    val editing = editingId?.let { id -> state.foods.find { it.id == id } }

    Box(modifier.fillMaxSize()) {
        FoodListContent(
            group = group,
            state = state,
            onBack = onBack,
            onEdit = { editingId = it.id },
            onDelete = { food ->
                viewModel.delete(food)
                deletion = Deletion(food, System.nanoTime())
            },
        )
        ExtendedFloatingActionButton(
            onClick = { adding = true },
            icon = { Icon(AppIcons.Plus, contentDescription = null) },
            text = { Text(stringResource(R.string.foods_add)) },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
        )
    }

    if (adding || editing != null) {
        FoodDialog(
            group = group,
            food = editing,
            onSave = { food ->
                viewModel.save(food)
                adding = false
                editingId = null
            },
            onDismiss = {
                adding = false
                editingId = null
            },
        )
    }

    deletion?.let { event ->
        val message = stringResource(R.string.foods_deleted, event.food.name)
        val undo = stringResource(R.string.undo)
        LaunchedEffect(event) {
            val result = snackbar.showSnackbar(message, undo, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.save(event.food)
        }
    }
}

@Composable
fun FoodListContent(
    group: FoodGroup,
    state: FoodListUiState,
    onBack: () -> Unit,
    onEdit: (Food) -> Unit,
    onDelete: (Food) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(state.foods, query) {
        val search = query.trim()
        state.foods.filter { it.name.contains(search, ignoreCase = true) }
    }
    Column(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 4.dp, end = 12.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
            }
            Column {
                Text(
                    stringResource(group.label),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
                if (!state.loading) {
                    Text(
                        pluralStringResource(
                            R.plurals.foods_subtitle,
                            state.foods.size,
                            state.foods.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        val searchLabel = stringResource(R.string.foods_search)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(searchLabel) },
            leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .semantics { contentDescription = searchLabel }
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        ) {
            items(shown, key = { it.id }) { food ->
                FoodRow(food, onEdit = { onEdit(food) }, onDelete = { onDelete(food) })
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 4.dp),
    ) {
        Text(food.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            food.portion,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        IconButton(onClick = onEdit) {
            Icon(AppIcons.Edit, contentDescription = stringResource(R.string.foods_edit, food.name))
        }
        IconButton(onClick = onDelete) {
            Icon(
                AppIcons.Trash,
                contentDescription = stringResource(R.string.foods_delete, food.name),
            )
        }
    }
}
