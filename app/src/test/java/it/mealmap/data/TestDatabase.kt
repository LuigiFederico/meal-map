package it.mealmap.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import it.mealmap.data.db.AppDatabase
import it.mealmap.data.seed.Seeder

/** An in-memory database, seeded like the real one unless [seeded] is false. */
fun inMemoryDatabase(seeded: Boolean = true): AppDatabase {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .apply { if (seeded) addCallback(Seeder(context.assets)) }
        .allowMainThreadQueries()
        .build()
}
