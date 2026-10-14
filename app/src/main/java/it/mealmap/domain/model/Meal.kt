package it.mealmap.domain.model

/** What is planned for one lunch or dinner. */
sealed interface Meal {
    /** A protein and a carb, always with vegetables. Counts in the balance. */
    data class Food(val protein: ProteinCategory, val carb: Carb, val locked: Boolean = false) :
        Meal

    /** A meal with no category, outside the balance. */
    data class Other(val kind: OtherKind) : Meal
}

enum class OtherKind {
    CANTEEN,
    EATING_OUT,
    FREE_CHOICE,
}
