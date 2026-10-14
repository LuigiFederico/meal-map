package it.mealmap.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.mealmap.domain.model.Carb
import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory

@Entity(tableName = "food", indices = [Index("food_group"), Index("category")])
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "food_group") val group: FoodGroup,
    val category: ProteinCategory?,
    val name: String,
    val portion: String,
    /** Comma-separated [Carb] names. */
    val carbs: String,
) {
    fun toFood() = Food(id, group, category, name, portion, decodeCarbs(carbs))

    companion object {
        fun from(food: Food) = FoodEntity(
            food.id,
            food.group,
            food.category,
            food.name,
            food.portion,
            encodeCarbs(food.carbs),
        )

        fun encodeCarbs(carbs: Set<Carb>): String = carbs.joinToString(",") { it.name }

        private fun decodeCarbs(carbs: String): Set<Carb> =
            if (carbs.isEmpty()) emptySet() else carbs.split(",").map(Carb::valueOf).toSet()
    }
}
