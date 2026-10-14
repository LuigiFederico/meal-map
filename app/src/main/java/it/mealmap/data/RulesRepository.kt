package it.mealmap.data

import it.mealmap.data.db.FrequencyRuleEntity
import it.mealmap.data.db.RulesDao
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RulesRepository(private val dao: RulesDao) {
    val rules: Flow<Map<ProteinCategory, FrequencyRule>> = dao.observeAll().map { rows ->
        rows.associate { it.category to it.toRule() }
    }

    suspend fun update(category: ProteinCategory, rule: FrequencyRule) {
        dao.upsert(listOf(FrequencyRuleEntity.from(category, rule)))
    }

    suspend fun resetToDefaults() {
        dao.upsert(
            FrequencyRule.DEFAULTS.map { (category, rule) ->
                FrequencyRuleEntity.from(category, rule)
            },
        )
    }
}
