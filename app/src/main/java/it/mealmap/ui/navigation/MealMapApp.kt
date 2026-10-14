package it.mealmap.ui.navigation

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.mealmap.ui.balance.BalanceScreen
import it.mealmap.ui.rules.RulesScreen
import it.mealmap.ui.today.TodayScreen
import it.mealmap.ui.week.WeekScreen

@Composable
fun MealMapApp(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = { MealMapBottomBar(navController) },
        // Screens draw their header under the status bar, so the top inset is left to them.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<TodayRoute> { TodayScreen() }
            composable<WeekRoute> { WeekScreen() }
            composable<BalanceRoute> { BalanceScreen() }
            composable<RulesRoute> { RulesScreen() }
        }
    }
}

@Composable
private fun MealMapBottomBar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}
