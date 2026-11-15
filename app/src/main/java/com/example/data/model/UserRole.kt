package com.example.data.model

enum class UserRole(
    val title: String,
    val description: String,
    val badge: String
) {
    PATIENT(
        title = "Patient",
        description = "Book specialists, call ambulance, access lab reports & insurance",
        badge = "Patient Access"
    ),
    SPECIALIST(
        title = "Specialist Doctor",
        description = "Manage appointments, consultations, prescriptions & patient charts",
        badge = "Specialist MD"
    ),
    AMBULANCE_DISPATCHER(
        title = "Ambulance Dispatcher",
        description = "Emergency SOS alerts, ambulance fleet routing & hospital triage",
        badge = "EMS Dispatch"
    ),
    DIAGNOSTICS_TECH(
        title = "Diagnostics Lab Tech",
        description = "Manage diagnostic test slots, sample pick-ups & lab report uploads",
        badge = "Diagnostics Lab"
    ),
    INSURANCE_OFFICER(
        title = "Health Insurance Officer",
        description = "Adjudicate cashless claims, policy coverage & hospital approvals",
        badge = "Insurance Desk"
    );

    companion object {
        fun fromString(name: String?): UserRole {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PATIENT
        }
    }
}
