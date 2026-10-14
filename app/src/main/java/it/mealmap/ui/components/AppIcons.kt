package it.mealmap.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Outline icons drawn on a 24×24 grid, copied from the design reference. */
object AppIcons {
    val Today = icon("M4 6h16v14H4z M4 10h16 M8 3v4 M16 3v4 M10 13.5h4v3.5h-4z")
    val Week = icon("M4 4h16v16H4z M4 9.3h16 M4 14.7h16 M9.3 4v16 M14.7 4v16")
    val Balance = icon("M3 20h18 M6 20v-8 M12 20V5 M18 20v-5")
    val Rules = icon("M4 7h9 M17 7h3 M15 5v4 M4 17h3 M11 17h9 M9 15v4")
}

private fun icon(pathData: String): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(pathData),
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 1.8f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
).build()
