package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FamilyMember(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val name: String = "",
    val relationship: String = "Spouse", // Spouse, Child, Parent, Sibling, Other
    val age: String = "",
    val gender: String = "Female",
    val bloodGroup: String = "O+",
    val allergies: String = "None",
    val phone: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class HomeCareService(
    val id: String,
    val title: String,
    val category: String, // NURSING, PHYSIOTHERAPY, ELDERLY_CARE, DOCTOR_VISIT, IV_THERAPY
    val duration: String,
    val description: String,
    val price: Double,
    val providerTeam: String
)

@IgnoreExtraProperties
data class HomeCareBooking(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val memberName: String = "Self",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val serviceCategory: String = "",
    val providerTeam: String = "",
    val visitAddress: String = "",
    val scheduledDate: String = "",
    val timeWindow: String = "09:00 AM - 11:00 AM",
    val notes: String = "",
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED
    val cost: Double = 0.0,
    val paymentStatus: String = "PAID",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PaymentRecord(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val title: String = "",
    val providerName: String = "",
    val category: String = "APPOINTMENT", // APPOINTMENT, DIAGNOSTIC, HOME_CARE
    val billingForName: String = "Self",
    val amount: Double = 0.0,
    val paymentMethod: String = "Card •••• 4242",
    val status: String = "PAID", // PAID, PAY_AT_COUNTER
    val transactionRef: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "BOOKING", // BOOKING, LAB, PAYMENT, EMERGENCY, SUPPORT
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Review(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val targetType: String = "DOCTOR", // DOCTOR, HOSPITAL, LAB
    val rating: Int = 5,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val patientId: String = "",
    val category: String = "Booking Issue",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED
    @ServerTimestamp val createdAt: Timestamp? = null
)
