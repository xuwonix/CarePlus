package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class DiagnosticTest(
    val id: String,
    val name: String,
    val category: String, // BLOOD_WORK, RADIOLOGY, CARDIOLOGY, PATHOLOGY
    val sampleType: String,
    val turnaroundTime: String,
    val preparationSummary: String,
    val price: Double
)

data class DiagnosticCenter(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double,
    val rating: Double,
    val providesHomeCollection: Boolean,
    val accreditation: String = "NABL & CAP Accredited",
    val operatingHours: String = "06:30 AM - 09:00 PM",
    val testsOffered: List<DiagnosticTest>
)

@Keep
@IgnoreExtraProperties
data class DiagnosticBooking(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val familyMemberName: String = "Self",
    val centerId: String = "",
    val centerName: String = "",
    val testId: String = "",
    val testName: String = "",
    val testCategory: String = "BLOOD_WORK",
    val scheduledDate: String = "",
    val scheduledTime: String = "08:30 AM",
    val collectionType: String = "HOME_COLLECTION", // HOME_COLLECTION, LAB_VISIT
    val collectionAddress: String = "",
    val status: String = "BOOKED", // BOOKED, SAMPLE_COLLECTED, RESULTS_READY, CANCELLED
    val resultSummary: String = "",
    val abnormalFlag: Boolean = false,
    val cost: Double = 0.0,
    val paymentStatus: String = "PAID",
    val paymentMethod: String = "UPI / Card",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
