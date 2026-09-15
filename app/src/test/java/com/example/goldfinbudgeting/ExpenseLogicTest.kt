package com.example.goldfinbudgeting

import com.example.goldfinbudgeting.data.EntityMappers
import com.example.goldfinbudgeting.data.ExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Automated unit tests for the main expenses functionality.
 * These run on the JVM (./gradlew test) and in GitHub Actions.
 */
class ExpenseLogicTest {

    private val augustExpenses = listOf(
        Expense("Call of Duty 3", 1000.00, "Games", ExpenseLogic.dateOn(2026, Calendar.AUGUST, 5)),
        Expense("McDonalds", 200.00, "Takeouts", ExpenseLogic.dateOn(2026, Calendar.AUGUST, 8)),
        Expense("Netflix Subscription", 100.00, "Subscriptions", ExpenseLogic.dateOn(2026, Calendar.AUGUST, 1)),
        Expense("Typo", 300.00, "Groceries", ExpenseLogic.dateOn(2026, Calendar.AUGUST, 20), description = "stationery"),
        Expense("Disney+", 99.00, "Subscriptions", ExpenseLogic.dateOn(2026, Calendar.JULY, 1))
    )

    @Test
    fun expensesInMonth_onlyReturnsSelectedMonth() {
        val august = ExpenseLogic.expensesInMonth(augustExpenses, 2026, Calendar.AUGUST)

        assertEquals(4, august.size)
        assertTrue(august.none { it.name == "Disney+" })
    }

    @Test
    fun expensesForScreen_filtersByCategory() {
        val games = ExpenseLogic.expensesForScreen(
            expenses = augustExpenses,
            year = 2026,
            month = Calendar.AUGUST,
            category = "Games"
        )

        assertEquals(1, games.size)
        assertEquals("Call of Duty 3", games.first().name)
        assertEquals(1000.00, ExpenseLogic.totalAmount(games), 0.001)
    }

    @Test
    fun expensesForScreen_filtersBySearchText() {
        val results = ExpenseLogic.expensesForScreen(
            expenses = augustExpenses,
            year = 2026,
            month = Calendar.AUGUST,
            category = null,
            searchQuery = "netflix"
        )

        assertEquals(1, results.size)
        assertEquals("Netflix Subscription", results.first().name)
    }

    @Test
    fun expensesForScreen_searchAlsoMatchesDescription() {
        val results = ExpenseLogic.expensesForScreen(
            expenses = augustExpenses,
            year = 2026,
            month = Calendar.AUGUST,
            category = null,
            searchQuery = "stationery"
        )

        assertEquals(1, results.size)
        assertEquals("Typo", results.first().name)
    }

    @Test
    fun expensesForScreen_filtersByCustomDateRange() {
        val start = ExpenseLogic.dateOn(2026, Calendar.AUGUST, 1)
        val end = ExpenseLogic.dateOn(2026, Calendar.AUGUST, 10)

        val results = ExpenseLogic.expensesForScreen(
            expenses = augustExpenses,
            year = 2026,
            month = Calendar.AUGUST,
            category = null,
            rangeStartMillis = start,
            rangeEndMillis = end
        )

        assertEquals(3, results.size)
        assertTrue(results.none { it.name == "Typo" })
    }

    @Test
    fun totalAmount_sumsFilteredExpenses() {
        val august = ExpenseLogic.expensesInMonth(augustExpenses, 2026, Calendar.AUGUST)

        assertEquals(1600.00, ExpenseLogic.totalAmount(august), 0.001)
    }

    @Test
    fun formatAmount_usesRandCurrencyStyle() {
        assertEquals("R 1,000.00", ExpenseLogic.formatAmount(1000.0))
        assertEquals("R 99.00", ExpenseLogic.formatAmount(99.0))
    }

    @Test
    fun parseDate_roundTripsWithFormatDate() {
        val millis = ExpenseLogic.dateOn(2026, Calendar.AUGUST, 14)
        val formatted = ExpenseLogic.formatDate(millis)

        assertEquals("2026/08/14", formatted)
        assertEquals(millis, ExpenseLogic.parseDate(formatted))
    }

    @Test
    fun monthTitle_formatsReadableMonth() {
        assertEquals("August 2026", ExpenseLogic.monthTitle(2026, Calendar.AUGUST))
    }

    @Test
    fun areMonthlyGoalsValid_rejectsInvalidRanges() {
        assertTrue(ExpenseLogic.areMonthlyGoalsValid(2000.0, 4000.0))
        assertFalse(ExpenseLogic.areMonthlyGoalsValid(5000.0, 4000.0))
        assertFalse(ExpenseLogic.areMonthlyGoalsValid(-1.0, 4000.0))
        assertFalse(ExpenseLogic.areMonthlyGoalsValid(100.0, 0.0))
    }

    @Test
    fun progressFillPercent_capsAtOneHundred() {
        assertEquals(50.0, ExpenseLogic.progressFillPercent(2000.0, 4000.0), 0.001)
        assertEquals(100.0, ExpenseLogic.progressFillPercent(5000.0, 4000.0), 0.001)
        assertEquals(0.0, ExpenseLogic.progressFillPercent(100.0, 0.0), 0.001)
    }

    @Test
    fun entityMappers_roundTripPreservesExpenseFields() {
        val original = Expense(
            name = "Kauai",
            amount = 200.00,
            category = "Takeouts",
            dateCreated = ExpenseLogic.dateOn(2026, Calendar.AUGUST, 15),
            description = "lunch",
            startTime = "12:00",
            endTime = "13:00",
            receiptPath = "/tmp/receipt.jpg",
            id = 42
        )

        val entity: ExpenseEntity = EntityMappers.toExpenseEntity(original)
        val mappedBack = EntityMappers.toExpense(entity)

        assertEquals(original, mappedBack)
        assertEquals("Takeouts", entity.categoryName)
    }
}
