package com.example.data.model

data class NearbyFacility(
    val id: String,
    val name: String,
    val facilityType: String, // HOSPITAL, DIAGNOSTIC_CENTER
    val address: String,
    val distanceKm: Double,
    val rating: Double,
    val reviewCount: Int,
    val isOpen24x7: Boolean,
    val hasEmergencyUnit: Boolean,
    val providesHomeCollection: Boolean,
    val departments: List<String>,
    val waitTimeMinutes: Int
)

data class MedicalSpecialtyItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val doctorCount: Int
)

enum class HealthServiceCategory(
    val title: String,
    val subtitle: String
) {
    SPECIALIST_CONSULT("Doctor Visit", "Book specialists"),
    LAB_DIAGNOSTICS("Lab & Scans", "Home collection"),
    EMERGENCY_EMS("Ambulance", "Rapid 24/7 SOS"),
    HEALTH_INSURANCE("Insurance", "Cashless claims"),
    PROCEDURE_GUIDES("Procedures", "Prep & recovery"),
    HEALTH_LOCKER("Health Vault", "Digital records")
}
