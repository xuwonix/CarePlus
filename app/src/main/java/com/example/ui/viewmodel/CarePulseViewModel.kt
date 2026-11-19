package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
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
import com.example.data.model.InsurancePartner
import com.example.data.model.MedicalRecord
import com.example.data.model.MedicalSpecialtyItem
import com.example.data.model.NearbyFacility
import com.example.data.model.PaymentRecord
import com.example.data.model.ProcedureGuide
import com.example.data.model.Review
import com.example.data.model.Specialist
import com.example.data.model.SupportTicket
import com.example.data.model.UserInsurancePolicy
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareCatalog
import com.example.data.repository.HealthcareRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MainNavTab(val label: String) {
    HOME("Home"),
    BOOKING("Booking"),
    EMERGENCY("Emergency"),
    RECORDS("Records"),
    PROFILE("Profile")
}

data class HealthcareUiState(
    val selectedTab: MainNavTab = MainNavTab.HOME,
    val bookingSubTab: Int = 0, // 0 = Doctors, 1 = Lab Tests, 2 = Home Care, 3 = My Bookings
    val selectedSpecialtyFilter: String = "All",
    val searchQuery: String = "",
    val selectedLocationArea: String = "Mission Chariali, Tezpur",
    val userProfile: UserProfile? = null,
    val familyMembers: List<FamilyMember> = emptyList(),
    val patientAppointments: List<Appointment> = emptyList(),
    val patientAmbulanceRequests: List<AmbulanceRequest> = emptyList(),
    val patientDiagnosticBookings: List<DiagnosticBooking> = emptyList(),
    val homeCareBookings: List<HomeCareBooking> = emptyList(),
    val paymentRecords: List<PaymentRecord> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val supportTickets: List<SupportTicket> = emptyList(),
    val patientInsuranceClaims: List<InsuranceClaim> = emptyList(),
    val medicalRecords: List<MedicalRecord> = emptyList(),
    val currentChatMessages: List<ChatMessage> = emptyList(),
    val activeConsultationId: String = HealthcareRepository.SUPPORT_DESK_CONSULTATION_ID,
    val activeRecipientName: String = HealthcareRepository.SUPPORT_DESK_RECIPIENT_NAME,
    val activeRecipientId: String = HealthcareRepository.SUPPORT_DESK_RECIPIENT_ID,
    val activeRecipientSpecialty: String = "24/7 Tezpur & Biswanath Care Desk",
    val specialists: List<Specialist> = emptyList(),
    val diagnosticCenters: List<DiagnosticCenter> = emptyList(),
    val nearbyFacilities: List<NearbyFacility> = emptyList(),
    val popularSpecialties: List<MedicalSpecialtyItem> = emptyList(),
    val homeCareServices: List<HomeCareService> = emptyList(),
    val insurancePartners: List<InsurancePartner> = emptyList(),
    val procedureGuides: List<ProcedureGuide> = emptyList(),
    val completedChecklistSteps: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null
) {
    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }

    val userInsurancePolicies: List<UserInsurancePolicy>
        get() = userProfile?.insurancePolicies ?: emptyList()

    val bookedSpecialistIds: Set<String>
        get() = patientAppointments
            .filter { it.status != "CANCELLED" }
            .map { it.specialistId }
            .toSet()

    fun hasBookedDoctor(specialistId: String): Boolean = specialistId in bookedSpecialistIds

    fun canMessageRecipient(recipientId: String): Boolean {
        return recipientId == HealthcareRepository.SUPPORT_DESK_RECIPIENT_ID ||
            recipientId == HealthcareRepository.INSURANCE_DESK_RECIPIENT_ID ||
            hasBookedDoctor(recipientId)
    }
}

class CarePulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HealthcareRepository(application)
    private var chatJob: Job? = null

    private val _uiState = MutableStateFlow(
        HealthcareUiState(
            specialists = HealthcareCatalog.getLocalSpecialists(),
            diagnosticCenters = HealthcareCatalog.getLocalDiagnosticCenters(),
            nearbyFacilities = HealthcareCatalog.getNearbyHospitalsAndCenters("Mission Chariali, Tezpur"),
            popularSpecialties = HealthcareCatalog.getPopularSpecialties(),
            homeCareServices = HealthcareCatalog.getHomeCareServices(),
            insurancePartners = HealthcareCatalog.getTiedUpInsurancePartners(),
            procedureGuides = HealthcareCatalog.getProcedureGuides()
        )
    )
    val uiState: StateFlow<HealthcareUiState> = _uiState.asStateFlow()

    private val profileCachePrefs = application.getSharedPreferences("nidanmitra_profile_cache", Context.MODE_PRIVATE)

    private fun cacheProfileLocally(profile: UserProfile) {
        if (profile.userId.isBlank()) return
        val p = profile.userId
        profileCachePrefs.edit()
            .putBoolean("${p}_has_cache", true)
            .putString("${p}_email", profile.email)
            .putString("${p}_displayName", profile.displayName)
            .putString("${p}_phone", profile.phone)
            .putString("${p}_age", profile.age)
            .putString("${p}_gender", profile.gender)
            .putString("${p}_bloodGroup", profile.bloodGroup)
            .putString("${p}_allergies", profile.allergies)
            .putString("${p}_chronicConditions", profile.chronicConditions)
            .putString("${p}_emergencyContactName", profile.emergencyContactName)
            .putString("${p}_emergencyContactPhone", profile.emergencyContactPhone)
            .putString("${p}_locationArea", profile.locationArea)
            .putString("${p}_savedAddress", profile.savedAddress)
            .putBoolean("${p}_onboardingCompleted", profile.onboardingCompleted)
            .apply()
    }

    private fun loadCachedProfile(uid: String, email: String, displayName: String): UserProfile? {
        if (uid.isBlank() || !profileCachePrefs.getBoolean("${uid}_has_cache", false)) return null
        return UserProfile(
            userId = uid,
            email = profileCachePrefs.getString("${uid}_email", email).orEmpty().ifBlank { email },
            displayName = profileCachePrefs.getString("${uid}_displayName", displayName).orEmpty().ifBlank { displayName },
            phone = profileCachePrefs.getString("${uid}_phone", "").orEmpty(),
            age = profileCachePrefs.getString("${uid}_age", "").orEmpty(),
            gender = profileCachePrefs.getString("${uid}_gender", "Prefer not to say").orEmpty(),
            bloodGroup = profileCachePrefs.getString("${uid}_bloodGroup", "").orEmpty(),
            allergies = profileCachePrefs.getString("${uid}_allergies", "").orEmpty(),
            chronicConditions = profileCachePrefs.getString("${uid}_chronicConditions", "").orEmpty(),
            emergencyContactName = profileCachePrefs.getString("${uid}_emergencyContactName", "").orEmpty(),
            emergencyContactPhone = profileCachePrefs.getString("${uid}_emergencyContactPhone", "").orEmpty(),
            locationArea = profileCachePrefs.getString("${uid}_locationArea", "Mission Chariali, Tezpur").orEmpty(),
            savedAddress = profileCachePrefs.getString("${uid}_savedAddress", "").orEmpty(),
            onboardingCompleted = profileCachePrefs.getBoolean("${uid}_onboardingCompleted", false)
        )
    }

    private fun describeSyncError(e: Exception): String {
        val fsError = generateSequence<Throwable>(e) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        return when (fsError?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Firestore rules blocked cloud sync. Saved on device—publish firestore.rules in Firebase Console."
            FirebaseFirestoreException.Code.NOT_FOUND ->
                "Firestore database not found in Firebase project. Saved on device—create the (default) Firestore database in Firebase Console."
            else ->
                "Cloud sync unavailable right now. Changes saved on this device."
        }
    }

    init {
        initUserData()
    }

    private fun initUserData() {
        val currentUser = repository.getCurrentUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = try {
                repository.getOrCreateUserProfile().also { cacheProfileLocally(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("CarePulseVM", "Initial profile cloud sync failed: ${e.message}", e)
                val cached = loadCachedProfile(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName ?: "Nidanmitra Patient"
                )
                cached ?: UserProfile(
                    userId = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName ?: "Nidanmitra Patient",
                    onboardingCompleted = false
                )
            }
            val area = profile.locationArea.ifBlank { "Mission Chariali, Tezpur" }
            _uiState.update {
                it.copy(
                    userProfile = profile,
                    selectedLocationArea = area,
                    nearbyFacilities = HealthcareCatalog.getNearbyHospitalsAndCenters(area),
                    isLoading = false
                )
            }

            launch {
                repository.observeUserProfile().collect { updatedProfile ->
                    if (updatedProfile != null) {
                        cacheProfileLocally(updatedProfile)
                        val updatedArea = updatedProfile.locationArea.ifBlank { "Mission Chariali, Tezpur" }
                        _uiState.update {
                            it.copy(
                                userProfile = updatedProfile,
                                selectedLocationArea = updatedArea,
                                nearbyFacilities = HealthcareCatalog.getNearbyHospitalsAndCenters(updatedArea)
                            )
                        }
                    }
                }
            }

            launch {
                repository.observeFamilyMembers().collect { members ->
                    _uiState.update { it.copy(familyMembers = members) }
                }
            }

            launch {
                repository.observePatientAppointments().collect { appointments ->
                    _uiState.update { it.copy(patientAppointments = appointments) }
                }
            }

            launch {
                repository.observePatientAmbulanceRequests().collect { requests ->
                    _uiState.update { it.copy(patientAmbulanceRequests = requests) }
                }
            }

            launch {
                repository.observePatientDiagnosticBookings().collect { bookings ->
                    _uiState.update { it.copy(patientDiagnosticBookings = bookings) }
                }
            }

            launch {
                repository.observeHomeCareBookings().collect { bookings ->
                    _uiState.update { it.copy(homeCareBookings = bookings) }
                }
            }

            launch {
                repository.observePaymentRecords().collect { payments ->
                    _uiState.update { it.copy(paymentRecords = payments) }
                }
            }

            launch {
                repository.observeNotifications().collect { items ->
                    _uiState.update { it.copy(notifications = items) }
                }
            }

            launch {
                repository.observeReviews().collect { items ->
                    _uiState.update { it.copy(reviews = items) }
                }
            }

            launch {
                repository.observeSupportTickets().collect { tickets ->
                    _uiState.update { it.copy(supportTickets = tickets) }
                }
            }

            launch {
                repository.observePatientInsuranceClaims().collect { claims ->
                    _uiState.update { it.copy(patientInsuranceClaims = claims) }
                }
            }

            launch {
                repository.observeMedicalRecords().collect { records ->
                    _uiState.update { it.copy(medicalRecords = records) }
                }
            }

            observeChatMessages(_uiState.value.activeConsultationId)
        }
    }

    fun selectMainTab(tab: MainNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun navigateToBooking(subTab: Int = 0, specialtyFilter: String = "All") {
        _uiState.update {
            it.copy(
                selectedTab = MainNavTab.BOOKING,
                bookingSubTab = subTab,
                selectedSpecialtyFilter = specialtyFilter
            )
        }
    }

    fun selectBookingSubTab(subTab: Int) {
        _uiState.update { it.copy(bookingSubTab = subTab) }
    }

    fun selectSpecialtyFilter(specialty: String) {
        _uiState.update { it.copy(selectedSpecialtyFilter = specialty) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updatePatientProfile(profile: UserProfile) {
        val currentUser = repository.getCurrentUser()
        val updatedLocal = profile.copy(
            userId = profile.userId.ifBlank { currentUser?.uid ?: "" },
            email = profile.email.ifBlank { currentUser?.email ?: "" },
            onboardingCompleted = true
        )
        val area = updatedLocal.locationArea.ifBlank { "Mission Chariali, Tezpur" }
        cacheProfileLocally(updatedLocal)
        _uiState.update {
            it.copy(
                userProfile = updatedLocal,
                selectedLocationArea = area,
                nearbyFacilities = HealthcareCatalog.getNearbyHospitalsAndCenters(area)
            )
        }
        viewModelScope.launch {
            try {
                repository.updateUserProfile(updatedLocal)
                _uiState.update { it.copy(feedbackMessage = "Patient profile updated") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("CarePulseVM", "Profile cloud update failed: ${e.message}", e)
                _uiState.update { it.copy(feedbackMessage = describeSyncError(e)) }
            }
        }
    }

    fun skipOnboarding() {
        val updatedLocal = (_uiState.value.userProfile ?: UserProfile(
            userId = repository.getCurrentUser()?.uid ?: "",
            email = repository.getCurrentUser()?.email ?: "",
            displayName = repository.getCurrentUser()?.displayName ?: "Nidanmitra Patient"
        )).copy(onboardingCompleted = true)
        cacheProfileLocally(updatedLocal)
        _uiState.update { it.copy(userProfile = updatedLocal) }
        viewModelScope.launch {
            try {
                repository.skipOnboarding()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("CarePulseVM", "Skip onboarding cloud sync failed: ${e.message}", e)
                _uiState.update {
                    it.copy(feedbackMessage = "You can complete your health profile anytime in Profile.")
                }
            }
        }
    }

    fun updateLocation(area: String, address: String) {
        val updatedProfile = _uiState.value.userProfile?.copy(
            locationArea = area,
            savedAddress = address
        )
        if (updatedProfile != null) {
            cacheProfileLocally(updatedProfile)
        }
        _uiState.update {
            it.copy(
                userProfile = updatedProfile ?: it.userProfile,
                selectedLocationArea = area,
                nearbyFacilities = HealthcareCatalog.getNearbyHospitalsAndCenters(area)
            )
        }
        viewModelScope.launch {
            try {
                repository.updateLocation(area, address)
                _uiState.update { it.copy(feedbackMessage = "Location set to $area") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("CarePulseVM", "Location cloud sync failed: ${e.message}", e)
                _uiState.update { it.copy(feedbackMessage = "Location set to $area") }
            }
        }
    }

    fun addFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            try {
                repository.addFamilyMember(member)
                _uiState.update { it.copy(feedbackMessage = "${member.name} added to Family Profiles") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not add family member: ${e.message}") }
            }
        }
    }

    fun deleteFamilyMember(memberId: String) {
        viewModelScope.launch {
            try {
                repository.deleteFamilyMember(memberId)
                _uiState.update { it.copy(feedbackMessage = "Family member removed") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not remove member: ${e.message}") }
            }
        }
    }

    fun scheduleAppointment(
        specialist: Specialist,
        date: String,
        timeSlot: String,
        mode: String,
        notes: String,
        familyMemberName: String = "Self",
        paymentMethod: String = "UPI / Card"
    ) {
        viewModelScope.launch {
            try {
                val appointment = Appointment(
                    specialistId = specialist.id,
                    specialistName = specialist.name,
                    specialty = specialist.specialty,
                    clinicHospital = specialist.clinicHospital,
                    date = date,
                    timeSlot = timeSlot,
                    mode = mode,
                    notes = notes,
                    consultationFee = specialist.fee,
                    familyMemberName = familyMemberName,
                    paymentMethod = paymentMethod
                )
                repository.scheduleAppointment(appointment)
                _uiState.update {
                    it.copy(feedbackMessage = "Appointment confirmed with ${specialist.name} for $familyMemberName ($date, $timeSlot)")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed to book: ${e.message}") }
            }
        }
    }

    fun rescheduleAppointment(appointmentId: String, newDate: String, newSlot: String) {
        viewModelScope.launch {
            try {
                repository.rescheduleAppointment(appointmentId, newDate, newSlot)
                _uiState.update { it.copy(feedbackMessage = "Appointment rescheduled to $newDate at $newSlot") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not reschedule: ${e.message}") }
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                repository.cancelAppointment(appointmentId)
                _uiState.update { it.copy(feedbackMessage = "Appointment cancelled") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not cancel: ${e.message}") }
            }
        }
    }

    fun requestAmbulance(
        pickupAddress: String,
        emergencyType: String,
        ambulanceType: String,
        patientPhone: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.requestAmbulance(pickupAddress, emergencyType, ambulanceType, patientPhone)
                val emsZone = if (HealthcareCatalog.isBiswanathArea(pickupAddress)) "Biswanath" else "Tezpur"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedTab = MainNavTab.EMERGENCY,
                        feedbackMessage = "Emergency Ambulance dispatched! $emsZone 108 EMS en route."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, feedbackMessage = "Dispatch error: ${e.message}") }
            }
        }
    }

    fun updateAmbulanceStatus(requestId: String, status: String, etaMinutes: Long = 0L) {
        viewModelScope.launch {
            try {
                repository.updateAmbulanceStatus(requestId, status, etaMinutes)
                _uiState.update { it.copy(feedbackMessage = "Emergency request marked $status") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Error updating ambulance: ${e.message}") }
            }
        }
    }

    fun bookDiagnosticTest(
        center: DiagnosticCenter,
        test: DiagnosticTest,
        date: String,
        timeWindow: String,
        collectionType: String,
        collectionAddress: String,
        familyMemberName: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            try {
                repository.bookDiagnosticTest(
                    center = center,
                    test = test,
                    scheduledDate = date,
                    scheduledTime = timeWindow,
                    collectionType = collectionType,
                    collectionAddress = collectionAddress,
                    familyMemberName = familyMemberName,
                    paymentMethod = paymentMethod
                )
                _uiState.update {
                    it.copy(feedbackMessage = "Booked ${test.name} for $familyMemberName ($date)")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed booking: ${e.message}") }
            }
        }
    }

    fun rescheduleDiagnosticBooking(bookingId: String, newDate: String, newTime: String) {
        viewModelScope.launch {
            try {
                repository.rescheduleDiagnosticBooking(bookingId, newDate, newTime)
                _uiState.update { it.copy(feedbackMessage = "Lab booking rescheduled to $newDate ($newTime)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not reschedule lab test: ${e.message}") }
            }
        }
    }

    fun cancelDiagnosticBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                repository.cancelDiagnosticBooking(bookingId)
                _uiState.update { it.copy(feedbackMessage = "Diagnostic booking cancelled") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not cancel diagnostic booking: ${e.message}") }
            }
        }
    }

    fun bookHomeCare(
        service: HomeCareService,
        memberName: String,
        visitAddress: String,
        scheduledDate: String,
        timeWindow: String,
        notes: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            try {
                repository.bookHomeCare(
                    service = service,
                    memberName = memberName,
                    visitAddress = visitAddress,
                    scheduledDate = scheduledDate,
                    timeWindow = timeWindow,
                    notes = notes,
                    paymentMethod = paymentMethod
                )
                _uiState.update {
                    it.copy(feedbackMessage = "Home care booked: ${service.title} for $memberName")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Home care booking error: ${e.message}") }
            }
        }
    }

    fun rescheduleHomeCare(bookingId: String, newDate: String, newWindow: String) {
        viewModelScope.launch {
            try {
                repository.rescheduleHomeCare(bookingId, newDate, newWindow)
                _uiState.update { it.copy(feedbackMessage = "Home care rescheduled to $newDate ($newWindow)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not reschedule home care: ${e.message}") }
            }
        }
    }

    fun cancelHomeCare(bookingId: String) {
        viewModelScope.launch {
            try {
                repository.cancelHomeCare(bookingId)
                repository.createNotification(
                    title = "Home Care Visit Cancelled",
                    message = "Your scheduled home healthcare visit has been cancelled.",
                    category = "BOOKING"
                )
                _uiState.update { it.copy(feedbackMessage = "Home care booking cancelled") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not cancel home care: ${e.message}") }
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationRead(id)
            } catch (_: Exception) {
            }
        }
    }

    fun markAllNotificationsRead() {
        val unreadIds = _uiState.value.notifications.filter { !it.isRead }.map { it.id }.filter { it.isNotBlank() }
        if (unreadIds.isEmpty()) return
        viewModelScope.launch {
            repository.markAllNotificationsRead(unreadIds)
            _uiState.update { it.copy(feedbackMessage = "All notifications marked as read") }
        }
    }

    fun submitReview(
        targetId: String,
        targetName: String,
        targetType: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            try {
                repository.submitReview(targetId, targetName, targetType, rating, comment)
                _uiState.update { it.copy(feedbackMessage = "Thank you for reviewing $targetName!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not submit review: ${e.message}") }
            }
        }
    }

    fun submitSupportTicket(category: String, subject: String, description: String) {
        viewModelScope.launch {
            try {
                repository.submitSupportTicket(category, subject, description)
                _uiState.update { it.copy(feedbackMessage = "Support ticket submitted. Team responds within 2 hrs.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not submit ticket: ${e.message}") }
            }
        }
    }

    fun submitInsuranceClaim(
        policyNumber: String,
        provider: String,
        hospital: String,
        procedure: String,
        amount: Double,
        claimType: String,
        docRef: String
    ) {
        viewModelScope.launch {
            try {
                val claim = InsuranceClaim(
                    policyNumber = policyNumber,
                    insuranceProvider = provider,
                    hospitalName = hospital,
                    procedureName = procedure,
                    claimedAmount = amount,
                    claimType = claimType,
                    documentReference = docRef
                )
                repository.submitInsuranceClaim(claim)
                _uiState.update { it.copy(feedbackMessage = "Claim of ₹${amount.toInt()} submitted for pre-authorization") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Claim submission error: ${e.message}") }
            }
        }
    }

    fun addUserInsurancePolicy(policy: UserInsurancePolicy) {
        viewModelScope.launch {
            try {
                val current = _uiState.value.userInsurancePolicies
                val newPolicy = policy.copy(
                    id = policy.id.ifBlank { "pol_${UUID.randomUUID().toString().take(8)}" }
                )
                val updatedList = listOf(newPolicy) + current
                repository.saveUserInsurancePolicies(updatedList)
                repository.createNotification(
                    title = "Insurance Policy Added",
                    message = "${newPolicy.insurerName} (${newPolicy.policyNumber}) saved to your Nidanmitra profile.",
                    category = "PAYMENT"
                )
                _uiState.update {
                    it.copy(
                        userProfile = it.userProfile?.copy(insurancePolicies = updatedList),
                        feedbackMessage = "Saved ${newPolicy.insurerName} policy to your profile"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not save policy: ${e.message}") }
            }
        }
    }

    fun deleteUserInsurancePolicy(policyId: String) {
        viewModelScope.launch {
            try {
                val updatedList = _uiState.value.userInsurancePolicies.filterNot { it.id == policyId }
                repository.saveUserInsurancePolicies(updatedList)
                _uiState.update {
                    it.copy(
                        userProfile = it.userProfile?.copy(insurancePolicies = updatedList),
                        feedbackMessage = "Insurance policy removed"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not remove policy: ${e.message}") }
            }
        }
    }

    fun submitInsuranceInquiry(
        partnerName: String,
        planName: String,
        inquiryType: String,
        coverageAmount: String,
        membersInfo: String,
        preferredPhone: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.submitInsuranceInquiry(
                    partnerName = partnerName,
                    planName = planName,
                    inquiryType = inquiryType,
                    coverageAmount = coverageAmount,
                    membersInfo = membersInfo,
                    preferredPhone = preferredPhone,
                    notes = notes
                )
                _uiState.update {
                    it.copy(feedbackMessage = "Request sent to Nidanmitra Insurance Assistance for $partnerName!")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not submit inquiry: ${e.message}") }
            }
        }
    }

    fun addMedicalRecord(
        title: String,
        category: String,
        doctorOrFacility: String,
        recordDate: String,
        summary: String,
        keyMetrics: String,
        memberName: String = "Self",
        fileType: String = "IMAGE",
        attachmentPath: String = "",
        attachmentName: String = ""
    ) {
        viewModelScope.launch {
            try {
                val record = MedicalRecord(
                    memberName = memberName,
                    title = title,
                    category = category,
                    doctorOrFacility = doctorOrFacility,
                    recordDate = recordDate,
                    summary = summary,
                    keyMetrics = keyMetrics,
                    fileType = fileType,
                    attachmentPath = attachmentPath,
                    attachmentName = attachmentName
                )
                repository.addMedicalRecord(record)
                _uiState.update { it.copy(feedbackMessage = "Saved '$title' ($memberName) to Health Locker") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed to save record: ${e.message}") }
            }
        }
    }

    fun openConsultationChat(
        consultationId: String,
        recipientName: String,
        recipientId: String,
        recipientSpecialty: String = "Specialist Consultation"
    ) {
        _uiState.update {
            it.copy(
                activeConsultationId = consultationId,
                activeRecipientName = recipientName,
                activeRecipientId = recipientId,
                activeRecipientSpecialty = recipientSpecialty
            )
        }
        observeChatMessages(consultationId)
    }

    fun openSupportDeskChat() {
        openConsultationChat(
            consultationId = HealthcareRepository.SUPPORT_DESK_CONSULTATION_ID,
            recipientName = HealthcareRepository.SUPPORT_DESK_RECIPIENT_NAME,
            recipientId = HealthcareRepository.SUPPORT_DESK_RECIPIENT_ID,
            recipientSpecialty = "24/7 Tezpur & Biswanath Care Desk"
        )
    }

    fun openInsuranceAssistanceChat() {
        openConsultationChat(
            consultationId = HealthcareRepository.INSURANCE_DESK_CONSULTATION_ID,
            recipientName = HealthcareRepository.INSURANCE_DESK_RECIPIENT_NAME,
            recipientId = HealthcareRepository.INSURANCE_DESK_RECIPIENT_ID,
            recipientSpecialty = "Cashless TPA & Policy Advisors"
        )
    }

    private fun observeChatMessages(consultationId: String) {
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            repository.observeConsultationMessages(consultationId).collect { messages ->
                _uiState.update { it.copy(currentChatMessages = messages) }
            }
        }
    }

    fun sendChatMessage(
        message: String,
        messageType: String = "TEXT",
        attachmentType: String = "",
        attachmentPath: String = "",
        attachmentName: String = "",
        alsoSaveToLocker: Boolean = false
    ) {
        if (message.isBlank() && attachmentPath.isBlank()) return
        val current = _uiState.value
        if (!current.canMessageRecipient(current.activeRecipientId)) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "Book an appointment with ${current.activeRecipientName} to unlock direct messaging."
                )
            }
            return
        }
        viewModelScope.launch {
            try {
                repository.sendChatMessage(
                    consultationId = current.activeConsultationId,
                    recipientId = current.activeRecipientId,
                    recipientName = current.activeRecipientName,
                    message = message,
                    messageType = messageType,
                    attachmentType = attachmentType,
                    attachmentPath = attachmentPath,
                    attachmentName = attachmentName
                )
                if (alsoSaveToLocker && attachmentPath.isNotBlank()) {
                    val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    repository.addMedicalRecord(
                        MedicalRecord(
                            memberName = "Self",
                            title = attachmentName.substringBeforeLast(".").ifBlank { "Shared Prescription" },
                            category = "PRESCRIPTION",
                            doctorOrFacility = current.activeRecipientName,
                            recordDate = today,
                            summary = message.ifBlank { "Shared via consultation chat with ${current.activeRecipientName}" },
                            keyMetrics = "",
                            fileType = attachmentType.ifBlank { "IMAGE" },
                            attachmentPath = attachmentPath,
                            attachmentName = attachmentName
                        )
                    )
                    _uiState.update {
                        it.copy(feedbackMessage = "Prescription sent and saved to Health Locker")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not send message: ${e.message}") }
            }
        }
    }

    fun notifyBookingRequiredForChat(doctorName: String) {
        _uiState.update {
            it.copy(
                feedbackMessage = "Book an appointment with $doctorName first to message them directly."
            )
        }
    }

    fun toggleProcedureStep(stepId: String) {
        _uiState.update { state ->
            val set = state.completedChecklistSteps.toMutableSet()
            if (set.contains(stepId)) {
                set.remove(stepId)
            } else {
                set.add(stepId)
            }
            state.copy(completedChecklistSteps = set)
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
