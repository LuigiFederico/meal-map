package it.mealmap.domain

import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.OtherKind
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.ProteinCategory.CHEESE
import it.mealmap.domain.model.ProteinCategory.CURED_MEAT
import it.mealmap.domain.model.ProteinCategory.EGGS
import it.mealmap.domain.model.ProteinCategory.FISH
import it.mealmap.domain.model.ProteinCategory.LEGUMES
import it.mealmap.domain.model.ProteinCategory.RED_MEAT
import it.mealmap.domain.model.ProteinCategory.WHITE_MEAT
import it.mealmap.domain.model.Slot
import it.mealmap.domain.model.Week
import java.time.DayOfWeek

fun food(protein: ProteinCategory, carb: Carb = Carb.PASTA, locked: Boolean = false) =
    Meal.Food(protein, carb, locked)

fun slot(day: DayOfWeek, slot: Slot) = MealSlot(day, slot)

/** The sample week of the design: red meat is over its limit. */
val sampleWeek = Week(
    MealSlot.ALL.zip(
        listOf(
            food(EGGS, Carb.BREAD), food(LEGUMES, Carb.RICE),
            Meal.Other(OtherKind.CANTEEN), food(RED_MEAT, Carb.BARLEY),
            food(CURED_MEAT, Carb.PIADINA), food(LEGUMES, Carb.PASTA),
            food(LEGUMES, Carb.SPELT), food(WHITE_MEAT, Carb.BREAD),
            food(EGGS, Carb.POTATOES), food(FISH, Carb.PASTA, locked = true),
            food(RED_MEAT, Carb.PASTA), Meal.Other(OtherKind.FREE_CHOICE),
            food(CHEESE, Carb.POLENTA), food(FISH, Carb.POTATOES),
        ),
    ).toMap(),
)
