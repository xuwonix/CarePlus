package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MedicalRecord(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val memberName: String = "Self",
    val title: String = "",
    val category: String = "PRESCRIPTION", // PRESCRIPTION, LAB_REPORT, RADIOLOGY, DISCHARGE_SUMMARY, VITALS
    val doctorOrFacility: String = "",
    val recordDate: String = "",
    val summary: String = "",
    val keyMetrics: String = "",
    val fileType: String = "PDF",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
