package it.mealmap.ui.navigation

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.mealmap.R
import it.mealmap.domain.model.MealSlot
import it.mealmap.ui.appViewModel
import it.mealmap.ui.balance.BalanceScreen
import it.mealmap.ui.balance.BalanceViewModel
import it.mealmap.ui.meal.EditMealScreen
import it.mealmap.ui.rules.RulesScreen
import it.mealmap.ui.today.TodayScreen
import it.mealmap.ui.week.WeekScreen

@Composable
fun MealMapApp(navController: NavHostController = rememberNavController()) {
    // One balance for the whole app: the tab badge, the Balance screen and the Week warning.
    val balanceViewModel = appViewModel { BalanceViewModel(it.weekRepository, it.rulesRepository) }
    val balanceState by balanceViewModel.state.collectAsStateWithLifecycle()
    val hasOverLimit = balanceState.balance?.overLimit.orEmpty().isNotEmpty()
    // A meal waiting to be swapped by tapping another one; set from any meal detail sheet.
    var swapSource by remember { mutableStateOf<MealSlot?>(null) }
    val onEditMeal = { slot: MealSlot ->
        navController.navigate(EditMealRoute(MealSlot.ALL.indexOf(slot)))
    }
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    // The first frame has no destination yet: it is the start tab.
    val onTab = currentDestination == null ||
        TopLevelDestination.entries.any { currentDestination.hasRoute(it.route::class) }

    Scaffold(
        bottomBar = { if (onTab) MealMapBottomBar(navController, hasOverLimit) },
        // Screens draw their header under the status bar, so the top inset is left to them.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<TodayRoute> {
                TodayScreen(
                    onEditMeal = onEditMeal,
                    onSwapMeal = { slot ->
                        swapSource = slot
                        navController.navigateToTab(TopLevelDestination.WEEK)
                    },
                )
            }
            composable<WeekRoute> {
                WeekScreen(
                    overLimit = balanceState.balance?.overLimit.orEmpty().map { it.category },
                    swapSource = swapSource,
                    onSwapSourceChange = { swapSource = it },
                    onEditMeal = onEditMeal,
                    onOpenBalance = { navController.navigateToTab(TopLevelDestination.BALANCE) },
                )
            }
            composable<BalanceRoute> {
                BalanceScreen(
                    state = balanceState,
                    onOpenWeek = { navController.navigateToTab(TopLevelDestination.WEEK) },
                )
            }
            composable<RulesRoute> { RulesScreen() }
            composable<EditMealRoute> { entry ->
                EditMealScreen(
                    slot = MealSlot.ALL[entry.toRoute<EditMealRoute>().slot],
                    // Close and save can both fire: only the first one, while resumed, goes back.
                    onDone = dropUnlessResumed { navController.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun MealMapBottomBar(navController: NavHostController, hasOverLimit: Boolean) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(destination) },
                icon = {
                    if (destination == TopLevelDestination.BALANCE && hasOverLimit) {
                        val badgeDescription = stringResource(R.string.balance_tab_over_limit)
                        BadgedBox(
                            badge = {
                                Badge(
                                    Modifier.semantics {
                                        contentDescription =
                                            badgeDescription
                                    },
                                )
                            },
                        ) {
                            Icon(destination.icon, contentDescription = null)
                        }
                    } else {
                        Icon(destination.icon, contentDescription = null)
                    }
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}
