package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider
import com.example.goldfinbudgeting.data.EntityMappers
import com.example.goldfinbudgeting.data.GoldfinDatabase
import com.example.goldfinbudgeting.data.MonthlyGoalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ExpenseTempMemory used to keep expenses in a mutableList in RAM
// It now reads / writes the Room expenses table so data survives app restarts
object ExpenseTempMemory {
    private const val TAG = "ExpenseTempMemory"

    // Date helpers used by the expenses screens for display and parsing.
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    // Cached Room database reference after bind() / first use
    @Volatile
    private var database: GoldfinDatabase? = null

    // Application context so we can open Room even when Activities recreate
    @Volatile
    private var appContext: Context? = null

    // Returns the Room database, opening it through DatabaseProvider if needed
    private fun db(context: Context? = appContext): GoldfinDatabase {
        database?.let { return it }
        val ctx = context ?: appContext
            ?: throw IllegalStateException("ExpenseTempMemory used before GoldfinApp initialised Room")
        return DatabaseProvider.get(ctx).also { database = it }
    }

    // Called from GoldfinApp.onCreate so stores are ready before LoginActivity
    fun bind(context: Context) {
        appContext = context.applicationContext
        database = DatabaseProvider.get(context)
    }

    // Category names for the expense picker: defaults plus any names already in Room
    val categories: List<String>
        get() {
            val fromDb = db().categoryDao().getAll().map { it.name }
            val defaults = listOf(
                "General",
                "Subscriptions",
                "Games",
                "Groceries",
                "Takeouts"
            )
            // distinct() avoids showing the same category twice.
            return (defaults + fromDb).distinct()
        }

    // Live list of all expenses from Room. Not a cached mutable list anymore
    val expenses: List<Expense>
        get() = db().expenseDao().getAll().map(EntityMappers::toExpense)

    // Insert a new expense. id is forced to 0 so Room auto-generates a primary key
    fun addExpense(expense: Expense) {
        val id = db().expenseDao().insert(EntityMappers.toExpenseEntity(expense.copy(id = 0)))
        Log.d(TAG, "Inserted expense id=$id name=${expense.name}")
    }

    // Update the expense at a list index by keeping the existing Room id
    fun updateExpense(index: Int, expense: Expense): Boolean {
        val current = expenses
        if (index < 0 || index >= current.size) {
            return false
        }
        return updateExpenseById(current[index].id, expense)
    }

    // Update by Room id so edits stay correct even if the list order changes
    fun updateExpenseById(id: Long, expense: Expense): Boolean {
        if (id <= 0L || db().expenseDao().getById(id) == null) {
            return false
        }
        db().expenseDao().update(EntityMappers.toExpenseEntity(expense.copy(id = id)))
        Log.d(TAG, "Updated expense id=$id name=${expense.name}")
        return true
    }

    // Delete the expense currently shown at that index in the Room-backed list
    fun deleteExpense(index: Int): Boolean {
        val current = expenses
        if (index < 0 || index >= current.size) {
            return false
        }
        return deleteExpenseById(current[index].id)
    }

    // Delete by Room id
    fun deleteExpenseById(id: Long): Boolean {
        val entity = db().expenseDao().getById(id) ?: return false
        db().expenseDao().delete(entity)
        Log.d(TAG, "Deleted expense id=${entity.id} name=${entity.name}")
        return true
    }

    // Load one expense by Room id, or null if it was deleted
    fun expenseById(id: Long): Expense? {
        return db().expenseDao().getById(id)?.let(EntityMappers::toExpense)
    }

    // Short date for list subtitles, like "5 Aug"
    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    // Text under the expense name: "Games - 5 Aug"
    fun expenseSubtitle(expense: Expense): String {
        return "${expense.category} -> ${formatShortDate(expense.dateCreated)}"
    }

