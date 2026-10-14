package it.mealmap.domain

import it.mealmap.domain.model.Food
import it.mealmap.domain.model.FoodGroup
import it.mealmap.domain.model.ProteinCategory
import org.junit.Assert.assertThrows
import org.junit.Test

class FoodTest {
    private fun food(group: FoodGroup, category: ProteinCategory?) =
        Food(group = group, category = category, name = "x", portion = "1")

    @Test
    fun acceptsTheCategoryOfItsGroup() {
        food(FoodGroup.FISH, ProteinCategory.FISH)
        food(FoodGroup.CARBS, null)
        food(FoodGroup.MEAT, ProteinCategory.CURED_MEAT)
    }

    @Test
    fun rejectsACategoryFromAnotherGroup() {
        assertThrows(IllegalArgumentException::class.java) {
            food(FoodGroup.FISH, ProteinCategory.LEGUMES)
        }
        assertThrows(IllegalArgumentException::class.java) { food(FoodGroup.MEAT, null) }
        assertThrows(IllegalArgumentException::class.java) {
            food(FoodGroup.MEAT, ProteinCategory.FISH)
        }
    }
}
