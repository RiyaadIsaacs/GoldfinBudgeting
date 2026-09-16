package com.example.goldfinbudgeting.data

import androidx.room.Entity

// One min / max spending goal pair for a calendar month
@Entity(
    tableName = "monthly_goals",
    primaryKeys = ["year", "month"]
)
data class MonthlyGoalEntity(
    // Calendar year, e.g. 2026
    val year: Int,

    // Calendar.MONTH value. like 0 = January to 11 = December
    val month: Int,

    // Minimum monthly spending goal
    val minGoal: Double,

    // Maximum monthly spending goal
    val maxGoal: Double
)
