package it.mealmap.ui.balance

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.ui.components.AppIcons
import it.mealmap.ui.theme.EggsContainer
import it.mealmap.ui.theme.LegumesContainer
import it.mealmap.ui.theme.WhiteMeatContainer

/** Fixed food pyramid frequencies, independent of the user's rules. */
private val WeeklyFrequencies = listOf(
    R.string.healthy_plate_legumes to R.string.healthy_plate_legumes_value,
    R.string.healthy_plate_fish to R.string.healthy_plate_fish_value,
    R.string.healthy_plate_white_meat to R.string.healthy_plate_white_meat_value,
    R.string.healthy_plate_eggs to R.string.healthy_plate_eggs_value,
    R.string.healthy_plate_cheese to R.string.healthy_plate_cheese_value,
    R.string.healthy_plate_red_meat to R.string.healthy_plate_red_meat_value,
    R.string.healthy_plate_sweets to R.string.healthy_plate_sweets_week_value,
)

private val Numbers = listOf(
    R.string.healthy_plate_water to R.string.healthy_plate_water_value,
    R.string.healthy_plate_fruit to R.string.healthy_plate_fruit_value,
    R.string.healthy_plate_vegetables_portions to R.string.healthy_plate_vegetables_portions_value,
    R.string.healthy_plate_oil to R.string.healthy_plate_oil_value,
    R.string.healthy_plate_coffee to R.string.healthy_plate_coffee_value,
    R.string.healthy_plate_alcohol to R.string.healthy_plate_alcohol_value,
    R.string.healthy_plate_sweets to R.string.healthy_plate_sweets_value,
)

/** Bottom sheet explaining the healthy plate, from docs/piano-alimentare/linee_guida.md. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthyPlateSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        HealthyPlateContent(onClose = onDismiss)
    }
}

@Composable
fun HealthyPlateContent(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 12.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.healthy_plate_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Section(R.string.healthy_plate_plate_title) {
                val drawing = stringResource(R.string.healthy_plate_drawing)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PlateDrawing(
                        Modifier
                            .size(116.dp)
                            .semantics { contentDescription = drawing },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendItem(
                            LegumesContainer,
                            R.string.healthy_plate_vegetables,
                            R.string.healthy_plate_vegetables_hint,
                        )
                        LegendItem(
                            EggsContainer,
                            R.string.healthy_plate_cereals,
                            R.string.healthy_plate_cereals_hint,
                        )
                        LegendItem(
                            WhiteMeatContainer,
                            R.string.healthy_plate_proteins,
                            R.string.healthy_plate_proteins_hint,
                        )
                    }
                }
                Bullets(
                    R.string.healthy_plate_water_oil,
                    R.string.healthy_plate_colors,
                    R.string.healthy_plate_cereals_every_meal,
                )
            }
            Section(R.string.healthy_plate_week_title) {
                Table(WeeklyFrequencies)
                Text(
                    stringResource(R.string.healthy_plate_week_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Section(R.string.healthy_plate_day_title) {
                Bullets(
                    R.string.healthy_plate_no_skipping,
                    R.string.healthy_plate_skipped_snack,
                    R.string.healthy_plate_breakfast,
                    R.string.healthy_plate_fruit_vegetables,
                    R.string.healthy_plate_hunger,
                )
            }
            Section(R.string.healthy_plate_numbers_title) {
                Table(Numbers)
            }
        }
    }
}

@Composable
private fun Section(@StringRes title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
private fun LegendItem(color: Color, @StringRes title: Int, @StringRes hint: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .padding(top = 3.dp)
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp)),
        )
        Column {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(stringResource(hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Bullets(@StringRes vararg lines: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(line), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Table(rows: List<Pair<Int, Int>>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEach { (label, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(value),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** A round plate: half vegetables and fruit on the left, a quarter each of cereals and proteins. */
@Composable
internal fun PlateDrawing(modifier: Modifier = Modifier) {
    val plate = MaterialTheme.colorScheme.surfaceContainerLowest
    val rim = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        val side = size.minDimension
        drawCircle(plate, radius = side * 0.49f)
        drawCircle(rim, radius = side * 0.49f, style = Stroke(side * 0.015f))
        val inset = side * 0.08f
        val topLeft = Offset(inset, inset)
        val wedge = Size(size.width - 2 * inset, size.height - 2 * inset)
        // Angles start at 3 o'clock and grow clockwise.
        drawArc(LegumesContainer, 90f, 180f, useCenter = true, topLeft = topLeft, size = wedge)
        drawArc(EggsContainer, 270f, 90f, useCenter = true, topLeft = topLeft, size = wedge)
        drawArc(WhiteMeatContainer, 0f, 90f, useCenter = true, topLeft = topLeft, size = wedge)
        val divider = side * 0.02f
        drawLine(
            plate,
            Offset(center.x, inset),
            Offset(center.x, size.height - inset),
            strokeWidth = divider,
        )
        drawLine(plate, center, Offset(size.width - inset, center.y), strokeWidth = divider)
    }
}
