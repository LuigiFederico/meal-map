package it.mealmap.domain.model

/** Weekly portions allowed for a category; null means no bound. */
data class FrequencyRule(val min: Int? = null, val max: Int? = null) {
    init {
        require(min == null || min >= 0) { "min must not be negative" }
        require(max == null || max >= 0) { "max must not be negative" }
        require(min == null || max == null || min <= max) { "min must not exceed max" }
    }

    /** Whether [count] portions go over the max; going under the min is not an excess. */
    fun isExceededBy(count: Int): Boolean = max != null && count > max

    companion object {
        /** Limits from the nutritionist's plan (docs/piano-alimentare/frequenze_settimanali.csv). */
        val DEFAULTS: Map<ProteinCategory, FrequencyRule> = mapOf(
            ProteinCategory.LEGUMES to FrequencyRule(min = 3),
            ProteinCategory.FISH to FrequencyRule(min = 2),
            ProteinCategory.EGGS to FrequencyRule(min = 2, max = 2),
            ProteinCategory.WHITE_MEAT to FrequencyRule(min = 1, max = 2),
            ProteinCategory.RED_MEAT to FrequencyRule(max = 1),
            ProteinCategory.CURED_MEAT to FrequencyRule(max = 2),
            ProteinCategory.CHEESE to FrequencyRule(max = 2),
        )
    }
}
