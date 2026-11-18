package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AmbulanceRequest
import com.example.data.model.AppNotification
import com.example.data.model.Appointment
import com.example.data.model.ChatMessage
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.FamilyMember
import com.example.data.model.HomeCareBooking
import com.example.data.model.HomeCareService
import com.example.data.model.InsuranceClaim
import com.example.data.model.MedicalRecord
import com.example.data.model.PaymentRecord
import com.example.data.model.Review
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HealthcareRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing CarePulse data.")
    }

    fun getCurrentUser() = auth.currentUser

    // -------------------------------------------------------------
    // PATIENT PROFILE & LOCATION
    // -------------------------------------------------------------
    suspend fun getOrCreateUserProfile(): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(UserProfile::class.java) ?: UserProfile(
                userId = uid,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.displayName ?: "CarePulse Patient",
                role = UserRole.PATIENT.name
            )
        } else {
            val profile = UserProfile(
                userId = uid,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.displayName ?: "CarePulse Patient",
                role = UserRole.PATIENT.name,
                phone = "+1 (555) 392-1084"
            )
            docRef.set(
                mapOf(
                    "userId" to profile.userId,
                    "email" to profile.email,
                    "displayName" to profile.displayName,
                    "role" to UserRole.PATIENT.name,
                    "phone" to profile.phone,
                    "age" to profile.age,
                    "gender" to profile.gender,
                    "bloodGroup" to profile.bloodGroup,
                    "allergies" to profile.allergies,
                    "chronicConditions" to profile.chronicConditions,
                    "emergencyContactName" to profile.emergencyContactName,
                    "emergencyContactPhone" to profile.emergencyContactPhone,
                    "locationArea" to profile.locationArea,
                    "savedAddress" to profile.savedAddress,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return profile
        }
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        db.collection("users").document(uid)
            .snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { error ->
                Log.w("HealthcareRepo", "Error observing profile: ${error.message}")
                emit(null)
            }
            .collect { emit(it) }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        db.collection("users").document(uid).update(
            mapOf(
                "displayName" to profile.displayName,
                "phone" to profile.phone,
                "age" to profile.age,
                "gender" to profile.gender,
                "bloodGroup" to profile.bloodGroup,
                "allergies" to profile.allergies,
                "chronicConditions" to profile.chronicConditions,
                "emergencyContactName" to profile.emergencyContactName,
                "emergencyContactPhone" to profile.emergencyContactPhone,
                "savedAddress" to profile.savedAddress,
                "locationArea" to profile.locationArea,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateLocation(area: String, address: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).update(
            mapOf(
                "locationArea" to area,
                "savedAddress" to address,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // FAMILY PROFILES
    // -------------------------------------------------------------
    fun observeFamilyMembers(): Flow<List<FamilyMember>> = flow {
        val uid = requireUserId()
        db.collection("family_profiles")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(FamilyMember::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing family members: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun addFamilyMember(member: FamilyMember) {
        val uid = requireUserId()
        val docRef = db.collection("family_profiles").document()
        docRef.set(
            mapOf(
                "patientId" to uid,
                "name" to member.name,
                "relationship" to member.relationship,
                "age" to member.age,
                "gender" to member.gender,
                "bloodGroup" to member.bloodGroup,
                "allergies" to member.allergies,
                "phone" to member.phone,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteFamilyMember(memberId: String) {
        db.collection("family_profiles").document(memberId).delete().await()
    }

    // -------------------------------------------------------------
    // APPOINTMENTS & PAYMENTS
    // -------------------------------------------------------------
    fun observePatientAppointments(): Flow<List<Appointment>> = flow {
        val uid = requireUserId()
        db.collection("appointments")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(Appointment::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing appointments: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun scheduleAppointment(appointment: Appointment) {
        val uid = requireUserId()
        val docRef = db.collection("appointments").document()
        val paymentStatus = if (appointment.paymentMethod.contains("Clinic", ignoreCase = true)) "PAY_AT_COUNTER" else "PAID"
        docRef.set(
            mapOf(
                "patientId" to uid,
                "patientName" to appointment.patientName.ifBlank { auth.currentUser?.displayName ?: "Patient" },
                "familyMemberName" to appointment.familyMemberName,
                "specialistId" to appointment.specialistId,
                "specialistName" to appointment.specialistName,
                "specialty" to appointment.specialty,
                "clinicHospital" to appointment.clinicHospital,
                "date" to appointment.date,
                "timeSlot" to appointment.timeSlot,
                "mode" to appointment.mode,
                "status" to "SCHEDULED",
                "notes" to appointment.notes,
                "prescription" to "",
                "consultationFee" to appointment.consultationFee,
                "paymentStatus" to paymentStatus,
                "paymentMethod" to appointment.paymentMethod,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        recordPaymentInternal(
            title = "Consultation: ${appointment.specialistName}",
            providerName = appointment.clinicHospital,
            category = "APPOINTMENT",
            billingForName = appointment.familyMemberName,
            amount = appointment.consultationFee,
            paymentMethod = appointment.paymentMethod,
            status = paymentStatus
        )

        createNotification(
            title = "Appointment Confirmed",
            message = "${appointment.specialistName} (${appointment.specialty}) booked for ${appointment.familyMemberName} on ${appointment.date} at ${appointment.timeSlot}.",
            category = "BOOKING"
        )
    }

    suspend fun rescheduleAppointment(appointmentId: String, newDate: String, newSlot: String) {
        db.collection("appointments").document(appointmentId).update(
            mapOf(
                "date" to newDate,
                "timeSlot" to newSlot,
                "status" to "SCHEDULED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Appointment Rescheduled",
            message = "Your appointment was rescheduled to $newDate at $newSlot.",
            category = "BOOKING"
        )
    }

    suspend fun cancelAppointment(appointmentId: String) {
        db.collection("appointments").document(appointmentId).update(
            mapOf(
                "status" to "CANCELLED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Appointment Cancelled",
            message = "Your consultation has been cancelled. Any online payment will be refunded in 3–5 business days.",
            category = "BOOKING"
        )
    }

    // -------------------------------------------------------------
    // DIAGNOSTIC LABS & HOME COLLECTION
    // -------------------------------------------------------------
    fun observePatientDiagnosticBookings(): Flow<List<DiagnosticBooking>> = flow {
        val uid = requireUserId()
        db.collection("diagnostic_bookings")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(DiagnosticBooking::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing diagnostic bookings: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun bookDiagnosticTest(
        center: DiagnosticCenter,
        test: DiagnosticTest,
        scheduledDate: String,
        scheduledTime: String,
        collectionType: String,
        collectionAddress: String,
        familyMemberName: String,
        paymentMethod: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("diagnostic_bookings").document()
        val paymentStatus = if (paymentMethod.contains("Doorstep", ignoreCase = true) || paymentMethod.contains("Clinic", ignoreCase = true)) {
            "PAY_AT_COUNTER"
        } else {
            "PAID"
        }
        docRef.set(
            mapOf(
                "patientId" to uid,
                "patientName" to (auth.currentUser?.displayName ?: "Patient"),
                "familyMemberName" to familyMemberName,
                "centerId" to center.id,
                "centerName" to center.name,
                "testId" to test.id,
                "testName" to test.name,
                "testCategory" to test.category,
                "scheduledDate" to scheduledDate,
                "scheduledTime" to scheduledTime,
                "collectionType" to collectionType,
                "collectionAddress" to collectionAddress,
                "status" to "BOOKED",
                "resultSummary" to if (collectionType == "HOME_COLLECTION") {
                    "Phlebotomist assigned for home pickup ($scheduledTime)."
                } else {
                    "Lab visit confirmed. Follow test prep guidelines."
                },
                "abnormalFlag" to false,
                "cost" to test.price,
                "paymentStatus" to paymentStatus,
                "paymentMethod" to paymentMethod,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        recordPaymentInternal(
            title = "Lab Test: ${test.name}",
            providerName = center.name,
            category = "DIAGNOSTIC",
            billingForName = familyMemberName,
            amount = test.price,
            paymentMethod = paymentMethod,
            status = paymentStatus
        )

        createNotification(
            title = if (collectionType == "HOME_COLLECTION") "Home Sample Collection Scheduled" else "Diagnostic Test Booked",
            message = "${test.name} for $familyMemberName on $scheduledDate ($scheduledTime) at ${center.name}.",
            category = "LAB"
        )
    }

    suspend fun rescheduleDiagnosticBooking(bookingId: String, newDate: String, newTime: String) {
        db.collection("diagnostic_bookings").document(bookingId).update(
            mapOf(
                "scheduledDate" to newDate,
                "scheduledTime" to newTime,
                "status" to "BOOKED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Lab Booking Rescheduled",
            message = "Diagnostic test slot updated to $newDate ($newTime).",
            category = "LAB"
        )
    }

    suspend fun cancelDiagnosticBooking(bookingId: String) {
        db.collection("diagnostic_bookings").document(bookingId).update(
            mapOf(
                "status" to "CANCELLED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Diagnostic Booking Cancelled",
            message = "Your diagnostic test booking has been cancelled.",
            category = "LAB"
        )
    }

    // -------------------------------------------------------------
    // HOME HEALTHCARE SERVICES
    // -------------------------------------------------------------
    fun observeHomeCareBookings(): Flow<List<HomeCareBooking>> = flow {
        val uid = requireUserId()
        db.collection("home_care_bookings")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(HomeCareBooking::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing home care bookings: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun bookHomeCare(
        service: HomeCareService,
        memberName: String,
        visitAddress: String,
        scheduledDate: String,
        timeWindow: String,
        notes: String,
        paymentMethod: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("home_care_bookings").document()
        val paymentStatus = if (paymentMethod.contains("Doorstep", ignoreCase = true)) "PAY_AT_COUNTER" else "PAID"
        docRef.set(
            mapOf(
                "patientId" to uid,
                "patientName" to (auth.currentUser?.displayName ?: "Patient"),
                "memberName" to memberName,
                "serviceId" to service.id,
                "serviceTitle" to service.title,
                "serviceCategory" to service.category,
                "providerTeam" to service.providerTeam,
                "visitAddress" to visitAddress,
                "scheduledDate" to scheduledDate,
                "timeWindow" to timeWindow,
                "notes" to notes,
                "status" to "SCHEDULED",
                "cost" to service.price,
                "paymentStatus" to paymentStatus,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        recordPaymentInternal(
            title = "Home Care: ${service.title}",
            providerName = service.providerTeam,
            category = "HOME_CARE",
            billingForName = memberName,
            amount = service.price,
            paymentMethod = paymentMethod,
            status = paymentStatus
        )

        createNotification(
            title = "Home Healthcare Scheduled",
            message = "${service.title} for $memberName on $scheduledDate ($timeWindow).",
            category = "BOOKING"
        )
    }

    suspend fun rescheduleHomeCare(bookingId: String, newDate: String, newWindow: String) {
        db.collection("home_care_bookings").document(bookingId).update(
            mapOf(
                "scheduledDate" to newDate,
                "timeWindow" to newWindow,
                "status" to "SCHEDULED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Home Care Rescheduled",
            message = "Home healthcare visit updated to $newDate ($newWindow).",
            category = "BOOKING"
        )
    }

    suspend fun cancelHomeCare(bookingId: String) {
        db.collection("home_care_bookings").document(bookingId).update(
            mapOf(
                "status" to "CANCELLED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // AMBULANCE & EMERGENCY DISPATCH
    // -------------------------------------------------------------
    fun observePatientAmbulanceRequests(): Flow<List<AmbulanceRequest>> = flow {
        val uid = requireUserId()
        db.collection("ambulance_requests")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(AmbulanceRequest::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing ambulance requests: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun requestAmbulance(
        pickupAddress: String,
        emergencyType: String,
        ambulanceType: String,
        patientPhone: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("ambulance_requests").document()
        val vehicleNo = "EMS-${(1000..9999).random()}"
        docRef.set(
            mapOf(
                "patientId" to uid,
                "patientName" to (auth.currentUser?.displayName ?: "Emergency Patient"),
                "patientPhone" to patientPhone,
                "pickupAddress" to pickupAddress,
                "emergencyType" to emergencyType,
                "ambulanceType" to ambulanceType,
                "status" to "DISPATCHED",
                "vehicleNumber" to vehicleNo,
                "driverName" to "Paramedic Team Alpha",
                "driverPhone" to "+1 (555) 911-0428",
                "etaMinutes" to 7L,
                "hospitalDestination" to "Metro General Trauma & Cardiac Center",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        createNotification(
            title = "Emergency Ambulance Dispatched",
            message = "Unit $vehicleNo ($ambulanceType) en route to $pickupAddress. ETA 7 mins.",
            category = "EMERGENCY"
        )
        return docRef.id
    }

    suspend fun updateAmbulanceStatus(requestId: String, status: String, etaMinutes: Long = 0L) {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (etaMinutes > 0) updates["etaMinutes"] = etaMinutes
        db.collection("ambulance_requests").document(requestId).update(updates).await()
    }

    // -------------------------------------------------------------
    // PAYMENTS & RECEIPTS
    // -------------------------------------------------------------
    fun observePaymentRecords(): Flow<List<PaymentRecord>> = flow {
        val uid = requireUserId()
        db.collection("payments")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(PaymentRecord::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing payments: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    private suspend fun recordPaymentInternal(
        title: String,
        providerName: String,
        category: String,
        billingForName: String,
        amount: Double,
        paymentMethod: String,
        status: String
    ) {
        val uid = requireUserId()
        val txnRef = "TXN-${(100000..999999).random()}"
        db.collection("payments").document().set(
            mapOf(
                "patientId" to uid,
                "title" to title,
                "providerName" to providerName,
                "category" to category,
                "billingForName" to billingForName,
                "amount" to amount,
                "paymentMethod" to paymentMethod,
                "status" to status,
                "transactionRef" to txnRef,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // NOTIFICATIONS
    // -------------------------------------------------------------
    fun observeNotifications(): Flow<List<AppNotification>> = flow {
        val uid = requireUserId()
        db.collection("notifications")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { snap ->
                snap.toObjects(AppNotification::class.java)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing notifications: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun createNotification(title: String, message: String, category: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("notifications").document().set(
                mapOf(
                    "patientId" to uid,
                    "title" to title,
                    "message" to message,
                    "category" to category,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("HealthcareRepo", "Notification insert warning: ${e.message}")
        }
    }

    suspend fun markNotificationRead(notificationId: String) {
        db.collection("notifications").document(notificationId).update("isRead", true).await()
    }

    suspend fun markAllNotificationsRead(ids: List<String>) {
        ids.forEach { id ->
            try {
                db.collection("notifications").document(id).update("isRead", true).await()
            } catch (_: Exception) {
            }
        }
    }

    // -------------------------------------------------------------
    // REVIEWS
    // -------------------------------------------------------------
    fun observeReviews(): Flow<List<Review>> = flow {
        requireUserId()
        db.collection("reviews")
            .snapshots()
            .map { snap ->
                snap.toObjects(Review::class.java)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing reviews: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitReview(
        targetId: String,
        targetName: String,
        targetType: String,
        rating: Int,
        comment: String
    ) {
        val uid = requireUserId()
        db.collection("reviews").document().set(
            mapOf(
                "patientId" to uid,
                "patientName" to (auth.currentUser?.displayName ?: "Verified Patient"),
                "targetId" to targetId,
                "targetName" to targetName,
                "targetType" to targetType,
                "rating" to rating,
                "comment" to comment,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // HELP & SUPPORT TICKETS
    // -------------------------------------------------------------
    fun observeSupportTickets(): Flow<List<SupportTicket>> = flow {
        val uid = requireUserId()
        db.collection("support_tickets")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { snap ->
                snap.toObjects(SupportTicket::class.java)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing support tickets: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitSupportTicket(category: String, subject: String, description: String) {
        val uid = requireUserId()
        db.collection("support_tickets").document().set(
            mapOf(
                "patientId" to uid,
                "category" to category,
                "subject" to subject,
                "description" to description,
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Support Ticket Created",
            message = "Ticket '$subject' ($category) received. Care support responds within 2 hours.",
            category = "SUPPORT"
        )
    }

    // -------------------------------------------------------------
    // INSURANCE CLAIMS
    // -------------------------------------------------------------
    fun observePatientInsuranceClaims(): Flow<List<InsuranceClaim>> = flow {
        val uid = requireUserId()
        db.collection("insurance_claims")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(InsuranceClaim::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing insurance claims: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitInsuranceClaim(claim: InsuranceClaim) {
        val uid = requireUserId()
        val docRef = db.collection("insurance_claims").document()
        docRef.set(
            mapOf(
                "patientId" to uid,
                "patientName" to (auth.currentUser?.displayName ?: "Policy Holder"),
                "policyNumber" to claim.policyNumber,
                "insuranceProvider" to claim.insuranceProvider,
                "hospitalName" to claim.hospitalName,
                "procedureName" to claim.procedureName,
                "claimedAmount" to claim.claimedAmount,
                "approvedAmount" to 0.0,
                "status" to "SUBMITTED",
                "claimType" to claim.claimType,
                "documentReference" to claim.documentReference,
                "reviewerNotes" to "Under initial verification by claims team.",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        createNotification(
            title = "Insurance Claim Submitted",
            message = "Cashless claim for ${claim.procedureName} ($${claim.claimedAmount}) submitted.",
            category = "PAYMENT"
        )
    }

    // -------------------------------------------------------------
    // MEDICAL RECORDS (DIGITAL HEALTH LOCKER)
    // -------------------------------------------------------------
    fun observeMedicalRecords(): Flow<List<MedicalRecord>> = flow {
        val uid = requireUserId()
        db.collection("medical_records")
            .whereEqualTo("patientId", uid)
            .snapshots()
            .map { it.toObjects(MedicalRecord::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing medical records: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun addMedicalRecord(record: MedicalRecord) {
        val uid = requireUserId()
        val docRef = db.collection("medical_records").document()
        docRef.set(
            mapOf(
                "patientId" to uid,
                "memberName" to record.memberName,
                "title" to record.title,
                "category" to record.category,
                "doctorOrFacility" to record.doctorOrFacility,
                "recordDate" to record.recordDate,
                "summary" to record.summary,
                "keyMetrics" to record.keyMetrics,
                "fileType" to record.fileType,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------
    // SECURE REAL-TIME MESSAGING
    // -------------------------------------------------------------
    fun observeConsultationMessages(consultationId: String): Flow<List<ChatMessage>> = flow {
        db.collection("chat_messages")
            .whereEqualTo("consultationId", consultationId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(ChatMessage::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w("HealthcareRepo", "Error observing chat messages: ${error.message}")
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun sendChatMessage(
        consultationId: String,
        recipientId: String,
        recipientName: String,
        message: String,
        messageType: String = "TEXT"
    ) {
        val uid = requireUserId()
        val docRef = db.collection("chat_messages").document()
        docRef.set(
            mapOf(
                "consultationId" to consultationId,
                "senderId" to uid,
                "senderName" to (auth.currentUser?.displayName ?: "Patient"),
                "senderRole" to "PATIENT",
                "recipientId" to recipientId,
                "recipientName" to recipientName,
                "message" to message,
                "isRead" to false,
                "messageType" to messageType,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
