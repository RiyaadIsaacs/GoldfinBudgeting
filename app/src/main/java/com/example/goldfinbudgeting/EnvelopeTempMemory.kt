package com.example.goldfinbudgeting

//stores info from evelopes temporarily in memory. will replace when using local database
object EnvelopeTempMemory {
    val envelopes = mutableListOf(

        Envelope("Subscriptions", 800.00, 1000.00, 750.00),

        Envelope("Groceries", 1500.00, 2000.00, 1200.00),

        Envelope("Takeouts", 600.00, 1000.00, 400.00),

        Envelope("Games", 600.00, 2000.00, 2000.00),

        Envelope("Car", 4000.00, 10000.00, 2800.00)
    )

    //remove one envelope from the temp list
    fun deleteEnvelope(envelope: Envelope) {
        envelopes.remove(envelope)
    }
}