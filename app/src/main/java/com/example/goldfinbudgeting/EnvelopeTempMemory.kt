package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.FirebaseProvider
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

// EnvelopeTempMemory reads/writes Firestore categories and calculates spent from expenses
object EnvelopeTempMemory {
    private const val TAG = "EnvelopeTempMemory"

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val idCounter = AtomicLong(System.currentTimeMillis())

    @Volatile
    private var appContext: Context? = null

    private fun requireContext(): Context {
        return appContext
            ?: throw IllegalStateException("EnvelopeTempMemory used before GoldfinApp initialised Firebase")
    }

    private fun userId(): String {
        val uid = AccountStore.currentUserId(requireContext())
        if (uid.isBlank()) {
            throw IllegalStateException("No signed-in Firebase user for envelopes")
        }
        return uid
    }

    // Called from GoldfinApp so categories are available before any activity opens
    fun bind(context: Context) {
        appContext = context.applicationContext
    }

    // Live list of envelopes from Room for the current account
    // spent is calculated with SUM amount for matching expense category names
    private fun nextId(): Long = idCounter.incrementAndGet()

    private fun snapshotToEnvelope(doc: DocumentSnapshot, spent: Double): Envelope? {
        if (!doc.exists()) return null
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        return Envelope(
            name = doc.getString("name").orEmpty(),
            min = doc.getDouble("minGoal") ?: 0.0,
            max = doc.getDouble("maxGoal") ?: 0.0,
            spent = spent,
            dateCreated = doc.getLong("dateCreated") ?: 0L,
            id = id
        )
    }

    private fun envelopeToMap(envelope: Envelope, uid: String): Map<String, Any?> {
        return mapOf(
            "id" to envelope.id,
            "userId" to uid,
            "name" to envelope.name,
            "minGoal" to envelope.min,
            "maxGoal" to envelope.max,
            "dateCreated" to envelope.dateCreated
        )
    }

    private fun sumForCategory(uid: String, categoryName: String): Double {
        val snap = FirebaseProvider.await(
            FirebaseProvider.expenses(uid).whereEqualTo("categoryName", categoryName).get()
        )
        return snap.documents.sumOf { it.getDouble("amount") ?: 0.0 }
    }

    val envelopes: List<Envelope>
        get() {
            val uid = userId()
            val snap = FirebaseProvider.await(FirebaseProvider.categories(uid).get())
            FirebaseProvider.logRead("categories", uid, snap.size())
            return snap.documents.mapNotNull { doc ->
                val name = doc.getString("name").orEmpty()
                val spent = sumForCategory(uid, name)
                snapshotToEnvelope(doc, spent)
            }.sortedByDescending { it.dateCreated }
        }

    // Insert a new envelope
    fun addEnvelope(envelope: Envelope) {
        val uid = userId()
        val id = if (envelope.id != 0L) envelope.id else nextId()
        val toSave = envelope.copy(id = id)
        FirebaseProvider.await(
            FirebaseProvider.categories(uid).document(id.toString()).set(envelopeToMap(toSave, uid))
        )
        FirebaseProvider.logWrite("insert", "categories", uid, id.toString())
        Log.d(TAG, "Inserted category id=$id name=${envelope.name} userId=$uid")
    }

    // Delete one envelope 
    fun deleteEnvelope(envelope: Envelope) {
        val uid = userId()
        val docId = if (envelope.id != 0L) {
            envelope.id.toString()
        } else {
            val snap = FirebaseProvider.await(
                FirebaseProvider.categories(uid).whereEqualTo("name", envelope.name).limit(1).get()
            )
            snap.documents.firstOrNull()?.id ?: return
        }
        FirebaseProvider.await(FirebaseProvider.categories(uid).document(docId).delete())
        FirebaseProvider.logWrite("delete", "categories", uid, docId)
        Log.d(TAG, "Deleted category id=$docId name=${envelope.name} userId=$uid")
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
    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

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
