package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = UserRole.PATIENT.name,
    val phone: String = "",
    val age: String = "",
    val gender: String = "Prefer not to say",
    val bloodGroup: String = "",
    val allergies: String = "",
    val chronicConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val locationArea: String = "Mission Chariali, Tezpur",
    val savedAddress: String = "",
    val onboardingCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
