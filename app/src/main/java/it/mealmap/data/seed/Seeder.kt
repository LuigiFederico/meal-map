package it.mealmap.data.seed

import android.content.res.AssetManager
import android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT
import androidx.core.content.contentValuesOf
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Fills the database when it is first created, in the same transaction: the plan's rules and
 * the food alternatives from `assets/seed/foods.json` (built by scripts/build_seed_foods.py).
 * The week stays empty until the user generates one.
 */
class Seeder(private val assets: AssetManager) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        FrequencyRule.DEFAULTS.forEach { (category, rule) ->
            db.insert(
                "frequency_rule",
                "category" to category.name,
                "min" to rule.min,
                "max" to rule.max,
            )
        }
        readFoods().forEach { food ->
            db.insert(
                "food",
                "food_group" to food.group.name,
                "category" to food.category?.name,
                "name" to food.name,
                "portion" to food.portion,
            )
        }
    }

    private fun SupportSQLiteDatabase.insert(table: String, vararg values: Pair<String, Any?>) {
        insert(table, CONFLICT_ABORT, contentValuesOf(*values))
    }

    private fun readFoods(): List<SeedFood> {
        val json = assets.open(FOODS_ASSET).bufferedReader().use { it.readText() }
        return Json.decodeFromString(json)
    }

    @Serializable
    private class SeedFood(
        val group: FoodGroup,
        val category: ProteinCategory?,
        val name: String,
        val portion: String,
    )

    private companion object {
        const val FOODS_ASSET = "seed/foods.json"
    }
}
