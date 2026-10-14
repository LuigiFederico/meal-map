package it.mealmap.data

import it.mealmap.data.db.FrequencyRuleEntity
import it.mealmap.data.db.RulesDao
import it.mealmap.domain.model.FrequencyRule
import it.mealmap.domain.model.ProteinCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RulesRepository(private val dao: RulesDao) {
    private val writes = Mutex()

    val rules: Flow<Map<ProteinCategory, FrequencyRule>> = dao.observeAll().map { rows ->
        rows.associate { it.category to it.toRule() }
    }

    /**
     * Applies [change] to the stored rule of [category]; a null result leaves it as it is.
     * Changes are serialized, so quick taps on a stepper are never lost.
     */
    suspend fun update(category: ProteinCategory, change: (FrequencyRule) -> FrequencyRule?) =
        writes.withLock {
            val current = rules.first()[category] ?: FrequencyRule()
            change(current)?.let { dao.upsert(listOf(FrequencyRuleEntity.from(category, it))) }
        }

    suspend fun resetToDefaults() = writes.withLock {
        dao.upsert(
            FrequencyRule.DEFAULTS.map { (category, rule) ->
                FrequencyRuleEntity.from(category, rule)
            },
        )
    }
}
