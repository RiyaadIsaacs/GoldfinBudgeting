package com.example.goldfinbudgeting

// UI model for one expense row shown in Activities / adapters
// id links back to the Room primary key after an expense is loaded from the database
data class Expense(
    // Short title shown in the list
    val name: String,
    // Amount spent
    val amount: Double,
    // Category / envelope name this expense belongs to
    val category: String,
    // Expense date as epoch millis
    val dateCreated: Long,
    // Optional notes
    val description: String = "",
    // Optional start time text
    val startTime: String = "",
    // Optional end time text
    val endTime: String = "",
    // Optional receipt photo path / URI string
    val receiptPath: String? = null,
    // Room row id. 0 means "not saved yet" when creating a new expense in the form
    val id: Long = 0
)
