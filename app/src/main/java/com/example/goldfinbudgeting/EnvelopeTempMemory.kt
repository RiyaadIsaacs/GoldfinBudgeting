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

// It now reads / writes Room categories and calculates spent from expenses
// Public APIs stayed similar so Home / Envelopes / Edit screens keep working
object EnvelopeTempMemory {
    private const val TAG = "EnvelopeTempMemory"

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    // Cached Room database after bind() / first use
    @Volatile
    private var database: GoldfinDatabase? = null

    // Application context from GoldfinApp
    @Volatile
    private var appContext: Context? = null

    // Returns the shared Room database, creating it if needed
    private fun db(context: Context? = appContext): GoldfinDatabase {
        database?.let { return it }
        val ctx = context ?: appContext
            ?: throw IllegalStateException("EnvelopeTempMemory used before GoldfinApp initialised Room")
        return DatabaseProvider.get(ctx).also { database = it }
    }

    // Called from GoldfinApp so categories are available before any Activity opens
    fun bind(context: Context) {
        appContext = context.applicationContext
        database = DatabaseProvider.get(context)
    }

    // Live list of envelopes from Room
    // spent is calculated with SUM(amount) for matching expense category names
    val envelopes: List<Envelope>
        get() {
            val expenseDao = db().expenseDao()
            return db().categoryDao().getAll().map { entity ->
                val spent = expenseDao.sumForCategory(entity.name)
                EntityMappers.toEnvelope(entity, spent)
            }
        }

    // Insert a new category / envelope. id 0 lets Room auto-generate the primary key
    fun addEnvelope(envelope: Envelope) {
        val id = db().categoryDao().insert(EntityMappers.toCategoryEntity(envelope.copy(id = 0)))
        Log.d(TAG, "Inserted category id=$id name=${envelope.name}")
    }

    // Delete one envelope from Room
    // Prefer the Room id when present; otherwise look the category up by name
    fun deleteEnvelope(envelope: Envelope) {
        val entity = if (envelope.id != 0L) {
            EntityMappers.toCategoryEntity(envelope)
        } else {
            db().categoryDao().getByName(envelope.name)
                ?: EntityMappers.toCategoryEntity(envelope)
        }
        db().categoryDao().delete(entity)
        Log.d(TAG, "Deleted category id=${entity.id} name=${entity.name}")
    }

    // Only envelopes whose created date is in that calendar month
    fun envelopesInMonth(year: Int, month: Int): List<Envelope> {
        return envelopes.filter { envelope ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = envelope.dateCreated
            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    // Months for the envelopes dropdown: fixed demo months plus months that have envelopes
    fun filterMonths(): List<Pair<Int, Int>> {
        val months = linkedSetOf(
            2026 to Calendar.AUGUST,
            2026 to Calendar.JULY,
            2026 to Calendar.JUNE,
            2026 to Calendar.MAY
        )

        envelopes.forEach { envelope ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = envelope.dateCreated
            months.add(calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH))
        }

        return months.sortedWith(
            compareByDescending<Pair<Int, Int>> { it.first }
                .thenByDescending { it.second }
        )
    }

    // Human-readable month title, e.g. "August 2026"
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
