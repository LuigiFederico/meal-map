package it.mealmap.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import it.mealmap.R
import it.mealmap.ui.components.AppIcons
import kotlinx.serialization.Serializable

@Serializable data object TodayRoute

@Serializable data object WeekRoute

@Serializable data object BalanceRoute

@Serializable data object SettingsRoute

/** Full-screen editor of one meal; [slot] is its index in [it.mealmap.domain.model.MealSlot.ALL]. */
@Serializable data class EditMealRoute(val slot: Int)

@Serializable data object NewWeekRoute

/** The alternatives of a [FoodGroup], by name: the domain enum stays free of Android annotations. */
@Serializable data class FoodListRoute(val group: String)

/** The four tabs of the bottom navigation bar. */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    TODAY(TodayRoute, R.string.tab_today, AppIcons.Today),
    WEEK(WeekRoute, R.string.tab_week, AppIcons.Week),
    BALANCE(BalanceRoute, R.string.tab_balance, AppIcons.Balance),
    SETTINGS(SettingsRoute, R.string.tab_settings, AppIcons.Settings),
}
