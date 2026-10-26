package com.example.data.model

object BloodCompatibilityUtil {
    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    fun canDonateTo(bloodGroup: String): List<String> {
        return when (bloodGroup.trim().uppercase()) {
            "O-" -> listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+") // Universal donor
            "O+" -> listOf("O+", "A+", "B+", "AB+")
            "A-" -> listOf("A-", "A+", "AB-", "AB+")
            "A+" -> listOf("A+", "AB+")
            "B-" -> listOf("B-", "B+", "AB-", "AB+")
            "B+" -> listOf("B+", "AB+")
            "AB-" -> listOf("AB-", "AB+")
            "AB+" -> listOf("AB+") // Only AB+
            else -> emptyList()
        }
    }

    fun canReceiveFrom(bloodGroup: String): List<String> {
        return when (bloodGroup.trim().uppercase()) {
            "AB+" -> listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+") // Universal recipient
            "AB-" -> listOf("O-", "A-", "B-", "AB-")
            "A+" -> listOf("O-", "O+", "A-", "A+")
            "A-" -> listOf("O-", "A-")
            "B+" -> listOf("O-", "O+", "B-", "B+")
            "B-" -> listOf("O-", "B-")
            "O+" -> listOf("O-", "O+")
            "O-" -> listOf("O-") // Only O-
            else -> emptyList()
        }
    }

    fun getCompatibilityNote(bloodGroup: String): String {
        return when (bloodGroup.trim().uppercase()) {
            "O-" -> "Universal Red Cell Donor: Can give blood to any patient in critical emergency!"
            "AB+" -> "Universal Recipient: Can safely receive red blood cells from any blood group!"
            "O+" -> "Most Common Donor: Can donate to all positive blood types (approx. 85% of people)."
            "A+" -> "Second Most Common: High demand in hospitals and emergency trauma units."
            "B+" -> "High demand for thalassemia and surgery patients across South Asia."
            "AB-" -> "Rare Blood Type: Very valuable for emergency platelets and plasma donations."
            "A-" -> "Rare Type: Can help A+, A-, AB+, and AB- patients."
            "B-" -> "Rare Type: Vital for B+, B-, AB+, and AB- patients."
            else -> "Regular donations save up to 3 lives per unit!"
        }
    }
}
