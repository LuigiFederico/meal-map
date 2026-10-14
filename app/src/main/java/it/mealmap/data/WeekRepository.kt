package it.mealmap.data

import it.mealmap.data.db.PlannedMealEntity
import it.mealmap.data.db.WeekDao
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Week
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeekRepository(private val dao: WeekDao) {
    /** The planned week; null until the database has been seeded. */
    val week: Flow<Week?> = dao.observeAll().map { rows ->
        if (rows.size ==
            MealSlot.ALL.size
        ) {
            Week(rows.associate { it.mealSlot to it.toMeal() })
        } else {
            null
        }
    }

    suspend fun save(week: Week) {
        dao.upsert(MealSlot.ALL.map { PlannedMealEntity.from(it, week[it]) })
    }
}
