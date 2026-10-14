package it.mealmap.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekDao {
    @Query("SELECT * FROM planned_meal")
    fun observeAll(): Flow<List<PlannedMealEntity>>

    @Upsert
    suspend fun upsert(meals: List<PlannedMealEntity>)
}

@Dao
interface RulesDao {
    @Query("SELECT * FROM frequency_rule")
    fun observeAll(): Flow<List<FrequencyRuleEntity>>

    @Query("SELECT COUNT(*) FROM frequency_rule")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(rules: List<FrequencyRuleEntity>)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food WHERE food_group = :group ORDER BY name COLLATE NOCASE")
    fun observeGroup(group: FoodGroup): Flow<List<FoodEntity>>

    @Query("SELECT * FROM food WHERE category = :category ORDER BY name COLLATE NOCASE")
    fun observeCategory(category: ProteinCategory): Flow<List<FoodEntity>>

    @Query("SELECT food_group AS `group`, COUNT(*) AS count FROM food GROUP BY food_group")
    fun observeGroupCounts(): Flow<List<GroupCount>>

    @Insert
    suspend fun insertAll(foods: List<FoodEntity>)

    @Upsert
    suspend fun upsert(food: FoodEntity)

    @Delete
    suspend fun delete(food: FoodEntity)
}

data class GroupCount(val group: FoodGroup, val count: Int)
