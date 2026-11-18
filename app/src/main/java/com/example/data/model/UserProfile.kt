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
    val age: String = "32",
    val gender: String = "Prefer not to say",
    val bloodGroup: String = "O+",
    val allergies: String = "None reported",
    val chronicConditions: String = "None",
    val emergencyContactName: String = "Alex Morgan",
    val emergencyContactPhone: String = "+1 (555) 842-9910",
    val locationArea: String = "Downtown Metro",
    val savedAddress: String = "420 Maple Avenue, Apt 3B, Metro City",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
