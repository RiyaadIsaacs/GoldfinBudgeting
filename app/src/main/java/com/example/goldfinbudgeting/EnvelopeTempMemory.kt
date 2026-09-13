package com.example.goldfinbudgeting

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//stores info from evelopes temporarily in memory. will replace when using local database
object EnvelopeTempMemory {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    val envelopes = mutableListOf(

        Envelope("Subscriptions", 800.00, 1000.00, 750.00, dateOn(2026, Calendar.AUGUST, 1)),

        Envelope("Groceries", 1500.00, 2000.00, 1200.00, dateOn(2026, Calendar.AUGUST, 1)),

        Envelope("Takeouts", 600.00, 1000.00, 400.00, dateOn(2026, Calendar.AUGUST, 1)),

        Envelope("Games", 600.00, 2000.00, 2000.00, dateOn(2026, Calendar.AUGUST, 1)),

        Envelope("Car", 4000.00, 10000.00, 2800.00, dateOn(2026, Calendar.AUGUST, 1))
    )

    //remove one envelope from the temp list
    fun deleteEnvelope(envelope: Envelope) {
        envelopes.remove(envelope)
    }

    //only envelopes whose created date is in that month
    fun envelopesInMonth(year: Int, month: Int): List<Envelope> {
        return envelopes.filter { envelope ->
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = envelope.dateCreated

            calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
        }
    }

    //months shown in the envelopes dropdown. Figma months plus any month that has an envelope
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

    fun monthTitle(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return monthFormat.format(calendar.time)
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
