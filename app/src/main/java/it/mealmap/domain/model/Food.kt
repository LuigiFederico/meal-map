package it.mealmap.domain.model

/** A food alternative with its portion, as listed in the nutritionist's plan. */
data class Food(
    val id: Long = 0,
    val group: FoodGroup,
    /** Protein category the food counts as; null for carbs, breakfast and snacks. */
    val category: ProteinCategory?,
    val name: String,
    val portion: String,
) {
    init {
        val valid = when (group) {
            FoodGroup.MEAT -> category in MEAT_CATEGORIES
            else -> category == group.category
        }
        require(valid) { "$category does not belong to $group" }
    }
}

private val MEAT_CATEGORIES =
    setOf(ProteinCategory.WHITE_MEAT, ProteinCategory.RED_MEAT, ProteinCategory.CURED_MEAT)

/** Lists of alternatives, each with its protein category; meat is split into three. */
enum class FoodGroup(val category: ProteinCategory?) {
    LEGUMES(ProteinCategory.LEGUMES),
    FISH(ProteinCategory.FISH),
    MEAT(null),
    EGGS(ProteinCategory.EGGS),
    CHEESE(ProteinCategory.CHEESE),
    CARBS(null),
    BREAKFAST(null),
    SNACKS(null),
}
