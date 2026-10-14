package it.mealmap.data

import it.mealmap.data.db.PlannedMealEntity
import it.mealmap.data.db.WeekDao
import it.mealmap.domain.model.MealSlot
import it.mealmap.domain.model.Week
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WeekRepository(private val dao: WeekDao) {
    private val writes = Mutex()

    /** The planned week; null until the first one is generated. */
    val week: Flow<Week?> = dao.observeAll().map { rows ->
        if (rows.size ==
            MealSlot.ALL.size
        ) {
            Week(rows.associate { it.mealSlot to it.toMeal() })
        } else {
            null
        }
    }

    suspend fun save(week: Week) = writes.withLock { write(week) }

    /** Applies [change] to the current week; edits are serialized so quick taps are never lost. */
    suspend fun update(change: (Week) -> Week) = writes.withLock {
        week.first()?.let { write(change(it)) }
    }

    private suspend fun write(week: Week) {
        dao.upsert(MealSlot.ALL.map { PlannedMealEntity.from(it, week[it]) })
    }
}
