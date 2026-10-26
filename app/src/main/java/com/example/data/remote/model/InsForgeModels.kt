package com.example.data.remote.model

import com.example.data.model.BloodRequestEntity
import com.example.data.model.UserEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InsForgeUserDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "password") val password: String,
    @Json(name = "age") val age: Int,
    @Json(name = "blood_group") val bloodGroup: String,
    @Json(name = "location") val location: String,
    @Json(name = "is_available") val isAvailable: Boolean = true,
    @Json(name = "last_donation_date") val lastDonationDate: String = "",
    @Json(name = "total_donations") val totalDonations: Int = 0
) {
    fun toEntity(): UserEntity = UserEntity(
        id = id?.toInt() ?: 0,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        password = password,
        age = age,
        bloodGroup = bloodGroup,
        location = location,
        isAvailable = isAvailable,
        lastDonationDate = lastDonationDate,
        totalDonations = totalDonations
    )
}

fun UserEntity.toInsForgeDto(): InsForgeUserDto = InsForgeUserDto(
    id = if (id > 0) id.toLong() else null,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    password = password,
    age = age,
    bloodGroup = bloodGroup,
    location = location,
    isAvailable = isAvailable,
    lastDonationDate = lastDonationDate,
    totalDonations = totalDonations
)

@JsonClass(generateAdapter = true)
data class InsForgeBloodRequestDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "patient_name") val patientName: String,
    @Json(name = "blood_group") val bloodGroup: String,
    @Json(name = "units_needed") val unitsNeeded: Int = 1,
    @Json(name = "hospital_name") val hospitalName: String,
    @Json(name = "hospital_location") val hospitalLocation: String,
    @Json(name = "contact_phone") val contactPhone: String,
    @Json(name = "urgency") val urgency: String = "Critical / Immediate",
    @Json(name = "notes") val notes: String = "",
    @Json(name = "requester_name") val requesterName: String = "",
    @Json(name = "is_fulfilled") val isFulfilled: Boolean = false
) {
    fun toEntity(): BloodRequestEntity = BloodRequestEntity(
        id = id?.toInt() ?: 0,
        patientName = patientName,
        bloodGroup = bloodGroup,
        unitsNeeded = unitsNeeded,
        hospitalName = hospitalName,
        hospitalLocation = hospitalLocation,
        contactPhone = contactPhone,
        urgency = urgency,
        notes = notes,
        requesterName = requesterName,
        isFulfilled = isFulfilled
    )
}

fun BloodRequestEntity.toInsForgeDto(): InsForgeBloodRequestDto = InsForgeBloodRequestDto(
    id = if (id > 0) id.toLong() else null,
    patientName = patientName,
    bloodGroup = bloodGroup,
    unitsNeeded = unitsNeeded,
    hospitalName = hospitalName,
    hospitalLocation = hospitalLocation,
    contactPhone = contactPhone,
    urgency = urgency,
    notes = notes,
    requesterName = requesterName,
    isFulfilled = isFulfilled
)

@JsonClass(generateAdapter = true)
data class InsForgeDownloadStrategyDto(
    @Json(name = "method") val method: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "expiresAt") val expiresAt: String? = null
)
