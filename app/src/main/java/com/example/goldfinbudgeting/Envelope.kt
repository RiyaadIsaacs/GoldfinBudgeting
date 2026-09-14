package com.example.goldfinbudgeting

// UI model for one envelope / budget category card.
// Not a Room @Entity — Room stores this as CategoryEntity (without spent).
// spent is calculated from expenses so the progress bars stay accurate.
data class Envelope(
    // Category name shown on the card.
    val name: String,
    // Minimum spending goal for this category.
    val min: Double,
    // Maximum spending goal for this category.
    val max: Double,
    // How much has been spent so far (summed from matching expenses, not stored in Room).
    val spent: Double,
    // When the envelope was created (epoch millis) — used for month filtering.
    val dateCreated: Long,
    // Room row id. 0 means "not saved yet" when adding a new envelope.
    val id: Long = 0
)
