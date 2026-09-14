package com.example.goldfinbudgeting.data

import androidx.room.Database
import androidx.room.RoomDatabase

// version = 1 is the first schema; bump it when you change tables later
// exportSchema = false skips writing a schema JSON file 
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GoldfinDatabase : RoomDatabase() {
    // Room generates the real implementations at compile time via KSP.

    // Access the users table.
    abstract fun userDao(): UserDao

    // Access the categories / envelopes table.
    abstract fun categoryDao(): CategoryDao

    // Access the expenses table.
    abstract fun expenseDao(): ExpenseDao
}
