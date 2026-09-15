package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlyGoalDao {

    // Load the goals saved for one year / month, or null if none yet
    @Query("SELECT * FROM monthly_goals WHERE year = :year AND month = :month LIMIT 1")
    fun get(year: Int, month: Int): MonthlyGoalEntity?

    // Insert or replace goals for that month
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(goal: MonthlyGoalEntity)
}
