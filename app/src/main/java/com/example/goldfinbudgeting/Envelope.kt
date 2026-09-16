package com.example.goldfinbudgeting

//UI model for one envelope/budget category card
//spent is calculated from expenses so progress bars stay accurate
data class Envelope(
    //category name on card
    val name: String,

    //minimum spending goal for category
    val min: Double,

    //maximum spending goal for category
    val max: Double,

    //how much has been spent
    val spent: Double,

    //when envelope was created. used for month filtering
    val dateCreated: Long,

    //room row id. 0 means not saved yet when adding a new envelope
    val id: Long = 0
)
