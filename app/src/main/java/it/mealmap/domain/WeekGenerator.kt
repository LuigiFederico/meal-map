package it.mealmap.domain

import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.Meal
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.ProteinCategory
import it.mealmap.domain.model.Slot
import it.mealmap.domain.model.Week
import java.time.DayOfWeek
import kotlin.random.Random

private const val ATTEMPTS = 50
private const val SAME_DAY_PENALTY = 100
private const val CONSECUTIVE_DAY_PENALTY = 10

/**
 * Builds a week keeping the [fixed] meals and filling every other slot with a food meal.
 *
 * Category minimums are covered first, then the remaining slots get random categories still
 * under their maximum, preferring those that still fit on non-consecutive days. At most one
 * slot may go to a category that would then land on two consecutive days, so that limited
 * categories are not all pushed to their max. If the free slots cannot cover every minimum,
 * the shortfall stays visible in the balance. Proteins are then arranged so that, whenever the
 * fixed meals allow it, no day has the same protein twice and a protein eaten one day is not
 * eaten the next (Sunday to Monday included, since the week repeats). Carbs are spread evenly
 * and never repeat within a day.
 */
fun generateWeek(
    rules: Map<ProteinCategory, FrequencyRule>,
    fixed: Map<MealSlot, Meal>,
    random: Random = Random.Default,
): Week {
    val free = MealSlot.ALL.filterNot { it in fixed }
    val fixedProteins = fixed.mapValues { (_, meal) -> (meal as? Meal.Food)?.protein }
    val dayRoom = dayRoom(fixedProteins, free)
    val proteins = pickProteins(rules, fixedProteins, free, dayRoom, random)
    val arrangement = arrangeProteins(free, proteins, fixedProteins, dayRoom, random)
    return Week(assignCarbs(fixed, free.zip(arrangement), random))
}

/**
 * How many free days each category can still take without being eaten on consecutive days,
 * counting the days where a fixed meal already has it. A week has 7 days, so trying every
 * subset is cheap and exact.
 */
private fun dayRoom(
    fixedProteins: Map<MealSlot, ProteinCategory?>,
    free: List<MealSlot>,
): Map<ProteinCategory, Int> {
    val freeDays = free.map { it.day }.toSet()
    return ProteinCategory.entries.associateWith { category ->
        val fixedDays = fixedProteins.filterValues { it == category }.keys.map { it.day }.toSet()
        val candidates = (freeDays - fixedDays).toList()
        (0 until (1 shl candidates.size)).maxOf { mask ->
            val days = candidates.filterIndexed { i, _ -> mask shr i and 1 == 1 }
            val taken = days + fixedDays
            val apart = days.all { day -> taken.none { it.isNextTo(day) } }
            if (apart) days.size else 0
        }
    }
}

private fun pickProteins(
    rules: Map<ProteinCategory, FrequencyRule>,
    fixedProteins: Map<MealSlot, ProteinCategory?>,
    free: List<MealSlot>,
    dayRoom: Map<ProteinCategory, Int>,
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
    fun spreadsOut(category: ProteinCategory) =
        picked.count { it == category } < dayRoom.getValue(category)

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
    // One slot may repeat a category on consecutive days, so that the extra slot can go to any
    // category under its max instead of filling up the limited ones.
    var repeatAllowed = true
    while (picked.size < free.size) {
        val fitting = ProteinCategory.entries.filter { underMax(it) && fitsInADay(it) }
        val spread = fitting.filter(::spreadsOut)
        val repeating = if (repeatAllowed) fitting - spread.toSet() else emptyList()
        val category = (spread + repeating).randomOrNull(random)
            ?.also { if (it in repeating) repeatAllowed = false }
            ?: fitting.randomOrNull(random)
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
    dayRoom: Map<ProteinCategory, Int>,
    random: Random,
): List<ProteinCategory> {
    // No arrangement goes below this: the fixed meals' own repeats, plus one consecutive-day
    // repeat (or a worse same-day one) for each slot of a category beyond the days it can
    // spread over.
    val floor = repetitionPenalty { fixedProteins[it] } +
        ProteinCategory.entries.sumOf { category ->
            val excess = proteins.count { it == category } - dayRoom.getValue(category)
            minOf(SAME_DAY_PENALTY, excess.coerceAtLeast(0) * CONSECUTIVE_DAY_PENALTY)
        }
    fun penalty(arrangement: List<ProteinCategory>): Int {
        val generated = free.zip(arrangement).toMap()
        return repetitionPenalty { slot -> fixedProteins[slot] ?: generated[slot] }
    }

    var best = proteins
    var bestPenalty = penalty(best)
    repeat(ATTEMPTS) {
        if (bestPenalty <= floor) return best
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
    fun proteinsOf(day: DayOfWeek) = Slot.entries.mapNotNull { proteinOf(MealSlot(day, it)) }
    return DayOfWeek.entries.sumOf { day ->
        val today = proteinsOf(day)
        // The template week repeats, so Sunday is followed by Monday.
        val tomorrow = proteinsOf(day.plus(1))
        (today.size - today.distinct().size) * SAME_DAY_PENALTY +
            today.distinct().count { it in tomorrow } * CONSECUTIVE_DAY_PENALTY
    }
}

private fun DayOfWeek.isNextTo(other: DayOfWeek) = plus(1) == other || other.plus(1) == this

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
