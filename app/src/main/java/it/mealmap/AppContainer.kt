package it.mealmap

import android.content.Context
import androidx.room.Room
import it.mealmap.data.AppUpdater
import it.mealmap.data.FoodRepository
import it.mealmap.data.RulesRepository
import it.mealmap.data.WeekRepository
import it.mealmap.data.db.AppDatabase
import it.mealmap.data.seed.Seeder

/** Manual dependency injection: the app-wide database, repositories and updater. */
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "meal-map.db")
        .addCallback(Seeder(context.assets))
        .build()

    val weekRepository = WeekRepository(database.weekDao())
    val rulesRepository = RulesRepository(database.rulesDao())
    val foodRepository = FoodRepository(database.foodDao())
    val appUpdater = AppUpdater(context)
}
