package com.example.goldfinbudgeting

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//stores expense info temporarily in memory. will replace when using local database
object ExpenseTempMemory {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    val categories = listOf(
        "General",
        "Subscriptions",
        "Games",
        "Groceries",
        "Takeouts"
    )

    val expenses = mutableListOf(

        //August 2026 starter list from the hard coded expenses screen
        Expense("Call of Duty 3", 1000.00, "Games", dateOn(2026, Calendar.AUGUST, 5)),
        Expense("McDonalds", 200.00, "Takeouts", dateOn(2026, Calendar.AUGUST, 8)),
        Expense("Spiderman: Brand New Day", 150.00, "Games", dateOn(2026, Calendar.AUGUST, 10)),
        Expense("Astron Energy", 500.00, "Groceries", dateOn(2026, Calendar.AUGUST, 12)),
        Expense("Netflix Subscription", 100.00, "Subscriptions", dateOn(2026, Calendar.AUGUST, 1)),
        Expense("Kauai", 200.00, "Takeouts", dateOn(2026, Calendar.AUGUST, 15)),
        Expense("Comic Warehouse", 250.00, "Games", dateOn(2026, Calendar.AUGUST, 18)),
        Expense("Typo", 300.00, "Groceries", dateOn(2026, Calendar.AUGUST, 20)),

        //sample expenses for other months so the month picker shows a different list
        Expense("Disney+", 99.00, "Subscriptions", dateOn(2026, Calendar.JULY, 1)),
        Expense("Checkers", 850.00, "Groceries", dateOn(2026, Calendar.JULY, 7)),
        Expense("Nandos", 180.00, "Takeouts", dateOn(2026, Calendar.JULY, 14)),
        Expense("Steam Sale", 450.00, "Games", dateOn(2026, Calendar.JULY, 21)),

        Expense("Spotify", 79.00, "Subscriptions", dateOn(2026, Calendar.JUNE, 1)),
        Expense("Woolworths", 620.00, "Groceries", dateOn(2026, Calendar.JUNE, 9)),
        Expense("Uber Eats", 240.00, "Takeouts", dateOn(2026, Calendar.JUNE, 16)),

        Expense("Showmax", 99.00, "Subscriptions", dateOn(2026, Calendar.MAY, 1)),
        Expense("Engen", 480.00, "Groceries", dateOn(2026, Calendar.MAY, 11)),
        Expense("Steers", 160.00, "Takeouts", dateOn(2026, Calendar.MAY, 22))
    )

    fun addExpense(expense: Expense) {
        expenses.add(0, expense)
    }

    fun updateExpense(index: Int, expense: Expense): Boolean {
        if (index < 0 || index >= expenses.size) {
            return false
        }

        expenses[index] = expense

        return true
    }

    fun deleteExpense(index: Int): Boolean {
        if (index < 0 || index >= expenses.size) {
            return false
        }

        expenses.removeAt(index)

        return true
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun expenseSubtitle(expense: Expense): String {
        return "${expense.category} -> ${formatShortDate(expense.dateCreated)}"
    }

    //only expenses whose created date is in that month
    fun expensesInMonth(year: Int, month: Int): List<Expense> {
        return expenses.filter { expense ->
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = expense.dateCreated

            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    //month list, optionally narrowed to one category chip
    fun expensesForScreen(year: Int, month: Int, category: String?): List<Expense> {
        val inMonth = expensesInMonth(year, month)

        if (category.isNullOrBlank()) {
            return inMonth
        }

        return inMonth.filter { expense ->
            expense.category.equals(category, ignoreCase = true)
        }
    }

    //months shown in the expenses dropdown. Figma months plus any month that has an expense
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

        return months.sortedWith(
            compareByDescending<Pair<Int, Int>> { it.first }
                .thenByDescending { it.second }
        )
    }

    fun monthTitle(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return monthFormat.format(calendar.time)
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

    fun dateOn(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()

        calendar.clear()
        calendar.set(year, month, day)

        return calendar.timeInMillis
    }
}
