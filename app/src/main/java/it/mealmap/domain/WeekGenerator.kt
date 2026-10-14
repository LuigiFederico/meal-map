package it.mealmap.domain

import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.domain.model.Week
import kotlin.random.Random

private const val ATTEMPTS = 50
private const val SAME_DAY_PENALTY = 100
private const val CONSECUTIVE_PENALTY = 3

/**
 * Builds a week keeping the [fixed] meals and filling every other slot with a food meal.
 *
 * Category minimums are covered first, then the remaining slots get random categories still
 * under their maximum. If the free slots cannot cover every minimum, the shortfall stays
 * visible in the balance. Proteins are then arranged so that, whenever the fixed meals allow
 * it, no day has the same protein twice, and consecutive meals (Sunday dinner included, since
 * the week repeats) rarely share one. Carbs are spread evenly and never repeat within a day.
 */
fun generateWeek(
    rules: Map<ProteinCategory, FrequencyRule>,
    fixed: Map<MealSlot, Meal>,
    random: Random = Random.Default,
): Week {
    val free = MealSlot.ALL.filterNot { it in fixed }
    val fixedProteins = fixed.mapValues { (_, meal) -> (meal as? Meal.Food)?.protein }
    val proteins = pickProteins(rules, fixedProteins, free, random)
    val arrangement = arrangeProteins(free, proteins, fixedProteins, random)
    return Week(assignCarbs(fixed, free.zip(arrangement), random))
}

private fun pickProteins(
    rules: Map<ProteinCategory, FrequencyRule>,
    fixedProteins: Map<MealSlot, ProteinCategory?>,
    free: List<MealSlot>,
    random: Random,
): List<ProteinCategory> {
    val counts = ProteinCategory.entries
        .associateWith { category -> fixedProteins.values.count { it == category } }
        .toMutableMap()
    val picked = mutableListOf<ProteinCategory>()
    fun take(category: ProteinCategory) {
        picked += category
        counts[category] = counts.getValue(category) + 1
    }
    fun underMax(category: ProteinCategory): Boolean {
        val max = rules[category]?.max
        return max == null || counts.getValue(category) < max
    }

    // Days where a category can still go without repeating it on the same day.
    val freeDays = free.map { it.day }.toSet()
    val roomPerDay = ProteinCategory.entries.associateWith { category ->
        freeDays.count { day ->
            fixedProteins.none { (slot, p) -> slot.day == day && p == category }
        }
    }
    fun fitsInADay(category: ProteinCategory) =
        picked.count { it == category } < roomPerDay.getValue(category)

    // Every category is at its max: overshoot the one that ends up proportionally least over
    // it. A max of 0 means "never", so those categories come last.
    fun leastOverMax() = ProteinCategory.entries.minBy { category ->
        val max = rules[category]?.max ?: return@minBy 0.0
        (counts.getValue(category) + 1.0) / max
    }

    for (category in ProteinCategory.entries) {
        val missing = (rules[category]?.min ?: 0) - counts.getValue(category)
        repeat(missing.coerceAtMost(free.size - picked.size).coerceAtLeast(0)) { take(category) }
    }
    while (picked.size < free.size) {
        val category =
            ProteinCategory.entries.filter { underMax(it) && fitsInADay(it) }.randomOrNull(random)
                ?: ProteinCategory.entries.filter(::underMax).randomOrNull(random)
                ?: leastOverMax()
        take(category)
    }
    return picked
}

/** Random restarts of a pairwise-swap local search on the repetition penalty. */
private fun arrangeProteins(
    free: List<MealSlot>,
    proteins: List<ProteinCategory>,
    fixedProteins: Map<MealSlot, ProteinCategory?>,
    random: Random,
): List<ProteinCategory> {
    fun penalty(arrangement: List<ProteinCategory>): Int {
        val generated = free.zip(arrangement).toMap()
        return repetitionPenalty { slot -> fixedProteins[slot] ?: generated[slot] }
    }

    var best = proteins
    var bestPenalty = penalty(best)
    repeat(ATTEMPTS) {
        if (bestPenalty == 0) return best
        val candidate = proteins.shuffled(random).toMutableList()
        var candidatePenalty = penalty(candidate)
        var improved = true
        while (improved) {
            improved = false
            for (i in candidate.indices) {
                for (j in i + 1 until candidate.size) {
                    if (candidate[i] == candidate[j]) continue
                    candidate[i] = candidate[j].also { candidate[j] = candidate[i] }
                    val swapped = penalty(candidate)
                    if (swapped < candidatePenalty) {
                        candidatePenalty = swapped
                        improved = true
                    } else {
                        candidate[i] = candidate[j].also { candidate[j] = candidate[i] }
                    }
                }
            }
        }
        if (candidatePenalty < bestPenalty) {
            best = candidate
            bestPenalty = candidatePenalty
        }
    }
    return best
}

private fun repetitionPenalty(proteinOf: (MealSlot) -> ProteinCategory?): Int {
    // The template week repeats, so Sunday dinner is followed by Monday lunch.
    val pairs = MealSlot.ALL.zipWithNext() + (MealSlot.ALL.last() to MealSlot.ALL.first())
    return pairs.sumOf { (a, b) ->
        val protein = proteinOf(a)
        when {
            protein == null || protein != proteinOf(b) -> 0
            a.day == b.day -> SAME_DAY_PENALTY
            else -> CONSECUTIVE_PENALTY
        }
    }
}

private fun assignCarbs(
    fixed: Map<MealSlot, Meal>,
    generated: List<Pair<MealSlot, ProteinCategory>>,
    random: Random,
): Map<MealSlot, Meal> {
    val meals = fixed.toMutableMap()
    val usage = Carb.entries.associateWith { carb ->
        fixed.values.count { (it as? Meal.Food)?.carb == carb }
    }.toMutableMap()
    for ((slot, protein) in generated) {
        val sameDayCarb = (meals[slot.otherOfTheDay()] as? Meal.Food)?.carb
        val candidates = Carb.entries.filter { it != sameDayCarb }
        val leastUsed = candidates.minOf { usage.getValue(it) }
        val carb = candidates.filter { usage.getValue(it) == leastUsed }.random(random)
        usage[carb] = leastUsed + 1
        meals[slot] = Meal.Food(protein, carb)
    }
    return meals
}

private fun MealSlot.otherOfTheDay() = MealSlot(
    day,
    if (slot ==
        Slot.LUNCH
    ) {
        Slot.DINNER
    } else {
        Slot.LUNCH
    },
)
