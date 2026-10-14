package it.mealmap

import android.app.Application

class MealMapApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
