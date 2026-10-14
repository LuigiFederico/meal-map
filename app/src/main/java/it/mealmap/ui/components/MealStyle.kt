package it.mealmap.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import it.mealmap.R
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.ui.theme.CheeseContainer
import it.mealmap.ui.theme.CuredMeatContainer
import it.mealmap.ui.theme.EggsContainer
import it.mealmap.ui.theme.FishContainer
import it.mealmap.ui.theme.LegumesContainer
import it.mealmap.ui.theme.OnCheese
import it.mealmap.ui.theme.OnCuredMeat
import it.mealmap.ui.theme.OnEggs
import it.mealmap.ui.theme.OnFish
import it.mealmap.ui.theme.OnLegumes
import it.mealmap.ui.theme.OnRedMeat
import it.mealmap.ui.theme.OnWhiteMeat
import it.mealmap.ui.theme.RedMeatContainer
import it.mealmap.ui.theme.WhiteMeatContainer
import java.time.DayOfWeek
import java.util.Locale

// How domain values look in the UI: labels, icons and colors.

@get:StringRes
val ProteinCategory.label: Int
    get() = when (this) {
        ProteinCategory.LEGUMES -> R.string.category_legumes
        ProteinCategory.FISH -> R.string.category_fish
        ProteinCategory.EGGS -> R.string.category_eggs
        ProteinCategory.WHITE_MEAT -> R.string.category_white_meat
        ProteinCategory.RED_MEAT -> R.string.category_red_meat
        ProteinCategory.CURED_MEAT -> R.string.category_cured_meat
        ProteinCategory.CHEESE -> R.string.category_cheese
    }

val ProteinCategory.icon: ImageVector
    get() = when (this) {
        ProteinCategory.LEGUMES -> AppIcons.Legumes
        ProteinCategory.FISH -> AppIcons.Fish
        ProteinCategory.EGGS -> AppIcons.Eggs
        ProteinCategory.WHITE_MEAT -> AppIcons.WhiteMeat
        ProteinCategory.RED_MEAT -> AppIcons.RedMeat
        ProteinCategory.CURED_MEAT -> AppIcons.CuredMeat
        ProteinCategory.CHEESE -> AppIcons.Cheese
    }

val ProteinCategory.containerColor: Color
    get() = when (this) {
        ProteinCategory.LEGUMES -> LegumesContainer
        ProteinCategory.FISH -> FishContainer
        ProteinCategory.EGGS -> EggsContainer
        ProteinCategory.WHITE_MEAT -> WhiteMeatContainer
        ProteinCategory.RED_MEAT -> RedMeatContainer
        ProteinCategory.CURED_MEAT -> CuredMeatContainer
        ProteinCategory.CHEESE -> CheeseContainer
    }

val ProteinCategory.contentColor: Color
    get() = when (this) {
        ProteinCategory.LEGUMES -> OnLegumes
        ProteinCategory.FISH -> OnFish
        ProteinCategory.EGGS -> OnEggs
        ProteinCategory.WHITE_MEAT -> OnWhiteMeat
        ProteinCategory.RED_MEAT -> OnRedMeat
        ProteinCategory.CURED_MEAT -> OnCuredMeat
        ProteinCategory.CHEESE -> OnCheese
    }

@get:StringRes
val OtherKind.label: Int
    get() = when (this) {
        OtherKind.CANTEEN -> R.string.other_canteen
        OtherKind.EATING_OUT -> R.string.other_eating_out
        OtherKind.FREE_CHOICE -> R.string.other_free_choice
    }

val OtherKind.icon: ImageVector
    get() = when (this) {
        OtherKind.CANTEEN -> AppIcons.Canteen
        OtherKind.EATING_OUT -> AppIcons.EatingOut
        OtherKind.FREE_CHOICE -> AppIcons.FreeChoice
    }

@get:StringRes
val Carb.label: Int
    get() = when (this) {
        Carb.BREAD -> R.string.carb_bread
        Carb.PASTA -> R.string.carb_pasta
        Carb.RICE -> R.string.carb_rice
        Carb.POTATOES -> R.string.carb_potatoes
        Carb.PIADINA -> R.string.carb_piadina
        Carb.SPELT -> R.string.carb_spelt
        Carb.BARLEY -> R.string.carb_barley
        Carb.POLENTA -> R.string.carb_polenta
        Carb.GNOCCHI -> R.string.carb_gnocchi
        Carb.FOCACCIA -> R.string.carb_focaccia
    }

@get:StringRes
val Slot.label: Int
    get() = when (this) {
        Slot.LUNCH -> R.string.slot_lunch
        Slot.DINNER -> R.string.slot_dinner
    }

@Composable
fun dayName(day: DayOfWeek): String = stringArrayResource(R.array.days)[day.ordinal]

@Composable
fun dayShortName(day: DayOfWeek): String = stringArrayResource(R.array.days_short)[day.ordinal]

/** "martedì cena", or "Martedì cena" at the start of a sentence. */
@Composable
fun slotPhrase(slot: MealSlot, capitalized: Boolean = false): String {
    val phrase = "${dayName(slot.day)} ${stringResource(slot.slot.label)}".lowercase(Locale.ITALIAN)
    return if (capitalized) phrase.replaceFirstChar { it.titlecase(Locale.ITALIAN) } else phrase
}

/** Spoken description of a meal, e.g. "Carne rossa, pasta, verdure, bloccato". */
@Composable
fun mealDescription(meal: Meal): String = when (meal) {
    is Meal.Other -> stringResource(meal.kind.label)
    is Meal.Food -> listOfNotNull(
        stringResource(meal.protein.label),
        stringResource(meal.carb.label).lowercase(Locale.ITALIAN),
        stringResource(R.string.vegetables).lowercase(Locale.ITALIAN),
        stringResource(R.string.meal_locked).takeIf { meal.locked },
    ).joinToString(", ")
}

/** Title of a meal, e.g. "Legumi, verdure, pasta" or "Mensa". */
@Composable
fun mealTitle(meal: Meal): String = when (meal) {
    is Meal.Other -> stringResource(meal.kind.label)
    is Meal.Food -> stringResource(
        R.string.meal_line,
        stringResource(meal.protein.label),
        stringResource(meal.carb.label).lowercase(Locale.ITALIAN),
    )
}
