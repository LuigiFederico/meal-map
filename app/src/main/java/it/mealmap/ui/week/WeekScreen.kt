package it.mealmap.ui.week

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.mealmap.R
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.ui.appViewModel
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.components.EmptyWeek
import it.mealmap.ui.components.WaveHeader
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.label
import it.mealmap.ui.components.rememberToday
import it.mealmap.ui.components.slotPhrase
import it.mealmap.ui.meal.MealDetailSheet
import it.mealmap.ui.meal.mealDetailViewModel
import java.time.DayOfWeek
import java.util.Locale

/** A swap just made; [id] tells apart two identical swaps in a row. */
private data class SwapEvent(val swap: Swap, val id: Long)

@Composable
fun WeekScreen(
    overLimit: List<ProteinCategory>,
    swapSource: MealSlot?,
    onSwapSourceChange: (MealSlot?) -> Unit,
    onOpenBalance: () -> Unit,
    onEditMeal: (MealSlot) -> Unit,
    onNewWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = appViewModel { WeekViewModel(it.weekRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = mealDetailViewModel()
    val snackbar = remember { SnackbarHostState() }
    var lastSwap by remember { mutableStateOf<SwapEvent?>(null) }
    // Swap mode only lasts while the week is on screen.
    val currentOnSwapSourceChange by rememberUpdatedState(onSwapSourceChange)
    DisposableEffect(Unit) { onDispose { currentOnSwapSourceChange(null) } }
    // Back cancels a pending swap before it leaves the tab.
    BackHandler(enabled = swapSource != null) { onSwapSourceChange(null) }

    Box(modifier.fillMaxSize()) {
        WeekContent(
            state = state,
            overLimit = overLimit,
            today = rememberToday(),
            swapSource = swapSource,
            onCancelSwap = { onSwapSourceChange(null) },
            onOpenBalance = onOpenBalance,
            onOpenMeal = detail::show,
            onNewWeek = onNewWeek,
            onSwap = { swap ->
                viewModel.swap(swap)
                onSwapSourceChange(null)
                lastSwap = SwapEvent(swap, System.nanoTime())
            },
        )
        if (state.week != null) {
            ExtendedFloatingActionButton(
                onClick = onNewWeek,
                icon = { Icon(AppIcons.Generate, contentDescription = null) },
                text = { Text(stringResource(R.string.new_week_title)) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
        )
    }
    MealDetailSheet(detail, onEdit = onEditMeal, onSwap = onSwapSourceChange)

    lastSwap?.let { event ->
        val message = swapMessage(event.swap)
        val undo = stringResource(R.string.undo)
        LaunchedEffect(event) {
            val result = snackbar.showSnackbar(message, undo, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.swap(event.swap)
            }
        }
    }
}

@Composable
private fun swapMessage(swap: Swap): String = when (swap) {
    is Swap.Meals -> stringResource(
        R.string.week_swapped,
        slotPhrase(swap.a, capitalized = true),
        slotPhrase(swap.b),
    )
    is Swap.Days -> stringResource(
        R.string.week_swapped,
        dayName(swap.a),
        dayName(swap.b).lowercase(Locale.ITALIAN),
    )
}

@Composable
fun WeekContent(
    state: WeekUiState,
    overLimit: List<ProteinCategory>,
    today: DayOfWeek,
    swapSource: MealSlot?,
    onCancelSwap: () -> Unit,
    onOpenBalance: () -> Unit,
    onOpenMeal: (MealSlot) -> Unit,
    onNewWeek: () -> Unit,
    onSwap: (Swap) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        WaveHeader(
            title = stringResource(R.string.week_title),
            subtitle = stringResource(R.string.week_subtitle),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 88.dp),
        ) {
            val week = state.week
            when {
                state.loading -> Unit
                week == null -> EmptyWeek(onNewWeek)
                else -> {
                    if (overLimit.isNotEmpty()) OverLimitChip(overLimit, onOpenBalance)
                    swapSource?.let { SwapModeBanner(it, onCancelSwap) }
                    WeekGrid(week, today, swapSource, onOpenMeal, onSwap)
                    DragHint()
                }
            }
        }
    }
}

@Composable
private fun SwapModeBanner(source: MealSlot, onCancel: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Icon(
            AppIcons.Swap,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            stringResource(R.string.week_swap_mode, slotPhrase(source)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        )
        TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
    }
}

@Composable
private fun DragHint() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            AppIcons.Drag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            stringResource(R.string.week_drag_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
