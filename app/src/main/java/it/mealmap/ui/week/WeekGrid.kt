package it.mealmap.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import it.mealmap.R
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Slot
import it.mealmap.domain.model.Week
import it.mealmap.ui.components.MealTile
import it.mealmap.ui.components.dashedBorder
import it.mealmap.ui.components.dayName
import it.mealmap.ui.components.dayShortName
import it.mealmap.ui.components.mealDescription
import it.mealmap.ui.components.slotPhrase
import it.mealmap.ui.theme.DashedOutline
import java.time.DayOfWeek
import kotlin.math.roundToInt

private val DayColumnWidth = 44.dp
private val TileShape = RoundedCornerShape(12.dp)

/** Bounds in root coordinates, not clipped by the scrolling parent like `boundsInRoot()`. */
private fun LayoutCoordinates.unclippedBounds() = Rect(positionInRoot(), size.toSize())

/** A meal being dragged: the pointer and the grab point inside the tile, in root coordinates. */
private data class Drag(val source: MealSlot, val pointer: Offset, val grab: Offset)

/**
 * The 7×2 week grid. Tapping a meal opens it, or completes a swap while [swapSource] is set
 * (then day names can be tapped too). Long-press and drag a meal onto another meal or onto a
 * day name to swap them.
 */
@Composable
fun WeekGrid(
    week: Week,
    today: DayOfWeek,
    swapSource: MealSlot?,
    onOpenMeal: (MealSlot) -> Unit,
    onSwap: (Swap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileBounds = remember { mutableMapOf<MealSlot, Rect>() }
    val dayBounds = remember { mutableMapOf<DayOfWeek, Rect>() }
    var gridOrigin by remember { mutableStateOf(Offset.Zero) }
    var drag by remember { mutableStateOf<Drag?>(null) }
    val currentOnSwap by rememberUpdatedState(onSwap)
    val haptics = LocalHapticFeedback.current

    /** The swap that dropping [from] at [point] would make, if any. */
    fun swapAt(from: MealSlot, point: Offset): Swap? {
        tileBounds.entries.firstOrNull { point in it.value }?.key?.let { to ->
            return if (to != from) Swap.Meals(from, to) else null
        }
        dayBounds.entries.firstOrNull { point in it.value }?.key?.let { day ->
            return if (day != from.day) Swap.Days(from.day, day) else null
        }
        return null
    }
    val hovered = drag?.let { swapAt(it.source, it.pointer) }

    fun Modifier.draggableMeal(slot: MealSlot) = pointerInput(slot) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                val bounds = tileBounds[slot] ?: return@detectDragGesturesAfterLongPress
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                drag = Drag(slot, pointer = bounds.topLeft + offset, grab = offset)
            },
            onDrag = { change, amount ->
                change.consume()
                drag = drag?.let { it.copy(pointer = it.pointer + amount) }
            },
            onDragEnd = {
                drag?.let { swapAt(it.source, it.pointer) }?.let(currentOnSwap)
                drag = null
            },
            onDragCancel = { drag = null },
        )
    }

    Box(modifier.onGloballyPositioned { gridOrigin = it.positionInRoot() }) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    DayLabel(
                        day = day,
                        isToday = day == today,
                        dropTarget = drag != null || swapSource != null,
                        hovered = hovered is Swap.Days && hovered.b == day,
                        onSwapHere = swapSource
                            ?.takeIf { it.day != day }
                            ?.let { source -> { onSwap(Swap.Days(source.day, day)) } },
                        modifier = Modifier
                            .fillMaxHeight()
                            .onGloballyPositioned { dayBounds[day] = it.unclippedBounds() },
                    )
                    Slot.entries.forEach { slot ->
                        val mealSlot = MealSlot(day, slot)
                        GridTile(
                            week = week,
                            slot = mealSlot,
                            isToday = day == today,
                            dragged = drag?.source == mealSlot,
                            hovered = hovered is Swap.Meals && hovered.b == mealSlot,
                            onClick = tapAction(
                                swapSource,
                                mealSlot,
                                onOpenMeal,
                                onSwap,
                            )?.let { action ->
                                // Releasing a long press that started a drag is not a tap.
                                { if (drag == null) action() }
                            },
                            swapping = swapSource != null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .onGloballyPositioned {
                                    tileBounds[mealSlot] = it.unclippedBounds()
                                }
                                .draggableMeal(mealSlot),
                        )
                    }
                }
            }
        }
        drag?.let { DraggedTile(week, it, tileBounds[it.source], gridOrigin) }
    }
}

