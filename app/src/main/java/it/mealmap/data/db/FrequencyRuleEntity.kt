package it.mealmap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory

@Entity(tableName = "frequency_rule")
data class FrequencyRuleEntity(
    @PrimaryKey val category: ProteinCategory,
    val min: Int?,
    val max: Int?,
) {
    fun toRule() = FrequencyRule(min, max)

    companion object {
        fun from(category: ProteinCategory, rule: FrequencyRule) =
            FrequencyRuleEntity(category, rule.min, rule.max)
    }
}
