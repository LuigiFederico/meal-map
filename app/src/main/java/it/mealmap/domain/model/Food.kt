package it.mealmap.domain.model

/** A food alternative with its portion, as listed in the nutritionist's plan. */
data class Food(
    val id: Long = 0,
    val group: FoodGroup,
    /** Protein category the food counts as; null for carbs, breakfast and snacks. */
    val category: ProteinCategory?,
    val name: String,
    val portion: String,
    /** For carbs, the meal carbs this portion can replace; empty for side portions and mixes. */
    val carbs: Set<Carb> = emptySet(),
) {
    init {
        val valid = if (group.categories.isEmpty()) {
            category == null
        } else {
            category in
                group.categories
        }
        require(valid) { "$category does not belong to $group" }
        require(carbs.isEmpty() || group == FoodGroup.CARBS) {
            "only carbs can replace a meal carb"
        }
    }
}

/** Lists of alternatives and the protein categories their foods count as; meat is split into three. */
enum class FoodGroup(val categories: List<ProteinCategory>) {
    LEGUMES(listOf(ProteinCategory.LEGUMES)),
    FISH(listOf(ProteinCategory.FISH)),
    MEAT(listOf(ProteinCategory.WHITE_MEAT, ProteinCategory.RED_MEAT, ProteinCategory.CURED_MEAT)),
    EGGS(listOf(ProteinCategory.EGGS)),
    CHEESE(listOf(ProteinCategory.CHEESE)),
    CARBS(emptyList()),
    BREAKFAST(emptyList()),
    SNACKS(emptyList()),
}
