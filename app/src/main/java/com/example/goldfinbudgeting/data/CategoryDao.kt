package com.example.goldfinbudgeting.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// CategoryDao = Room data-access methods for categories / envelopes
@Dao
interface CategoryDao {

    // Insert one new category. Returns the new row id
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(category: CategoryEntity): Long

    // Insert many categories at once
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(categories: List<CategoryEntity>)

    // Update an existing category
    @Update
    fun update(category: CategoryEntity)

    // Delete a category row
    @Delete
    fun delete(category: CategoryEntity)

    // All categories for one account, newest first
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY dateCreated DESC, id DESC")
    fun getAllForUser(userId: Long): List<CategoryEntity>

    // Find one category by display name for that account
    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name LIMIT 1")
    fun getByName(userId: Long, name: String): CategoryEntity?

    // Row count for one account — used when seeding demo data for user 1 only
    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    fun countForUser(userId: Long): Int
}
