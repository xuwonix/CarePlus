package com.example.data.model

data class Specialist(
    val id: String,
    val name: String,
    val title: String,
    val specialty: String,
    val experienceYears: Int,
    val rating: Double,
    val reviewCount: Int,
    val clinicHospital: String,
    val hospitalId: String,
    val address: String,
    val fee: Double,
    val nextAvailableSlot: String,
    val bio: String,
    val qualifications: String,
    val languages: List<String> = listOf("English"),
    val offersVideoConsult: Boolean = true,
    val availableDays: List<String> = listOf("Today", "Tomorrow", "Fri Nov 20", "Sat Nov 21"),
    val timeSlots: List<String> = listOf("09:00 AM", "10:30 AM", "02:00 PM", "04:30 PM", "06:00 PM")
)
