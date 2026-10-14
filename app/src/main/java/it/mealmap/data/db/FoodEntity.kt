package it.mealmap.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
) {
    fun toFood() = Food(id, group, category, name, portion)

    companion object {
        fun from(food: Food) =
            FoodEntity(food.id, food.group, food.category, food.name, food.portion)
    }
}
