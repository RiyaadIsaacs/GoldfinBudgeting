package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlyGoalDao {

    // Load the goals saved for one year / month for that account, or null if none yet
    @Query(
        "SELECT * FROM monthly_goals WHERE userId = :userId AND year = :year AND month = :month LIMIT 1"
    )
    fun get(userId: Long, year: Int, month: Int): MonthlyGoalEntity?

    // Insert or replace goals for that month / account
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(goal: MonthlyGoalEntity)
}
