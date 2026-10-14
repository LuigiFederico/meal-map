package it.mealmap.ui.rules

import it.mealmap.domain.model.FrequencyRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SteppersTest {
    @Test
    fun noMinimumSitsBelowOne() {
        assertEquals(FrequencyRule(min = null), FrequencyRule(min = 1).decreaseMin())
        assertEquals(FrequencyRule(min = 1), FrequencyRule().increaseMin())
        assertNull(FrequencyRule().decreaseMin())
    }

    @Test
    fun theMaxGoesDownToNeverAndCanBeRemoved() {
        assertEquals(FrequencyRule(max = 0), FrequencyRule(max = 1).decreaseMax())
        assertNull(FrequencyRule(max = 0).decreaseMax())
        assertEquals(FrequencyRule(), FrequencyRule(max = 0).removeMax())
        assertNull(FrequencyRule().removeMax())
        assertNull(FrequencyRule().decreaseMax())
        assertEquals(FrequencyRule(max = 1), FrequencyRule().increaseMax())
    }

    @Test
    fun neverLetsTheMinGoAboveTheMax() {
        assertNull(FrequencyRule(min = 2, max = 2).increaseMin())
        assertNull(FrequencyRule(min = 2, max = 2).decreaseMax())
        assertEquals(FrequencyRule(min = 3, max = 3), FrequencyRule(min = 3).increaseMax())
    }
}
