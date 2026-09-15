package com.example.goldfinbudgeting

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Pure helpers for expense filtering / formatting.
// Kept free of Room / Context so unit tests can run on the JVM and in GitHub Actions.
object ExpenseLogic {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun dateOn(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }

    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun formatAmount(amount: Double): String {
        return "R " + String.format(Locale.US, "%,.2f", amount)
    }

    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun parseDate(text: String): Long {
        return try {
            dateFormat.parse(text)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun monthTitle(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return monthFormat.format(calendar.time)
    }

    fun expenseSubtitle(expense: Expense): String {
        return "${expense.category} -> ${formatShortDate(expense.dateCreated)}"
    }

    fun expensesInMonth(expenses: List<Expense>, year: Int, month: Int): List<Expense> {
        return expenses.filter { expense ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = expense.dateCreated
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    fun expensesInRange(expenses: List<Expense>, startMillis: Long, endMillis: Long): List<Expense> {
        val start = startOfDay(startMillis)
        val end = endOfDay(endMillis)
        return expenses.filter { expense ->
            expense.dateCreated in start..end
        }
    }

    // Same filtering rules as the Expenses screen: month or range, then category, then search
    fun expensesForScreen(
        expenses: List<Expense>,
        year: Int,
        month: Int,
        category: String?,
        searchQuery: String? = null,
        rangeStartMillis: Long? = null,
        rangeEndMillis: Long? = null
    ): List<Expense> {
        val baseList = if (rangeStartMillis != null && rangeEndMillis != null) {
            expensesInRange(expenses, rangeStartMillis, rangeEndMillis)
        } else {
            expensesInMonth(expenses, year, month)
        }

        val byCategory = if (category.isNullOrBlank()) {
            baseList
        } else {
            baseList.filter { expense ->
                expense.category.equals(category, ignoreCase = true)
            }
        }

        val query = searchQuery?.trim().orEmpty()
        if (query.isBlank()) {
            return byCategory
        }

        return byCategory.filter { expense ->
            expense.name.contains(query, ignoreCase = true) ||
                expense.category.contains(query, ignoreCase = true) ||
                expense.description.contains(query, ignoreCase = true)
        }
    }

    fun totalAmount(expenses: List<Expense>): Double {
        return expenses.sumOf { it.amount }
    }

    // Min must be >= 0, max must be > 0, and min cannot be higher than max
    fun areMonthlyGoalsValid(minGoal: Double, maxGoal: Double): Boolean {
        return minGoal >= 0.0 && maxGoal > 0.0 && minGoal <= maxGoal
    }

    // Progress fill used by the monthly goals bar (0 to 100)
    fun progressFillPercent(spent: Double, maxGoal: Double): Double {
        if (maxGoal <= 0.0) {
            return 0.0
        }
        return ((spent / maxGoal) * 100).coerceAtMost(100.0)
    }
}
