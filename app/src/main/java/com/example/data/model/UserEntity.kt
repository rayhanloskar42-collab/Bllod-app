package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val password: String,
    val age: Int,
    val bloodGroup: String, // e.g., "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    val location: String,   // City / District / Area e.g. "Dhaka", "Chittagong"
    val isAvailable: Boolean = true,
    val lastDonationDate: String = "",
    val totalDonations: Int = 0,
    val registeredAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}
