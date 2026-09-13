package com.example.goldfinbudgeting

//data class for envelopes. not an activity, but a Kotlin Class/File
data class Envelope (

    val name: String,

    val min: Double,

    val max: Double,

    val spent: Double,

    val dateCreated: Long
)