    // Filter to expenses whose created date falls in the given calendar month
    fun expensesInMonth(year: Int, month: Int): List<Expense> {
        return expenses.filter { expense ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = expense.dateCreated
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    // Builds the list shown on the Expenses screen
    // Can use a custom date range, or fall back to the selected month
    fun expensesForScreen(
        year: Int,
        month: Int,
        category: String?,
        searchQuery: String? = null,
        rangeStartMillis: Long? = null,
        rangeEndMillis: Long? = null
    ): List<Expense> {
        // Prefer an explicit from/to range when the user picked one
        val baseList = if (rangeStartMillis != null && rangeEndMillis != null) {
            expensesInRange(rangeStartMillis, rangeEndMillis)
        } else {
            expensesInMonth(year, month)
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

        // Search matches name, category, or description
        return byCategory.filter { expense ->
            expense.name.contains(query, ignoreCase = true) ||
                expense.category.contains(query, ignoreCase = true) ||
                expense.description.contains(query, ignoreCase = true)
        }
    }

    // Uses Room getBetween for a user-selectable period
    fun expensesInRange(startMillis: Long, endMillis: Long): List<Expense> {
        val start = startOfDay(startMillis)
        val end = endOfDay(endMillis)
        return db().expenseDao().getBetween(start, end).map(EntityMappers::toExpense)
    }

    // Category totals for a period via Room GROUP BY
    fun totalsByCategoryInRange(startMillis: Long, endMillis: Long): Map<String, Double> {
        val start = startOfDay(startMillis)
        val end = endOfDay(endMillis)
        return db().expenseDao().totalsByCategory(start, end)
            .associate { it.categoryName to it.total }
    }

    // Sum of amounts for whatever the Expenses screen is currently filtering
    fun totalForScreen(
        year: Int,
        month: Int,
        category: String?,
        searchQuery: String? = null,
        rangeStartMillis: Long? = null,
        rangeEndMillis: Long? = null
    ): Double {
        return expensesForScreen(
            year,
            month,
            category,
            searchQuery,
            rangeStartMillis,
            rangeEndMillis
        ).sumOf { it.amount }
    }

    // Start of the calendar day for inclusive range queries
    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // End of the calendar day for inclusive range queries
    fun endOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Months shown in the expenses dropdown
    fun filterMonths(): List<Pair<Int, Int>> {
        val months = linkedSetOf(
            2026 to Calendar.AUGUST,
            2026 to Calendar.JULY,
            2026 to Calendar.JUNE,
            2026 to Calendar.MAY
        )

        expenses.forEach { expense ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = expense.dateCreated
            months.add(calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH))
        }

        // Newest months first
        return months.sortedWith(
            compareByDescending<Pair<Int, Int>> { it.first }
                .thenByDescending { it.second }
        )
    }

    // Human-readable month title for buttons / headers, like "August 2026"
    fun monthTitle(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return monthFormat.format(calendar.time)
    }

    // Rand formatting for totals and list amounts
    fun formatAmount(amount: Double): String {
        return "R " + String.format(Locale.US, "%,.2f", amount)
    }

    // Full date string for edit fields, like "2026/08/05"
    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    // Parse a typed date back to millis; falls back to "now" if the text is invalid
    fun parseDate(text: String): Long {
        return try {
            dateFormat.parse(text)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    // Build millis for a specific calendar day (used by seed data and date pickers)
    fun dateOn(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }

    // Monthly spending goals for one calendar month, or null if the user has not set any
    fun monthlyGoals(year: Int, month: Int): Pair<Double, Double>? {
        val goal = db().monthlyGoalDao().get(year, month) ?: return null
        return goal.minGoal to goal.maxGoal
    }

    // Save or replace the min / max spending goals for one month
    fun setMonthlyGoals(year: Int, month: Int, minGoal: Double, maxGoal: Double) {
        db().monthlyGoalDao().upsert(
            MonthlyGoalEntity(
                year = year,
                month = month,
                minGoal = minGoal,
                maxGoal = maxGoal
            )
        )
        Log.d(TAG, "Saved monthly goals year=$year month=$month min=$minGoal max=$maxGoal")
    }
}
