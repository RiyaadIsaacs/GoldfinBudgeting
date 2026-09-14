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

    // All categories, newest first — drives Home / Envelopes lists
    @Query("SELECT * FROM categories ORDER BY dateCreated DESC, id DESC")
    fun getAll(): List<CategoryEntity>

    // Find one category by its display name
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    fun getByName(name: String): CategoryEntity?

    // Row count — used to seed sample envelopes only when the table is empty
    @Query("SELECT COUNT(*) FROM categories")
    fun count(): Int
}
