package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Simple result type for total spent per category in date range
// Room fills categoryName and total from the SELECT aliases below
data class CategoryTotal(
    val categoryName: String,
    val total: Double
)

//list expenses in a period and category totals in a period
@Dao
interface ExpenseDao {

    // Insert one expense. Returns the new auto-generated id
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(expense: ExpenseEntity): Long

    // Bulk insert for first-launch seed data
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(expenses: List<ExpenseEntity>)

    // Update an existing expense row. must include the correct id
    @Update
    fun update(expense: ExpenseEntity)

    // Delete one expense
    @Delete
    fun delete(expense: ExpenseEntity)

    // All expenses for one account, newest first
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY dateCreated DESC, id DESC")
    fun getAllForUser(userId: Long): List<ExpenseEntity>

    // One expense by primary key for that account
    @Query("SELECT * FROM expenses WHERE id = :id AND userId = :userId LIMIT 1")
    fun getById(userId: Long, id: Long): ExpenseEntity?

    // Expenses whose dateCreated falls inside a user-selected period
    @Query(
        "SELECT * FROM expenses WHERE userId = :userId AND dateCreated BETWEEN :startMillis AND :endMillis " +
            "ORDER BY dateCreated DESC, id DESC"
    )
    fun getBetween(userId: Long, startMillis: Long, endMillis: Long): List<ExpenseEntity>

    // Sum of amounts grouped by category for a period
    @Query(
        "SELECT categoryName AS categoryName, SUM(amount) AS total FROM expenses " +
            "WHERE userId = :userId AND dateCreated BETWEEN :startMillis AND :endMillis " +
            "GROUP BY categoryName ORDER BY total DESC"
    )
    fun totalsByCategory(userId: Long, startMillis: Long, endMillis: Long): List<CategoryTotal>

    // Lifetime total spent in one category for that account
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE userId = :userId AND categoryName = :categoryName"
    )
    fun sumForCategory(userId: Long, categoryName: String): Double

    // Same as sumForCategory but limited to a date range
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE userId = :userId AND categoryName = :categoryName " +
            "AND dateCreated BETWEEN :startMillis AND :endMillis"
    )
    fun sumForCategoryBetween(
        userId: Long,
        categoryName: String,
        startMillis: Long,
        endMillis: Long
    ): Double

    // Row count for one account — used when seeding demo expenses for user 1 only
    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    fun countForUser(userId: Long): Int
}
