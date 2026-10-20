package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.FirebaseProvider
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Calendar
import java.util.concurrent.atomic.AtomicLong

// ExpenseTempMemory reads/writes Cloud Firestore expenses for the signed-in user
object ExpenseTempMemory {
    private const val TAG = "ExpenseTempMemory"

    private val idCounter = AtomicLong(System.currentTimeMillis())

    @Volatile
    private var appContext: Context? = null

    private fun requireContext(): Context {
        return appContext
            ?: throw IllegalStateException("ExpenseTempMemory used before GoldfinApp initialised Firebase")
    }

    private fun userId(): String {
        val uid = AccountStore.currentUserId(requireContext())
        if (uid.isBlank()) {
            throw IllegalStateException("No signed-in Firebase user for expenses")
        }
        return uid
    }

    fun bind(context: Context) {
        appContext = context.applicationContext
    }

    private fun nextId(): Long = idCounter.incrementAndGet()

    private fun snapshotToExpense(doc: DocumentSnapshot): Expense? {
        if (!doc.exists()) return null
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        return Expense(
            name = doc.getString("name").orEmpty(),
            amount = doc.getDouble("amount") ?: 0.0,
            category = doc.getString("categoryName").orEmpty(),
            dateCreated = doc.getLong("dateCreated") ?: 0L,
            description = doc.getString("description").orEmpty(),
            startTime = doc.getString("startTime").orEmpty(),
            endTime = doc.getString("endTime").orEmpty(),
            receiptPath = doc.getString("receiptPath"),
            id = id
        )
    }

    private fun expenseToMap(expense: Expense, uid: String): Map<String, Any?> {
        return mapOf(
            "id" to expense.id,
            "userId" to uid,
            "name" to expense.name,
            "amount" to expense.amount,
            "categoryName" to expense.category,
            "dateCreated" to expense.dateCreated,
            "description" to expense.description,
            "startTime" to expense.startTime,
            "endTime" to expense.endTime,
            "receiptPath" to expense.receiptPath
        )
    }

    val categories: List<String>
        get() {
            val uid = userId()
            val snap = FirebaseProvider.await(FirebaseProvider.categories(uid).get())
            FirebaseProvider.logRead("categories", uid, snap.size())
            val fromDb = snap.documents.mapNotNull { doc ->
                doc.getString("name")?.takeIf { it.isNotBlank() }
            }
            // Same categories as the expense chips, for every account, plus any custom envelopes
            val defaults = listOf("General", "Subscriptions", "Games", "Groceries", "Takeouts")
            return (defaults + fromDb).distinct()
        }

    val expenses: List<Expense>
        get() {
            val uid = userId()
            val snap = FirebaseProvider.await(
                FirebaseProvider.expenses(uid).orderBy("dateCreated", com.google.firebase.firestore.Query.Direction.DESCENDING).get()
            )
            FirebaseProvider.logRead("expenses", uid, snap.size())
            return snap.documents.mapNotNull { snapshotToExpense(it) }
        }

    fun addExpense(expense: Expense) {
        val uid = userId()
        val id = if (expense.id != 0L) expense.id else nextId()
        val toSave = expense.copy(id = id)
        FirebaseProvider.await(
            FirebaseProvider.expenses(uid).document(id.toString()).set(expenseToMap(toSave, uid))
        )
        FirebaseProvider.logWrite("insert", "expenses", uid, id.toString())
        Log.d(TAG, "Inserted expense id=$id name=${expense.name} userId=$uid")
    }

    fun updateExpense(index: Int, expense: Expense): Boolean {
        val current = expenses
        if (index < 0 || index >= current.size) {
            return false
        }
        return updateExpenseById(current[index].id, expense)
    }

    fun updateExpenseById(id: Long, expense: Expense): Boolean {
        val uid = userId()
        if (id <= 0L) return false
        val existing = FirebaseProvider.await(FirebaseProvider.expenses(uid).document(id.toString()).get())
        if (!existing.exists()) return false
        val toSave = expense.copy(id = id)
        FirebaseProvider.await(
            FirebaseProvider.expenses(uid).document(id.toString()).set(expenseToMap(toSave, uid))
        )
        FirebaseProvider.logWrite("update", "expenses", uid, id.toString())
        Log.d(TAG, "Updated expense id=$id name=${expense.name} userId=$uid")
        return true
    }

    fun deleteExpense(index: Int): Boolean {
        val current = expenses
        if (index < 0 || index >= current.size) {
            return false
        }
        return deleteExpenseById(current[index].id)
    }

    fun deleteExpenseById(id: Long): Boolean {
        val uid = userId()
        val ref = FirebaseProvider.expenses(uid).document(id.toString())
        val existing = FirebaseProvider.await(ref.get())
        if (!existing.exists()) return false
        FirebaseProvider.await(ref.delete())
        FirebaseProvider.logWrite("delete", "expenses", uid, id.toString())
        Log.d(TAG, "Deleted expense id=$id userId=$uid")
        return true
    }

    fun expenseById(id: Long): Expense? {
        val uid = userId()
        val doc = FirebaseProvider.await(FirebaseProvider.expenses(uid).document(id.toString()).get())
        return snapshotToExpense(doc)
    }

