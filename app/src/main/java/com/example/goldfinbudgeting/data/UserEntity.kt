package com.example.goldfinbudgeting.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// UserEntity maps to the users SQLite table used for login
@Entity(tableName = "users")
data class UserEntity(
    // Room generates a unique id for each new row when autoGenerate is true
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Login name the user types on the login / profile screens
    val username: String,

    // Plain-text password 
    val password: String
)
