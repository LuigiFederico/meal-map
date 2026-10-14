package it.mealmap.domain

import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Week

enum class BalanceStatus {
    OK,
    UNDER,
    OVER,
}

data class CategoryBalance(
    val category: ProteinCategory,
    val rule: FrequencyRule,
    val slots: List<MealSlot>,
) {
    val count: Int get() = slots.size

    val status: BalanceStatus
        get() = when {
            rule.isExceededBy(count) -> BalanceStatus.OVER
            rule.min != null && count < rule.min -> BalanceStatus.UNDER
            else -> BalanceStatus.OK
        }
}

/** Weekly portions per category; only lunches and dinners with a category count. */
data class Balance(val categories: List<CategoryBalance>, val uncountedMeals: Int) {
    val overLimit: List<CategoryBalance> get() = categories.filter {
        it.status == BalanceStatus.OVER
    }
}

fun Week.balance(rules: Map<ProteinCategory, FrequencyRule>): Balance {
    val slotsByCategory = MealSlot.ALL
        .mapNotNull { slot -> (this[slot] as? Meal.Food)?.let { it.protein to slot } }
        .groupBy({ it.first }, { it.second })
    return Balance(
        categories = ProteinCategory.entries.map { category ->
            CategoryBalance(
                category = category,
                rule = rules[category] ?: FrequencyRule(),
                slots = slotsByCategory[category].orEmpty(),
            )
        },
        uncountedMeals = MealSlot.ALL.size - slotsByCategory.values.sumOf { it.size },
    )
}
