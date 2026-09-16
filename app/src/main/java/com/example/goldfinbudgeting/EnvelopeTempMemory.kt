package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider
import com.example.goldfinbudgeting.data.EntityMappers
import com.example.goldfinbudgeting.data.GoldfinDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// It now reads and writes Room categories and calculates spent from expenses
// Public APIs stayed similar so Home Envelopes and Edit screens keep working
object EnvelopeTempMemory {
    private const val TAG = "EnvelopeTempMemory"

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    // Cached Room database after bind or first use
    @Volatile
    private var database: GoldfinDatabase? = null

    // Application context from GoldfinApp
    @Volatile
    private var appContext: Context? = null

    // Returns the shared Room database creating it if needed
    private fun db(context: Context? = appContext): GoldfinDatabase {
        database?.let { return it }
        val ctx = context ?: appContext?: throw IllegalStateException("EnvelopeTempMemory used before GoldfinApp initialised Room")

        return DatabaseProvider.get(ctx).also { database = it }
    }

    // Per user helpers
    // every envelope read and write uses the signed in accounts id
    private fun requireContext(): Context {
        return appContext
            ?: throw IllegalStateException("EnvelopeTempMemory used before GoldfinApp initialised Room")
    }

    private fun userId(): Long {
        return AccountStore.currentUserId(requireContext())
    }

    // Called from GoldfinApp so categories are available before any activity opens
    fun bind(context: Context) {
        appContext = context.applicationContext
        database = DatabaseProvider.get(context)
    }

    // Live list of envelopes from Room for the current account
    // spent is calculated with SUM amount for matching expense category names
    val envelopes: List<Envelope>
        get() {
            val uid = userId()
            val expenseDao = db().expenseDao()
            return db().categoryDao().getAllForUser(uid).map { entity ->
                val spent = expenseDao.sumForCategory(uid, entity.name)
                EntityMappers.toEnvelope(entity, spent)
            }
        }

    // Insert a new envelope. id 0 lets Room auto generate the primary key
    fun addEnvelope(envelope: Envelope) {
        val uid = userId()
        val id = db().categoryDao().insert(EntityMappers.toCategoryEntity(envelope.copy(id = 0), uid))
        Log.d(TAG, "Inserted category id=$id name=${envelope.name} userId=$uid")
    }

    // Delete one envelope from Room
    // Prefer the Room id when present; otherwise look the category up by name
    fun deleteEnvelope(envelope: Envelope) {
        val uid = userId()
        val entity = if (envelope.id != 0L) {
            EntityMappers.toCategoryEntity(envelope, uid)
        } else {
            db().categoryDao().getByName(uid, envelope.name) ?: EntityMappers.toCategoryEntity(envelope, uid)
        }

        db().categoryDao().delete(entity)

        Log.d(TAG, "Deleted category id=${entity.id} name=${entity.name} userId=$uid")
    }

    // Only envelopes whose created date is in that calendar month
    fun envelopesInMonth(year: Int, month: Int): List<Envelope> {
        return envelopes.filter { envelope ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = envelope.dateCreated
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    // Months for the envelopes dropdown
    // Demo months stay for account 1; other accounts only see months they have data for
    fun filterMonths(): List<Pair<Int, Int>> {
        val months = linkedSetOf<Pair<Int, Int>>()

        if (AccountStore.isDemoAccount(requireContext())) {
            months.add(2026 to Calendar.AUGUST)
            months.add(2026 to Calendar.JULY)
            months.add(2026 to Calendar.JUNE)
            months.add(2026 to Calendar.MAY)
        }

        envelopes.forEach { envelope ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = envelope.dateCreated
            months.add(calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH))
        }

        // If a new account has no envelopes yet, still show the current month
        if (months.isEmpty()) {
            val now = Calendar.getInstance()
            months.add(now.get(Calendar.YEAR) to now.get(Calendar.MONTH))
        }

        return months.sortedWith(
            compareByDescending<Pair<Int, Int>> { it.first }
                .thenByDescending { it.second }
        )
    }

    // Human-readable month title, like August 2026
    fun monthTitle(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return monthFormat.format(calendar.time)
    }

    // Format millis as yyyy/MM/dd for edit fields
    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    // Parse typed date text back to millis
    fun parseDate(text: String): Long {
        return try {
            dateFormat.parse(text)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    // Build millis for a specific day
    fun dateOn(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }
}
