package com.example.goldfinbudgeting

//chat message class. contains text and who sent it
data class Message(
    val text: String,

    val isFromUser: Boolean
)