package it.mealmap.ui.rules

import it.mealmap.domain.model.FrequencyRule

// Steps of the min and max steppers. Each returns the new rule, or null when the step is not
// allowed; every step keeps min ≤ max.

/** No minimum sits below 1: "−" from 1 clears it. */
fun FrequencyRule.decreaseMin(): FrequencyRule? = min?.let {
    if (it <=
        1
    ) {
        copy(min = null)
    } else {
        copy(min = it - 1)
    }
}

fun FrequencyRule.increaseMin(): FrequencyRule? {
    val next = (min ?: 0) + 1
    return if (max != null && next > max) null else copy(min = next)
}

/** Down to 0 ("never"), but not below the min; without a max there is nothing to lower. */
fun FrequencyRule.decreaseMax(): FrequencyRule? = max?.let {
    if (it - 1 <
        (min ?: 0)
    ) {
        null
    } else {
        copy(max = it - 1)
    }
}

/** From "no limit", sets the tightest limit the min allows. */
fun FrequencyRule.increaseMax(): FrequencyRule = copy(max = max?.plus(1) ?: maxOf(1, min ?: 0))

fun FrequencyRule.removeMax(): FrequencyRule? = if (max == null) null else copy(max = null)
