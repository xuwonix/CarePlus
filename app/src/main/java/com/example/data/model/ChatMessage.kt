package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val consultationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "PATIENT",
    val recipientId: String = "",
    val recipientName: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val messageType: String = "TEXT", // TEXT, CLINICAL_UPDATE, PRESCRIPTION
    @ServerTimestamp val createdAt: Timestamp? = null
)
