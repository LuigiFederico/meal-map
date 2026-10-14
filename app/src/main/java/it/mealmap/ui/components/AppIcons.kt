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
    val Settings = icon(
        "M9 12a3 3 0 1 0 6 0 3 3 0 1 0-6 0 " +
            "M10.3 3.5h3.4l.6 2.4 1.6.9 2.4-.7 1.7 2.9-1.8 1.7v2.6l1.8 1.7-1.7 2.9-2.4-.7-1.6.9" +
            "-.6 2.4h-3.4l-.6-2.4-1.6-.9-2.4.7-1.7-2.9 1.8-1.7v-2.6L4 9l1.7-2.9 2.4.7 1.6-.9z",
    )

    val Legumes = icon(
        "M4.5 15.5C7 9 12 5 19.5 4.5 19 12 14.5 17.5 8.5 19.5c-2.5.8-4.8-1.5-4-4z " +
            "M8 15a1.4 1.4 0 1 0 2.8 0 1.4 1.4 0 1 0-2.8 0 " +
            "M11.5 11.5a1.4 1.4 0 1 0 2.8 0 1.4 1.4 0 1 0-2.8 0 " +
            "M15 8a1.4 1.4 0 1 0 2.8 0 1.4 1.4 0 1 0-2.8 0",
    )
    val Fish = icon(
        "M6 12c2.5-3.3 5.4-5 8.5-5 3 0 5.2 2 6.5 5-1.3 3-3.5 5-6.5 5-3.1 0-6-1.7-8.5-5z " +
            "M6 12 2.5 8.5v7z M16.5 10.8v.01",
    )
    val Eggs = icon("M12 3c3.6 0 6.5 5.6 6.5 10a6.5 6.5 0 0 1-13 0C5.5 8.6 8.4 3 12 3z")
    val WhiteMeat = icon(
        "M14.5 3.5c3.3 0 6 2.7 6 6 0 3.6-3.4 5.5-6.6 5.5l-3.6 3.6a2 2 0 1 1-2.6 2.3 " +
            "2 2 0 1 1-2.3-2.6l3.6-3.6c0-3.2 1.9-11.2 5.5-11.2z",
    )
    val RedMeat = icon(
        "M4.5 10c0-3.6 3.4-6 7.8-6 4.6 0 7.7 2.6 7.7 6.5 0 5-3.4 9.5-9 9.5-3.7 0-6.5-2-6.5-4.7 " +
            "0-1.8 1.4-2.4 1.4-3.4S4.5 11.2 4.5 10z M13.5 10.5a1.6 1.6 0 1 0 3.2 0 1.6 1.6 0 1 0-3.2 0",
    )
    val CuredMeat = icon("M3 12a9 9 0 1 0 18 0 9 9 0 1 0-18 0 M8 12a4 4 0 1 0 8 0 4 4 0 1 0-8 0")
    val Cheese = icon("M3 18v-6.5L15 5l6 6.5V18z M3 11.5h18 M8 15v.01 M15.5 14.5v.01")

    val Canteen = icon("M3 20h18 M5 20V9l7-4.5L19 9v11 M9.5 20v-5h5v5")
    val EatingOut = icon("M7 3v18 M4.5 3v5a2.5 2.5 0 0 0 5 0V3 M17.5 21V3c-2.2 0-3.5 3-3.5 7h3.5")
    val FreeChoice = icon("M4.5 4.5h15v15h-15z M9 9v.01 M15 15v.01 M12 12v.01 M15 9v.01 M9 15v.01")

    val Carb = icon(
        "M12 21V9 M12 9c-2.5 0-4-1.5-4-4 2.5 0 4 1.5 4 4z M12 9c2.5 0 4-1.5 4-4-2.5 0-4 1.5-4 4z " +
            "M12 14c-2.5 0-4-1.5-4-4 2.5 0 4 1.5 4 4z M12 14c2.5 0 4-1.5 4-4-2.5 0-4 1.5-4 4z",
    )
    val Vegetables = icon("M5 19c0-8.3 5.2-14 14-14 0 8.8-5.7 14-14 14z M5 19l8.5-8.5")

    val Lock = icon("M6 11h12v9H6z M8.5 11V8a3.5 3.5 0 0 1 7 0v3")
    val Unlock = icon("M6 11h12v9H6z M8.5 11V8a3.5 3.5 0 0 1 6.8-1.2")
    val Close = icon("M6 6l12 12 M18 6 6 18")
    val Edit = icon("M4 20h4L19 9l-4-4L4 16z")
    val Swap = icon("M7 4 3 8l4 4 M3 8h14 M17 20l4-4-4-4 M21 16H7")
    val Generate = icon(
        "M12 3.5v3 M12 17.5v3 M3.5 12h3 M17.5 12h3 M6 6l2 2 M16 16l2 2 M18 6l-2 2 M8 16l-2 2",
    )
    val Minus = icon("M5 12h14")
    val Plus = icon("M12 5v14 M5 12h14")
    val Reset = icon("M4 12a8 8 0 1 0 2.3-5.7 M4 4v5h5")
    val Back = icon("M19 12H5 M11 6l-6 6 6 6")
    val Search = icon("M4 11a7 7 0 1 0 14 0 7 7 0 1 0-14 0 M20 20l-4-4")
    val Trash = icon("M4 7h16 M9 7V4h6v3 M6 7l1 13h10l1-13")
    val Drag = icon("M9 6v.01 M15 6v.01 M9 12v.01 M15 12v.01 M9 18v.01 M15 18v.01")
    val Link = icon(
        "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1 " +
            "M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1",
    )

    val Breakfast = icon(
        "M4 9h12v4.5A5.5 5.5 0 0 1 10.5 19h-1A5.5 5.5 0 0 1 4 13.5z " +
            "M16 10.5h1.5a2.5 2.5 0 0 1 0 5H16 M8 3.5v2.5 M12 3.5v2.5",
    )
    val Snack = icon(
        "M12 7.5c-2-1.8-7-1.4-7 3.8 0 4 2.8 8.7 4.8 8.7 1 0 1.4-.5 2.2-.5s1.2.5 2.2.5 " +
            "c2 0 4.8-4.7 4.8-8.7 0-5.2-5-5.6-7-3.8z M12 7.5c0-2 1-3.5 3-4",
    )
    val AfternoonSnack = icon("M5.5 9h13l-1.5 11h-10z M4.5 9h15 M8.5 4.5h7l1 4.5h-9z")
    val Warning = icon("M12 4 2.5 20h19z M12 10v4.5 M12 17.2v.01")
    val Check = icon("M5 12.5l4.5 4.5L19 7.5")
    val ChevronRight = icon("M9 6l6 6-6 6")
    val Info = icon("M3 12a9 9 0 1 0 18 0 9 9 0 1 0-18 0 M12 11v5 M12 8v.01")
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
