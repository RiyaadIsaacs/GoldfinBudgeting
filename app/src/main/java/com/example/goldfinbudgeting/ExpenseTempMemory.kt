package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider
import com.example.goldfinbudgeting.data.EntityMappers
import com.example.goldfinbudgeting.data.GoldfinDatabase
import com.example.goldfinbudgeting.data.MonthlyGoalEntity
import java.util.Calendar

// ExpenseTempMemory used to keep expenses in a mutableList in RAM
// It now reads / writes the Room expenses table so data survives app restarts
object ExpenseTempMemory {
    private const val TAG = "ExpenseTempMemory"

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

    // Short date for list subtitles, like 5 Aug
    fun formatShortDate(millis: Long): String {
        return ExpenseLogic.formatShortDate(millis)
    }

    // Text under the expense name like Games - 5 Aug
    fun expenseSubtitle(expense: Expense): String {
        return ExpenseLogic.expenseSubtitle(expense)
    }

    // Filter to expenses whose created date falls in the given calendar month
    fun expensesInMonth(year: Int, month: Int): List<Expense> {
        return ExpenseLogic.expensesInMonth(expenses, year, month)
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
        return ExpenseLogic.expensesForScreen(
            expenses = expenses,
            year = year,
            month = month,
            category = category,
            searchQuery = searchQuery,
            rangeStartMillis = rangeStartMillis,
            rangeEndMillis = rangeEndMillis
        )
    }

    // Uses Room getBetween for a user-selectable period
    fun expensesInRange(startMillis: Long, endMillis: Long): List<Expense> {
        val start = ExpenseLogic.startOfDay(startMillis)
        val end = ExpenseLogic.endOfDay(endMillis)
        return db().expenseDao().getBetween(start, end).map(EntityMappers::toExpense)
    }

    // Category totals for a period via Room GROUP BY
    fun totalsByCategoryInRange(startMillis: Long, endMillis: Long): Map<String, Double> {
        val start = ExpenseLogic.startOfDay(startMillis)
        val end = ExpenseLogic.endOfDay(endMillis)
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
        return ExpenseLogic.totalAmount(
            expensesForScreen(
                year,
                month,
                category,
                searchQuery,
                rangeStartMillis,
                rangeEndMillis
            )
        )
    }

    // Start of the calendar day for inclusive range queries
    fun startOfDay(millis: Long): Long {
        return ExpenseLogic.startOfDay(millis)
    }

    // End of the calendar day for inclusive range queries
    fun endOfDay(millis: Long): Long {
        return ExpenseLogic.endOfDay(millis)
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

    // Human-readable month title for buttons / headers, like August 2026
    fun monthTitle(year: Int, month: Int): String {
        return ExpenseLogic.monthTitle(year, month)
    }

    // Rand formatting for totals and list amounts
    fun formatAmount(amount: Double): String {
        return ExpenseLogic.formatAmount(amount)
    }

    // Full date string for edit fields, like 2026/08/05
    fun formatDate(millis: Long): String {
        return ExpenseLogic.formatDate(millis)
    }

    // Parse a typed date back to millis; falls back to now if the text is invalid
    fun parseDate(text: String): Long {
        return ExpenseLogic.parseDate(text)
    }

    // Build millis for a specific calendar day (used by seed data and date pickers)
    fun dateOn(year: Int, month: Int, day: Int): Long {
        return ExpenseLogic.dateOn(year, month, day)
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