    fun formatShortDate(millis: Long): String = ExpenseLogic.formatShortDate(millis)

    fun expenseSubtitle(expense: Expense): String = ExpenseLogic.expenseSubtitle(expense)

    fun expensesInMonth(year: Int, month: Int): List<Expense> {
        return ExpenseLogic.expensesInMonth(expenses, year, month)
    }

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

    fun expensesInRange(startMillis: Long, endMillis: Long): List<Expense> {
        val start = ExpenseLogic.startOfDay(startMillis)
        val end = ExpenseLogic.endOfDay(endMillis)
        val uid = userId()
        val snap = FirebaseProvider.await(
            FirebaseProvider.expenses(uid)
                .whereGreaterThanOrEqualTo("dateCreated", start)
                .whereLessThanOrEqualTo("dateCreated", end)
                .get()
        )
        FirebaseProvider.logRead("expenses(range)", uid, snap.size())
        return snap.documents.mapNotNull { snapshotToExpense(it) }
            .sortedByDescending { it.dateCreated }
    }

    fun totalsByCategoryInRange(startMillis: Long, endMillis: Long): Map<String, Double> {
        return expensesInRange(startMillis, endMillis)
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    fun totalForScreen(
        year: Int,
        month: Int,
        category: String?,
        searchQuery: String? = null,
        rangeStartMillis: Long? = null,
        rangeEndMillis: Long? = null
    ): Double {
        return ExpenseLogic.totalAmount(
            expensesForScreen(year, month, category, searchQuery, rangeStartMillis, rangeEndMillis)
        )
    }

    fun startOfDay(millis: Long): Long = ExpenseLogic.startOfDay(millis)

    fun endOfDay(millis: Long): Long = ExpenseLogic.endOfDay(millis)

    fun filterMonths(): List<Pair<Int, Int>> {
        return (Calendar.DECEMBER downTo Calendar.JANUARY).map { month ->
            2026 to month
        }
    }

    fun monthTitle(year: Int, month: Int): String = ExpenseLogic.monthTitle(year, month)

    fun formatAmount(amount: Double): String = ExpenseLogic.formatAmount(amount)

    fun formatDate(millis: Long): String = ExpenseLogic.formatDate(millis)

    fun parseDate(text: String): Long = ExpenseLogic.parseDate(text)

    fun dateOn(year: Int, month: Int, day: Int): Long = ExpenseLogic.dateOn(year, month, day)

    data class MonthlyGoal(
        val year: Int,
        val month: Int,
        val minGoal: Double,
        val maxGoal: Double
    )

    fun monthlyGoals(year: Int, month: Int): Pair<Double, Double>? {
        val goal = resolveMonthlyGoals(year, month) ?: return null
        return goal.minGoal to goal.maxGoal
    }

    fun setMonthlyGoals(year: Int, month: Int, minGoal: Double, maxGoal: Double) {
        val uid = userId()
        val docId = "${year}_$month"
        val data = mapOf(
            "year" to year,
            "month" to month,
            "minGoal" to minGoal,
            "maxGoal" to maxGoal
        )
        FirebaseProvider.await(FirebaseProvider.monthlyGoals(uid).document(docId).set(data))
        FirebaseProvider.logWrite("upsert", "monthlyGoals", uid, docId)

        filterMonths().forEach { (otherYear, otherMonth) ->
            val otherId = "${otherYear}_$otherMonth"
            val existing = FirebaseProvider.await(FirebaseProvider.monthlyGoals(uid).document(otherId).get())
            if (!existing.exists()) {
                FirebaseProvider.await(
                    FirebaseProvider.monthlyGoals(uid).document(otherId).set(
                        mapOf(
                            "year" to otherYear,
                            "month" to otherMonth,
                            "minGoal" to minGoal,
                            "maxGoal" to maxGoal
                        )
                    )
                )
            }
        }

        Log.d(TAG, "Saved monthly goals userId=$uid year=$year month=$month min=$minGoal max=$maxGoal")
    }

    private fun resolveMonthlyGoals(year: Int, month: Int): MonthlyGoal? {
        val uid = userId()
        val exact = FirebaseProvider.await(
            FirebaseProvider.monthlyGoals(uid).document("${year}_$month").get()
        )
        if (exact.exists()) {
            return MonthlyGoal(
                year = year,
                month = month,
                minGoal = exact.getDouble("minGoal") ?: 0.0,
                maxGoal = exact.getDouble("maxGoal") ?: 0.0
            )
        }
        val all = FirebaseProvider.await(FirebaseProvider.monthlyGoals(uid).get())
        val latest = all.documents.maxByOrNull {
            val y = it.getLong("year") ?: 0L
            val m = it.getLong("month") ?: 0L
            y * 12 + m
        } ?: return null
        return MonthlyGoal(
            year = (latest.getLong("year") ?: 0L).toInt(),
            month = (latest.getLong("month") ?: 0L).toInt(),
            minGoal = latest.getDouble("minGoal") ?: 0.0,
            maxGoal = latest.getDouble("maxGoal") ?: 0.0
        )
    }
}
