package it.mealmap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import it.mealmap.R

@OptIn(ExperimentalTextApi::class)
private fun robotoFlex(weight: FontWeight) = Font(
    resId = R.font.roboto_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val RobotoFlex = FontFamily(
    robotoFlex(FontWeight.Normal),
    robotoFlex(FontWeight.Medium),
    robotoFlex(FontWeight.SemiBold),
    robotoFlex(FontWeight.Bold),
)

private val Default = Typography()

private fun TextStyle.flex() = copy(fontFamily = RobotoFlex)

val MealMapTypography = Typography(
    displayLarge = Default.displayLarge.flex(),
    displayMedium = Default.displayMedium.flex(),
    displaySmall = Default.displaySmall.flex(),
    headlineLarge = Default.headlineLarge.flex(),
    headlineMedium = Default.headlineMedium.flex(),
    headlineSmall = Default.headlineSmall.flex(),
    titleLarge = Default.titleLarge.flex(),
    titleMedium = Default.titleMedium.flex(),
    titleSmall = Default.titleSmall.flex(),
    bodyLarge = Default.bodyLarge.flex(),
    bodyMedium = Default.bodyMedium.flex(),
    bodySmall = Default.bodySmall.flex(),
    labelLarge = Default.labelLarge.flex(),
    labelMedium = Default.labelMedium.flex(),
    labelSmall = Default.labelSmall.flex(),
)
