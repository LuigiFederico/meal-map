package it.mealmap.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mealmap.AppContainer
import it.mealmap.MealMapApplication

/**
 * A ViewModel built from the app's [AppContainer] (manual dependency injection).
 * Use [key] to keep one instance per argument, e.g. per meal slot.
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM = viewModel(key = key) {
    create((checkNotNull(this[APPLICATION_KEY]) as MealMapApplication).container)
}
