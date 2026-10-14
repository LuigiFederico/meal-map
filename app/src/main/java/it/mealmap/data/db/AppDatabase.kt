package it.mealmap.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlannedMealEntity::class, FrequencyRuleEntity::class, FoodEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weekDao(): WeekDao

    abstract fun rulesDao(): RulesDao

    abstract fun foodDao(): FoodDao
}
