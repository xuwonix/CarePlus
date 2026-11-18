package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class AmbulanceRequest(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val pickupAddress: String = "",
    val emergencyType: String = "CARDIAC_OR_RESPIRATORY", // TRAUMA, CARDIAC_OR_RESPIRATORY, MATERNITY, STROKE, GENERAL
    val ambulanceType: String = "ADVANCED_CARDIAC", // BASIC_LIFE_SUPPORT, ADVANCED_CARDIAC, PATIENT_TRANSPORT
    val status: String = "DISPATCHED", // DISPATCHED, EN_ROUTE, ARRIVED, COMPLETED
    val vehicleNumber: String = "AS-12-EM-1084",
    val driverName: String = "Tezpur 108 EMS Team",
    val driverPhone: String = "+91 3712-108-420",
    val etaMinutes: Long = 8L,
    val hospitalDestination: String = "Tezpur Medical College & Hospital (TMCH)",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
