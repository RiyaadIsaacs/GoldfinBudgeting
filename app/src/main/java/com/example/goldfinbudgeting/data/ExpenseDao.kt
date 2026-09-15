package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Simple result type for "total spent per category in a date range"
// Room fills categoryName and total from the SELECT aliases below
data class CategoryTotal(
    val categoryName: String,
    val total: Double
)

//list expenses in a period + category totals in a period
@Dao
interface ExpenseDao {

    // Insert one expense. Returns the new auto-generated id
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(expense: ExpenseEntity): Long

    // Bulk insert for first-launch seed data
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(expenses: List<ExpenseEntity>)

    // Update an existing expense row (must include the correct id)
    @Update
    fun update(expense: ExpenseEntity)

    // Delete one expense 
    @Delete
    fun delete(expense: ExpenseEntity)

    // All expenses, newest first — used by ExpenseTempMemory.expenses
    @Query("SELECT * FROM expenses ORDER BY dateCreated DESC, id DESC")
    fun getAll(): List<ExpenseEntity>

    // One expense by primary key
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getById(id: Long): ExpenseEntity?

    // Expenses whose dateCreated falls inside a user-selected period
    @Query(
        "SELECT * FROM expenses WHERE dateCreated BETWEEN :startMillis AND :endMillis " +
            "ORDER BY dateCreated DESC, id DESC"
    )
    fun getBetween(startMillis: Long, endMillis: Long): List<ExpenseEntity>

    // Sum of amounts grouped by category for a period
    @Query(
        "SELECT categoryName AS categoryName, SUM(amount) AS total FROM expenses " +
            "WHERE dateCreated BETWEEN :startMillis AND :endMillis " +
            "GROUP BY categoryName ORDER BY total DESC"
    )
    fun totalsByCategory(startMillis: Long, endMillis: Long): List<CategoryTotal>

    // Lifetime total spent in one category (used to fill Envelope.spent on the UI)
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE categoryName = :categoryName"
    )
    fun sumForCategory(categoryName: String): Double

    // Same as sumForCategory but limited to a date range
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE categoryName = :categoryName AND dateCreated BETWEEN :startMillis AND :endMillis"
    )
    fun sumForCategoryBetween(categoryName: String, startMillis: Long, endMillis: Long): Double

    // Row count — used to seed sample expenses only when the table is empty
    @Query("SELECT COUNT(*) FROM expenses")
    fun count(): Int
}
