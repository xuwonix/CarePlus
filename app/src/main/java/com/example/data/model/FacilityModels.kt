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
    val waitTimeMinutes: Int,
    val phone: String = "+91 3712-241305",
    val accreditation: String = "NABH & NABL Accredited",
    val acceptsCashlessInsurance: Boolean = true
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
    HOME_CARE("Home Care", "Nurse & physio"),
    EMERGENCY_EMS("Ambulance", "Rapid 24/7 SOS"),
    HEALTH_INSURANCE("Insurance", "Cashless claims"),
    PROCEDURE_GUIDES("Procedures", "Prep & recovery")
}
