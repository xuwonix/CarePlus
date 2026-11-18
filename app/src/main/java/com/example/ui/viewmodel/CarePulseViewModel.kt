package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AmbulanceRequest
import com.example.data.model.Appointment
import com.example.data.model.ChatMessage
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.InsuranceClaim
import com.example.data.model.MedicalRecord
import com.example.data.model.MedicalSpecialtyItem
import com.example.data.model.NearbyFacility
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainNavTab(val label: String) {
    HOME("Home"),
    BOOKING("Booking"),
    EMERGENCY("Emergency"),
    RECORDS("Records"),
    PROFILE("Profile")
}

data class HealthcareUiState(
    val selectedTab: MainNavTab = MainNavTab.HOME,
    val bookingSubTab: Int = 0, // 0 = Doctors, 1 = Lab Tests, 2 = My Bookings
    val selectedSpecialtyFilter: String = "All",
    val searchQuery: String = "",
    val userProfile: UserProfile? = null,
    val patientAppointments: List<Appointment> = emptyList(),
    val patientAmbulanceRequests: List<AmbulanceRequest> = emptyList(),
    val patientDiagnosticBookings: List<DiagnosticBooking> = emptyList(),
    val patientInsuranceClaims: List<InsuranceClaim> = emptyList(),
    val medicalRecords: List<MedicalRecord> = emptyList(),
    val currentChatMessages: List<ChatMessage> = emptyList(),
    val activeConsultationId: String = "consultation_default",
    val activeRecipientName: String = "Dr. Elena Rostova",
    val activeRecipientId: String = "doc_cardio_1",
    val specialists: List<Specialist> = emptyList(),
    val diagnosticCenters: List<DiagnosticCenter> = emptyList(),
    val nearbyFacilities: List<NearbyFacility> = emptyList(),
    val popularSpecialties: List<MedicalSpecialtyItem> = emptyList(),
    val procedureGuides: List<ProcedureGuide> = emptyList(),
    val completedChecklistSteps: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null
)

class CarePulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HealthcareRepository(application)

    private val _uiState = MutableStateFlow(
        HealthcareUiState(
            specialists = repository.getLocalSpecialists(),
            diagnosticCenters = repository.getLocalDiagnosticCenters(),
            nearbyFacilities = repository.getNearbyHospitalsAndCenters(),
            popularSpecialties = repository.getPopularSpecialties(),
            procedureGuides = repository.getProcedureGuides()
        )
    )
    val uiState: StateFlow<HealthcareUiState> = _uiState.asStateFlow()

    init {
        initUserData()
    }

    private fun initUserData() {
        repository.getCurrentUser() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = repository.getOrCreateUserProfile()
            _uiState.update { it.copy(userProfile = profile, isLoading = false) }

            launch {
                repository.observeUserProfile().collect { updatedProfile ->
                    if (updatedProfile != null) {
                        _uiState.update { it.copy(userProfile = updatedProfile) }
                    }
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

    fun scheduleAppointment(
        specialist: Specialist,
        date: String,
        timeSlot: String,
        mode: String,
        notes: String
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
                    consultationFee = specialist.fee
                )
                repository.scheduleAppointment(appointment)
                _uiState.update {
                    it.copy(feedbackMessage = "Appointment confirmed with ${specialist.name} for $date ($timeSlot)")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed to book: ${e.message}") }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedTab = MainNavTab.EMERGENCY,
                        feedbackMessage = "Emergency Ambulance dispatched! Paramedic Alpha en route."
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
        collectionType: String
    ) {
        viewModelScope.launch {
            try {
                repository.bookDiagnosticTest(center, test, date, collectionType)
                _uiState.update { it.copy(feedbackMessage = "Booked ${test.name} at ${center.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed booking: ${e.message}") }
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
                _uiState.update { it.copy(feedbackMessage = "Claim of $$amount submitted for pre-authorization") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Claim submission error: ${e.message}") }
            }
        }
    }

    fun addMedicalRecord(
        title: String,
        category: String,
        doctorOrFacility: String,
        recordDate: String,
        summary: String,
        keyMetrics: String
    ) {
        viewModelScope.launch {
            try {
                val record = MedicalRecord(
                    title = title,
                    category = category,
                    doctorOrFacility = doctorOrFacility,
                    recordDate = recordDate,
                    summary = summary,
                    keyMetrics = keyMetrics
                )
                repository.addMedicalRecord(record)
                _uiState.update { it.copy(feedbackMessage = "Added '$title' to Health Locker") }
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Failed to save record: ${e.message}") }
            }
        }
    }

    fun openConsultationChat(consultationId: String, doctorName: String, doctorId: String) {
        _uiState.update {
            it.copy(
                activeConsultationId = consultationId,
                activeRecipientName = doctorName,
                activeRecipientId = doctorId
            )
        }
        observeChatMessages(consultationId)
    }

    private fun observeChatMessages(consultationId: String) {
        viewModelScope.launch {
            repository.observeConsultationMessages(consultationId).collect { messages ->
                _uiState.update { it.copy(currentChatMessages = messages) }
            }
        }
    }

    fun sendChatMessage(message: String, messageType: String = "TEXT") {
        if (message.isBlank()) return
        val current = _uiState.value
        viewModelScope.launch {
            try {
                repository.sendChatMessage(
                    consultationId = current.activeConsultationId,
                    recipientId = current.activeRecipientId,
                    recipientName = current.activeRecipientName,
                    message = message,
                    messageType = messageType
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(feedbackMessage = "Could not send message: ${e.message}") }
            }
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
