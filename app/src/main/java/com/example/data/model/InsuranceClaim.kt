package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class InsuranceClaim(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val policyNumber: String = "",
    val insuranceProvider: String = "BlueCross Shield Health",
    val hospitalName: String = "",
    val procedureName: String = "",
    val claimedAmount: Double = 0.0,
    val approvedAmount: Double = 0.0,
    val status: String = "SUBMITTED", // SUBMITTED, UNDER_REVIEW, APPROVED, SETTLED
    val claimType: String = "CASHLESS", // CASHLESS, REIMBURSEMENT
    val documentReference: String = "",
    val reviewerNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
