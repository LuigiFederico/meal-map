package it.mealmap.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import it.mealmap.R
import it.mealmap.ui.components.AppIcons
import kotlinx.serialization.Serializable

@Serializable data object TodayRoute

@Serializable data object WeekRoute

@Serializable data object BalanceRoute

@Serializable data object RulesRoute

/** The four tabs of the bottom navigation bar. */
enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    TODAY(TodayRoute, R.string.tab_today, AppIcons.Today),
    WEEK(WeekRoute, R.string.tab_week, AppIcons.Week),
    BALANCE(BalanceRoute, R.string.tab_balance, AppIcons.Balance),
    RULES(RulesRoute, R.string.tab_rules, AppIcons.Rules),
}
