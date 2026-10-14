package it.mealmap.data

/** The three daily moments that are the same every day and are never generated. */
enum class ReminderMoment {
    BREAKFAST,
    SNACK,
    AFTERNOON_SNACK,
}

data class ReminderItem(val name: String, val quantity: String? = null)

/** A numbered option; options with the same number go well together across the day. */
data class ReminderOption(val number: Int, val items: List<ReminderItem>)

/** From the nutritionist's plan (docs/piano-alimentare/settimana_tipo.md). */
val Reminders: Map<ReminderMoment, List<ReminderOption>> = mapOf(
    ReminderMoment.BREAKFAST to listOf(
        ReminderOption(1, listOf(ReminderItem("Pane", "40 g"), ReminderItem("Frutto", "1"))),
        ReminderOption(
            2,
            listOf(
                ReminderItem("Caffè o tè"),
                ReminderItem("Biscotti a scelta", "4-5"),
                ReminderItem("Latte o yogurt", "1 bicchiere"),
            ),
        ),
        ReminderOption(
            3,
            listOf(
                ReminderItem("Cereali a scelta", "5-6 cucchiai"),
                ReminderItem("Yogurt o latte", "1 vasetto"),
                ReminderItem("Frutta", "a piacere"),
                ReminderItem("Frutta secca", "20 g"),
            ),
        ),
        ReminderOption(
            4,
            listOf(
                ReminderItem("Fette biscottate", "3-4 fette"),
                ReminderItem("Latte o yogurt", "1 bicchiere"),
                ReminderItem("Marmellata", "5-6 cucchiaini"),
            ),
        ),
    ),
    ReminderMoment.SNACK to listOf(
        ReminderOption(
            1,
            listOf(ReminderItem("Cracker", "1 pacchetto"), ReminderItem("Frutta secca")),
        ),
        ReminderOption(4, listOf(ReminderItem("Frutta secca"))),
    ),
    ReminderMoment.AFTERNOON_SNACK to listOf(
        ReminderOption(1, listOf(ReminderItem("Dolcetto"), ReminderItem("Yogurt", "1 vasetto"))),
        ReminderOption(2, listOf(ReminderItem("Wasa", "2-3"), ReminderItem("Grana", "20 g"))),
    ),
)
