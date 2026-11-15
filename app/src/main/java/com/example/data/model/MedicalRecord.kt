package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MedicalRecord(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val title: String = "",
    val category: String = "PRESCRIPTION", // PRESCRIPTION, LAB_REPORT, RADIOLOGY, DISCHARGE_SUMMARY, VACCINATION
    val doctorOrFacility: String = "",
    val recordDate: String = "",
    val summary: String = "",
    val keyMetrics: String = "", // e.g. "BP: 120/80 mmHg | Heart Rate: 72 bpm | Glucose: 94 mg/dL"
    val fileType: String = "PDF",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
