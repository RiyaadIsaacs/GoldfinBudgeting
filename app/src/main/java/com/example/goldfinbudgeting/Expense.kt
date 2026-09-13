package com.example.goldfinbudgeting

//data class for expenses. not an activity, but a Kotlin Class/File
data class Expense(

    val name: String,

    val amount: Double,

    val category: String,

    val dateCreated: Long,

    val description: String = "",

    val startTime: String = "",

    val endTime: String = "",

    val receiptPath: String? = null
)
