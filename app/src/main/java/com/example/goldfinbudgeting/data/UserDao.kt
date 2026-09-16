package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// UserDao = Room data-access methods for the users table
// Activities talk to AccountStore; AccountStore calls these DAO methods
@Dao
interface UserDao {

    // Insert a new user row. ABORT means insert fails if a conflict happens
    // Returns the new auto-generated id
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(user: UserEntity): Long

    // Look up one user by username. used when loading the current account
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun findByUsername(username: String): UserEntity?

    // Login check. both username and password must match a row
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun authenticate(username: String, password: String): UserEntity?

    // Save changes to an existing user
    @Update
    fun update(user: UserEntity)

    // How many users exist, used by DatabaseProvider to decide whether to seed the default 1/1 user
    @Query("SELECT COUNT(*) FROM users")
    fun count(): Int
}