/** Opens the meal, or swaps it with [swapSource] in swap mode; nothing for the source itself. */
private fun tapAction(
    swapSource: MealSlot?,
    slot: MealSlot,
    onOpenMeal: (MealSlot) -> Unit,
    onSwap: (Swap) -> Unit,
): (() -> Unit)? = when (swapSource) {
    null -> {
        { onOpenMeal(slot) }
    }
    slot -> null
    else -> {
        { onSwap(Swap.Meals(swapSource, slot)) }
    }
}

@Composable
private fun GridTile(
    week: Week,
    slot: MealSlot,
    isToday: Boolean,
    dragged: Boolean,
    hovered: Boolean,
    onClick: (() -> Unit)?,
    swapping: Boolean,
    modifier: Modifier = Modifier,
) {
    val meal = week[slot]
    val description = "${slotPhrase(slot, capitalized = true)}: ${mealDescription(meal)}"
    val clickLabel = stringResource(if (swapping) R.string.week_swap_here else R.string.open_detail)
    val hoverBorder = if (hovered) {
        Modifier.border(
            3.dp,
            MaterialTheme.colorScheme.primary,
            TileShape,
        )
    } else {
        Modifier
    }
    MealTile(
        meal = meal,
        highlighted = isToday,
        modifier = modifier
            .then(hoverBorder)
            .alpha(if (dragged) 0.35f else 1f)
            // The semantics replace the inner clickable's, keeping one node per tile.
            .clearAndSetSemantics {
                contentDescription = description
                if (onClick != null) {
                    role = Role.Button
                    onClick(clickLabel) {
                        onClick()
                        true
                    }
                }
            }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    )
}

@Composable
private fun DraggedTile(week: Week, drag: Drag, bounds: Rect?, gridOrigin: Offset) {
    bounds ?: return
    val density = LocalDensity.current
    MealTile(
        meal = week[drag.source],
        highlighted = false,
        modifier = Modifier
            .zIndex(1f)
            .offset {
                val topLeft = drag.pointer - drag.grab - gridOrigin
                IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
            }
            .size(with(density) { bounds.width.toDp() }, with(density) { bounds.height.toDp() })
            .graphicsLayer {
                rotationZ = -5f
                scaleX = 1.06f
                scaleY = 1.06f
                shadowElevation = 8.dp.toPx()
                shape = TileShape
                clip = true
            },
    )
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

/** Day name; while a meal is being moved it also accepts a whole-day swap. */
@Composable
private fun DayLabel(
    day: DayOfWeek,
    isToday: Boolean,
    dropTarget: Boolean,
    hovered: Boolean,
    onSwapHere: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val name = dayName(day)
    val description = if (isToday) stringResource(R.string.day_today, name) else name
    val swapLabel = stringResource(R.string.week_swap_here)
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val background = when {
        hovered -> Modifier.background(colors.secondaryContainer, shape)
        isToday -> Modifier.background(colors.primary, shape)
        else -> Modifier
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(DayColumnWidth)
            .then(background)
            .then(
                if (dropTarget) {
                    Modifier.dashedBorder(
                        DashedOutline,
                        cornerRadius = 12.dp,
                    )
                } else {
                    Modifier
                },
            )
            .clearAndSetSemantics {
                contentDescription = description
                if (onSwapHere != null) {
                    role = Role.Button
                    onClick(swapLabel) {
                        onSwapHere()
                        true
                    }
                }
            }
            .then(if (onSwapHere != null) Modifier.clickable(onClick = onSwapHere) else Modifier),
    ) {
        Text(
            dayShortName(day),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isToday && !hovered) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}
