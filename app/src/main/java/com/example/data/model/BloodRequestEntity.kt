package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_requests")
data class BloodRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientName: String,
    val bloodGroup: String,
    val unitsNeeded: Int = 1,
    val hospitalName: String,
    val hospitalLocation: String,
    val contactPhone: String,
    val urgency: String = "Urgent", // "Critical / Immediate", "Within 24 Hours", "Scheduled"
    val notes: String = "",
    val requesterName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFulfilled: Boolean = false
)
