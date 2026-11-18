package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Appointment(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val familyMemberName: String = "Self",
    val specialistId: String = "",
    val specialistName: String = "",
    val specialty: String = "",
    val clinicHospital: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val mode: String = "IN_CLINIC", // IN_CLINIC, VIDEO_CALL, CHAT
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED
    val notes: String = "",
    val prescription: String = "",
    val consultationFee: Double = 0.0,
    val paymentStatus: String = "PAID",
    val paymentMethod: String = "UPI / Card",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
