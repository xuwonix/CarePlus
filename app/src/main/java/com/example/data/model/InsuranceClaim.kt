package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class InsuranceClaim(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val policyNumber: String = "",
    val insuranceProvider: String = "",
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

@Keep
@IgnoreExtraProperties
data class UserInsurancePolicy(
    val id: String = "",
    val insurerName: String = "",
    val planName: String = "",
    val policyNumber: String = "",
    val policyType: String = "Family Floater", // Individual, Family Floater, Corporate, Govt. Scheme (PM-JAY / MMAAY)
    val sumInsured: Double = 500000.0,
    val validUntil: String = "",
    val coveredMembers: String = "Self",
    val cardAttachmentPath: String = "",
    val cardAttachmentName: String = "",
    val cardFileType: String = ""
)

data class InsurancePartner(
    val id: String,
    val name: String,
    val tagline: String,
    val featuredPlan: String,
    val startingAnnualPremium: Int, // INR per year, 0 for Govt schemes
    val coverRange: String,
    val cashlessHospitalsLabel: String,
    val claimSettlementRatio: String,
    val highlights: List<String>,
    val isGovtScheme: Boolean = false,
    val advisorName: String,
    val advisorPhone: String
)
