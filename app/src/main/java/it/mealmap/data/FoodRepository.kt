package it.mealmap.data

import it.mealmap.data.db.FoodDao
import it.mealmap.data.db.FoodEntity
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepository(private val dao: FoodDao) {
    val groupCounts: Flow<Map<FoodGroup, Int>> = dao.observeGroupCounts().map { rows ->
        rows.associate { it.group to it.count }
    }

    fun inGroup(group: FoodGroup): Flow<List<Food>> = dao.observeGroup(group).map { rows ->
        rows.map { it.toFood() }
    }

    fun forCategory(category: ProteinCategory): Flow<List<Food>> =
        dao.observeCategory(category).map { rows -> rows.map { it.toFood() } }

    /** Adds a new food (id 0) or updates an existing one; also restores a deleted food. */
    suspend fun save(food: Food) = dao.upsert(FoodEntity.from(food))

    suspend fun delete(food: Food) = dao.delete(FoodEntity.from(food))
}
