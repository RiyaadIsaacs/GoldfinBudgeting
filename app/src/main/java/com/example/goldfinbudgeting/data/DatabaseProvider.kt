package com.example.goldfinbudgeting.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import java.util.Calendar

// DatabaseProvider creates and shares one GoldfinDatabase instance for the whole app
// Using a singleton avoids opening many database connections
object DatabaseProvider {
    // Tag used in Logcat so we can prove Room is opening / seeding
    private const val TAG = "GoldfinDatabase"

    // File name of the SQLite database stored in the app's private data folder
    private const val DB_NAME = "goldfin_budgeting.db"

    // Cached database instance
    @Volatile
    private var instance: GoldfinDatabase? = null

    // Returns the shared database, creating it the first time if it is needed
    fun get(context: Context): GoldfinDatabase {
        // Double-checked locking: fast path if already built, otherwise build once safely.
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { db ->
                instance = db
                // Only insert starter rows when tables are empty.
                seedIfNeeded(db)
                Log.d(TAG, "Room database ready")
            }
        }
    }

    // Builds the Room database 
    private fun buildDatabase(context: Context): GoldfinDatabase {
        Log.d(TAG, "Opening Room database: $DB_NAME")
        return Room.databaseBuilder(context, GoldfinDatabase::class.java, DB_NAME)
            // Activities still call stores on the main thread in this prototype,
            // so Room is allowed to run queries on the UI thread.
            .allowMainThreadQueries()
            // If the schema version changes without a Migration, wipe and recreate tables.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    // Helper to build a calendar date as epoch millis for seed data
    private fun dateOn(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }

    // Inserts starter data only when each table has zero rows
    // This keeps the old demo experience after switching from in-memory lists to Room
    private fun seedIfNeeded(db: GoldfinDatabase) {
        val userDao = db.userDao()
        val categoryDao = db.categoryDao()
        val expenseDao = db.expenseDao()

        // Default test login used across the group: username 1 / password 1.
        if (userDao.count() == 0) {
            val userId = userDao.insert(UserEntity(username = "1", password = "1"))
            Log.d(TAG, "Seeded default user id=$userId (username=1)")
        }

        // Sample envelopes / categories with min and max goals
        if (categoryDao.count() == 0) {
            val categories = listOf(
                CategoryEntity(
                    name = "Subscriptions",
                    minGoal = 800.00,
                    maxGoal = 1000.00,
                    dateCreated = dateOn(2026, Calendar.AUGUST, 1)
                ),
                CategoryEntity(
                    name = "Groceries",
                    minGoal = 1500.00,
                    maxGoal = 2000.00,
                    dateCreated = dateOn(2026, Calendar.AUGUST, 1)
                ),
                CategoryEntity(
                    name = "Takeouts",
                    minGoal = 600.00,
                    maxGoal = 1000.00,
                    dateCreated = dateOn(2026, Calendar.AUGUST, 1)
                ),
                CategoryEntity(
                    name = "Games",
                    minGoal = 600.00,
                    maxGoal = 2000.00,
                    dateCreated = dateOn(2026, Calendar.AUGUST, 1)
                ),
                CategoryEntity(
                    name = "Car",
                    minGoal = 4000.00,
                    maxGoal = 10000.00,
                    dateCreated = dateOn(2026, Calendar.AUGUST, 1)
                )
            )
            categoryDao.insertAll(categories)
            Log.d(TAG, "Seeded ${categories.size} categories")
        }

        // Sample expenses across several months so month / period filters have data to show
        if (expenseDao.count() == 0) {
            // Local helper so we use named fields and do not pass values into the id parameter by mistake
            fun expense(
                name: String,
                amount: Double,
                category: String,
                dateCreated: Long
            ) = ExpenseEntity(
                name = name,
                amount = amount,
                categoryName = category,
                dateCreated = dateCreated
            )

            val expenses = listOf(
                // August 2026 sample expenses
                expense("Call of Duty 3", 1000.00, "Games", dateOn(2026, Calendar.AUGUST, 5)),
                expense("McDonalds", 200.00, "Takeouts", dateOn(2026, Calendar.AUGUST, 8)),
                expense("Spiderman: Brand New Day", 150.00, "Games", dateOn(2026, Calendar.AUGUST, 10)),
                expense("Astron Energy", 500.00, "Groceries", dateOn(2026, Calendar.AUGUST, 12)),
                expense("Netflix Subscription", 100.00, "Subscriptions", dateOn(2026, Calendar.AUGUST, 1)),
                expense("Kauai", 200.00, "Takeouts", dateOn(2026, Calendar.AUGUST, 15)),
                expense("Comic Warehouse", 250.00, "Games", dateOn(2026, Calendar.AUGUST, 18)),
                expense("Typo", 300.00, "Groceries", dateOn(2026, Calendar.AUGUST, 20)),
                // Other months so the period picker is useful in demos
                expense("Disney+", 99.00, "Subscriptions", dateOn(2026, Calendar.JULY, 1)),
                expense("Checkers", 850.00, "Groceries", dateOn(2026, Calendar.JULY, 7)),
                expense("Nandos", 180.00, "Takeouts", dateOn(2026, Calendar.JULY, 14)),
                expense("Steam Sale", 450.00, "Games", dateOn(2026, Calendar.JULY, 21)),
                expense("Spotify", 79.00, "Subscriptions", dateOn(2026, Calendar.JUNE, 1)),
                expense("Woolworths", 620.00, "Groceries", dateOn(2026, Calendar.JUNE, 9)),
                expense("Uber Eats", 240.00, "Takeouts", dateOn(2026, Calendar.JUNE, 16)),
                expense("Showmax", 99.00, "Subscriptions", dateOn(2026, Calendar.MAY, 1)),
                expense("Engen", 480.00, "Groceries", dateOn(2026, Calendar.MAY, 11)),
                expense("Steers", 160.00, "Takeouts", dateOn(2026, Calendar.MAY, 22))
            )
            expenseDao.insertAll(expenses)
            Log.d(TAG, "Seeded ${expenses.size} expenses")
        }

        // Sample monthly min / max spending goals
        if (db.monthlyGoalDao().get(2026, Calendar.AUGUST) == null) {
            db.monthlyGoalDao().upsert(
                MonthlyGoalEntity(
                    year = 2026,
                    month = Calendar.AUGUST,
                    minGoal = 2000.00,
                    maxGoal = 4000.00
                )
            )
            Log.d(TAG, "Seeded August 2026 monthly goals")
        }
    }
}
