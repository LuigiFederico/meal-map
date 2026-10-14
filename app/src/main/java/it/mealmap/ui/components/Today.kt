package it.mealmap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import java.time.DayOfWeek
import java.time.LocalDate

/** Today's day of the week, refreshed whenever the app comes back to the foreground. */
@Composable
fun rememberToday(): DayOfWeek {
    var today by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    LifecycleResumeEffect(Unit) {
        today = LocalDate.now().dayOfWeek
        onPauseOrDispose { }
    }
    return today
}
