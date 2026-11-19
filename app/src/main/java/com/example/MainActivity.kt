package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HomeCareBooking
import com.example.data.model.HomeCareService
import com.example.data.model.InsurancePartner
import com.example.data.model.NearbyFacility
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.data.model.UserInsurancePolicy
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareCatalog
import com.example.data.repository.HealthcareRepository
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.appSignOut
import com.example.ui.screens.chat.ChatConsultationScreen
import com.example.ui.screens.patient.AddFamilyMemberModal
import com.example.ui.screens.patient.AddInsurancePolicyModal
import com.example.ui.screens.patient.AddMedicalRecordModal
import com.example.ui.screens.patient.AmbulanceDispatchModal
import com.example.ui.screens.patient.BookDiagnosticModal
import com.example.ui.screens.patient.BookHomeCareModal
import com.example.ui.screens.patient.BookSpecialistModal
import com.example.ui.screens.patient.BuyInsuranceInquiryModal
import com.example.ui.screens.patient.DoctorProfileScreen
import com.example.ui.screens.patient.EditPatientProfileModal
import com.example.ui.screens.patient.FacilityDetailScreen
import com.example.ui.screens.patient.HelpSupportScreen
import com.example.ui.screens.patient.InsuranceHubScreen
import com.example.ui.screens.patient.LocationPickerModal
import com.example.ui.screens.patient.NotificationsScreen
import com.example.ui.screens.patient.PatientBookingTab
import com.example.ui.screens.patient.PatientEmergencyTab
import com.example.ui.screens.patient.PatientHomeTab
import com.example.ui.screens.patient.PatientOnboardingScreen
import com.example.ui.screens.patient.PatientProfileTab
import com.example.ui.screens.patient.PatientRecordsTab
import com.example.ui.screens.patient.RescheduleBookingModal
import com.example.ui.screens.patient.SubmitInsuranceClaimModal
import com.example.ui.screens.procedures.ProcedureDetailScreen
import com.example.ui.theme.CarePulseTheme
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguagePreferences
import com.example.ui.i18n.LanguageSelectorButton
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.viewmodel.CarePulseViewModel
import com.example.ui.viewmodel.MainNavTab
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private sealed class RescheduleTarget {
    data class DoctorAppt(val appointment: Appointment) : RescheduleTarget()
    data class LabTest(val booking: DiagnosticBooking) : RescheduleTarget()
    data class HomeCare(val booking: HomeCareBooking) : RescheduleTarget()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarePulseTheme {
                CarePulseRootApp()
            }
        }
    }
}

@Composable
fun CarePulseRootApp() {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var selectedLanguage by remember { mutableStateOf(LanguagePreferences.load(context)) }

    val onSelectLanguage: (AppLanguage) -> Unit = { lang ->
        selectedLanguage = lang
        LanguagePreferences.save(context, lang)
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides selectedLanguage,
        LocalAppStrings provides stringsForLanguage(selectedLanguage)
    ) {
        if (currentUser == null) {
            AuthScreen(
                selectedLanguage = selectedLanguage,
                onSelectLanguage = onSelectLanguage,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                }
            )
        } else {
            val uid = currentUser!!.uid
            key(uid) {
                val viewModel: CarePulseViewModel = viewModel(key = uid)
                PatientMainApp(
                    viewModel = viewModel,
                    selectedLanguage = selectedLanguage,
                    onSelectLanguage = onSelectLanguage,
                    onSignOutRequested = {
                        currentUser = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMainApp(
    viewModel: CarePulseViewModel,
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSignOutRequested: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Overlay screens
    var activeProcedureScreen by remember { mutableStateOf<ProcedureGuide?>(null) }
    var activeChatScreen by remember { mutableStateOf(false) }
    var activeDoctorProfile by remember { mutableStateOf<Specialist?>(null) }
    var activeFacilityDetail by remember { mutableStateOf<NearbyFacility?>(null) }
    var showNotificationsScreen by remember { mutableStateOf(false) }
    var showHelpSupportScreen by remember { mutableStateOf(false) }
    var showInsuranceHubScreen by remember { mutableStateOf(false) }
    var insuranceHubInitialTab by remember { mutableStateOf(0) }

    // Modals
    var showAmbulanceModal by remember { mutableStateOf(false) }
    var bookingSpecialist by remember { mutableStateOf<Specialist?>(null) }
    var bookingDiagnostic by remember { mutableStateOf<Pair<DiagnosticCenter, DiagnosticTest>?>(null) }
    var bookingHomeCare by remember { mutableStateOf<HomeCareService?>(null) }
    var rescheduleTarget by remember { mutableStateOf<RescheduleTarget?>(null) }
    var showClaimModal by remember { mutableStateOf(false) }
    var claimPreselectedPolicy by remember { mutableStateOf<UserInsurancePolicy?>(null) }
    var showAddPolicyModal by remember { mutableStateOf(false) }
    var showBuyInquiryModal by remember { mutableStateOf(false) }
    var buyInquiryPartner by remember { mutableStateOf<InsurancePartner?>(null) }
    var showAddRecordModal by remember { mutableStateOf(false) }
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showAddFamilyMemberModal by remember { mutableStateOf(false) }
    var showLocationPickerModal by remember { mutableStateOf(false) }
    var lockedChatDoctor by remember { mutableStateOf<Specialist?>(null) }

    fun openDoctorChatIfBooked(doctorName: String, doctorId: String) {
        if (uiState.canMessageRecipient(doctorId)) {
            val spec = uiState.specialists.firstOrNull { it.id == doctorId }?.specialty ?: "Specialist Consultation"
            viewModel.openConsultationChat("consult_$doctorId", doctorName, doctorId, spec)
            activeDoctorProfile = null
            activeChatScreen = true
        } else {
            val found = uiState.specialists.firstOrNull { it.id == doctorId }
            if (found != null) {
                lockedChatDoctor = found
            } else {
                viewModel.notifyBookingRequiredForChat(doctorName)
            }
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    when {
        uiState.userProfile == null && uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.userProfile != null && !uiState.userProfile!!.onboardingCompleted -> {
            PatientOnboardingScreen(
                initialProfile = uiState.userProfile!!,
                onSaveProfile = { updatedProfile ->
                    viewModel.updatePatientProfile(updatedProfile)
                },
                onSkip = {
                    viewModel.skipOnboarding()
                }
            )
        }

        activeProcedureScreen != null -> {
            ProcedureDetailScreen(
                guide = activeProcedureScreen!!,
                completedSteps = uiState.completedChecklistSteps,
                onToggleStep = { viewModel.toggleProcedureStep(it) },
                onRequestEmergencyAmbulance = {
                    activeProcedureScreen = null
                    showAmbulanceModal = true
                },
                onBack = { activeProcedureScreen = null }
            )
        }

        activeChatScreen -> {
            val isSupport = uiState.activeRecipientId == HealthcareRepository.SUPPORT_DESK_RECIPIENT_ID
            val isInsurance = uiState.activeRecipientId == HealthcareRepository.INSURANCE_DESK_RECIPIENT_ID
            val activeDoctorObj = uiState.specialists.firstOrNull { it.id == uiState.activeRecipientId }
            ChatConsultationScreen(
                recipientName = uiState.activeRecipientName,
                recipientSpecialty = uiState.activeRecipientSpecialty,
                canSendMessages = uiState.canMessageRecipient(uiState.activeRecipientId),
                isSupportChat = isSupport,
                isInsuranceChat = isInsurance,
                messages = uiState.currentChatMessages,
                savedLockerRecords = uiState.medicalRecords,
                onSendMessage = { msg, attachType, attachPath, attachName, saveToLocker ->
                    viewModel.sendChatMessage(
                        message = msg,
                        attachmentType = attachType,
                        attachmentPath = attachPath,
                        attachmentName = attachName,
                        alsoSaveToLocker = saveToLocker
                    )
                },
                onBookDoctor = activeDoctorObj?.let { doc ->
                    {
                        bookingSpecialist = doc
                    }
                },
                onBack = { activeChatScreen = false }
            )
        }

        activeDoctorProfile != null -> {
            val doc = activeDoctorProfile!!
            DoctorProfileScreen(
                specialist = doc,
                isBooked = uiState.hasBookedDoctor(doc.id),
                reviews = uiState.reviews,
                onBookAppointment = { bookingSpecialist = doc },
                onOpenChat = { openDoctorChatIfBooked(doc.name, doc.id) },
                onLockedChatAttempt = { lockedChatDoctor = doc },
                onSubmitReview = { rating, comment ->
                    viewModel.submitReview(doc.id, doc.name, "DOCTOR", rating, comment)
                },
                onBack = { activeDoctorProfile = null }
            )
        }

        activeFacilityDetail != null -> {
            val facility = activeFacilityDetail!!
            val affiliatedDoctors = uiState.specialists.filter { it.hospitalId == facility.id }
            val center = uiState.diagnosticCenters.firstOrNull { it.id == facility.id }
            FacilityDetailScreen(
                facility = facility,
                affiliatedDoctors = affiliatedDoctors,
                diagnosticCenter = center,
                reviews = uiState.reviews,
                onBookDoctor = { bookingSpecialist = it },
                onOpenDoctorProfile = { activeDoctorProfile = it },
                onBookTest = { c, t -> bookingDiagnostic = Pair(c, t) },
                onRequestEmergencySos = { showAmbulanceModal = true },
                onSubmitReview = { rating, comment ->
                    val targetType = if (facility.facilityType == "HOSPITAL") "HOSPITAL" else "LAB"
                    viewModel.submitReview(facility.id, facility.name, targetType, rating, comment)
                },
                onBack = { activeFacilityDetail = null }
            )
        }

        showNotificationsScreen -> {
            NotificationsScreen(
                notifications = uiState.notifications,
                onMarkRead = { viewModel.markNotificationRead(it) },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onBack = { showNotificationsScreen = false }
            )
        }

        showHelpSupportScreen -> {
            HelpSupportScreen(
                tickets = uiState.supportTickets,
                onSubmitTicket = { cat, subj, desc -> viewModel.submitSupportTicket(cat, subj, desc) },
                onOpenCareChat = {
                    viewModel.openSupportDeskChat()
                    showHelpSupportScreen = false
                    activeChatScreen = true
                },
                onBack = { showHelpSupportScreen = false }
            )
        }

        showInsuranceHubScreen -> {
            val insuranceTickets = uiState.supportTickets.filter {
                it.category.contains("Insurance", ignoreCase = true)
            }
            InsuranceHubScreen(
                initialTab = insuranceHubInitialTab,
                userPolicies = uiState.userInsurancePolicies,
                claims = uiState.patientInsuranceClaims,
                partners = uiState.insurancePartners,
                insuranceTickets = insuranceTickets,
                onAddPolicy = { showAddPolicyModal = true },
                onDeletePolicy = { viewModel.deleteUserInsurancePolicy(it) },
                onFileClaimForPolicy = { policy ->
                    claimPreselectedPolicy = policy
                    showClaimModal = true
                },
                onBuyPartnerPlan = { partner ->
                    buyInquiryPartner = partner
                    showBuyInquiryModal = true
                },
                onRequestGeneralCallback = {
                    buyInquiryPartner = null
                    showBuyInquiryModal = true
                },
                onOpenInsuranceChat = {
                    viewModel.openInsuranceAssistanceChat()
                    activeChatScreen = true
                },
                onBack = { showInsuranceHubScreen = false }
            )
        }

        else -> {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 840.dp

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .clickable { showLocationPickerModal = true }
                                            .testTag("top_bar_location_button")
                                    ) {
                                        Text(
                                            text = strings.appName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = uiState.selectedLocationArea,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Change Location",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                LanguageSelectorButton(
                                    selectedLanguage = selectedLanguage,
                                    onSelectLanguage = { lang ->
                                        onSelectLanguage(lang)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                stringsForLanguage(lang).languageChangedToast(lang)
                                            )
                                        }
                                    }
                                )
                                IconButton(
                                    onClick = { showNotificationsScreen = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("top_bar_notifications_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (uiState.unreadNotificationCount > 0) {
                                                Badge {
                                                    Text("${uiState.unreadNotificationCount}")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = strings.notificationsLabel,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showAmbulanceModal = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("top_bar_sos_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Emergency,
                                        contentDescription = strings.navEmergency,
                                        tint = EmergencyRed
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            PatientBottomNavigationBar(
                                selectedTab = uiState.selectedTab,
                                onSelectTab = { viewModel.selectMainTab(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            PatientNavigationRail(
                                selectedTab = uiState.selectedTab,
                                onSelectTab = { viewModel.selectMainTab(it) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            when (uiState.selectedTab) {
                                MainNavTab.HOME -> {
                                    PatientHomeTab(
                                        state = uiState,
                                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                        onNavigateTab = { viewModel.selectMainTab(it) },
                                        onNavigateToBooking = { subTab, specialty ->
                                            viewModel.navigateToBooking(subTab, specialty)
                                        },
                                        onOpenDoctorProfile = { activeDoctorProfile = it },
                                        onOpenFacilityDetail = { activeFacilityDetail = it },
                                        onOpenLocationPicker = { showLocationPickerModal = true },
                                        onBookSpecialist = { bookingSpecialist = it },
                                        onBookDiagnostic = { center, test ->
                                            bookingDiagnostic = Pair(center, test)
                                        },
                                        onOpenChatWithDoctor = { doctorName, doctorId ->
                                            openDoctorChatIfBooked(doctorName, doctorId)
                                        },
                                        onLockedChatAttempt = { specialist ->
                                            lockedChatDoctor = specialist
                                        },
                                        onSelectProcedure = { activeProcedureScreen = it },
                                        onOpenInsuranceHub = {
                                            insuranceHubInitialTab = 0
                                            showInsuranceHubScreen = true
                                        },
                                        onQuickEmergencySos = {
                                            viewModel.selectMainTab(MainNavTab.EMERGENCY)
                                        }
                                    )
                                }

                                MainNavTab.BOOKING -> {
                                    PatientBookingTab(
                                        state = uiState,
                                        onSelectSubTab = { viewModel.selectBookingSubTab(it) },
                                        onSelectSpecialty = { viewModel.selectSpecialtyFilter(it) },
                                        onOpenDoctorProfile = { activeDoctorProfile = it },
                                        onBookDoctor = { bookingSpecialist = it },
                                        onBookTest = { center, test ->
                                            bookingDiagnostic = Pair(center, test)
                                        },
                                        onBookHomeCare = { bookingHomeCare = it },
                                        onRescheduleAppointment = {
                                            rescheduleTarget = RescheduleTarget.DoctorAppt(it)
                                        },
                                        onCancelAppointment = { viewModel.cancelAppointment(it) },
                                        onRescheduleDiagnostic = {
                                            rescheduleTarget = RescheduleTarget.LabTest(it)
                                        },
                                        onCancelDiagnostic = { viewModel.cancelDiagnosticBooking(it) },
                                        onRescheduleHomeCare = {
                                            rescheduleTarget = RescheduleTarget.HomeCare(it)
                                        },
                                        onCancelHomeCare = { viewModel.cancelHomeCare(it) },
                                        onOpenChatWithDoctor = { doctorName, doctorId ->
                                            openDoctorChatIfBooked(doctorName, doctorId)
                                        },
                                        onLockedChatAttempt = { specialist ->
                                            lockedChatDoctor = specialist
                                        }
                                    )
                                }

                                MainNavTab.EMERGENCY -> {
                                    val activeAmbulance = uiState.patientAmbulanceRequests.firstOrNull {
                                        it.status != "COMPLETED"
                                    }
                                    PatientEmergencyTab(
                                        activeAmbulance = activeAmbulance,
                                        allRequests = uiState.patientAmbulanceRequests,
                                        emergencyHospitals = uiState.nearbyFacilities.filter { it.hasEmergencyUnit },
                                        onTriggerSOS = { showAmbulanceModal = true },
                                        onResolveAmbulance = { id ->
                                            viewModel.updateAmbulanceStatus(id, "COMPLETED")
                                        }
                                    )
                                }

                                MainNavTab.RECORDS -> {
                                    PatientRecordsTab(
                                        records = uiState.medicalRecords,
                                        diagnosticBookings = uiState.patientDiagnosticBookings,
                                        procedureGuides = uiState.procedureGuides,
                                        onAddRecord = { showAddRecordModal = true },
                                        onSelectProcedure = { activeProcedureScreen = it }
                                    )
                                }

                                MainNavTab.PROFILE -> {
                                    PatientProfileTab(
                                        profile = uiState.userProfile,
                                        familyMembers = uiState.familyMembers,
                                        paymentRecords = uiState.paymentRecords,
                                        userPolicies = uiState.userInsurancePolicies,
                                        tiedUpInsurerCount = uiState.insurancePartners.size,
                                        claims = uiState.patientInsuranceClaims,
                                        onEditProfile = { showEditProfileModal = true },
                                        onOpenLocationPicker = { showLocationPickerModal = true },
                                        onAddFamilyMember = { showAddFamilyMemberModal = true },
                                        onDeleteFamilyMember = { viewModel.deleteFamilyMember(it) },
                                        onOpenInsuranceHub = { tabIndex ->
                                            insuranceHubInitialTab = tabIndex
                                            showInsuranceHubScreen = true
                                        },
                                        onAddInsurancePolicy = { showAddPolicyModal = true },
                                        onSubmitClaim = { policy ->
                                            claimPreselectedPolicy = policy
                                            showClaimModal = true
                                        },
                                        onOpenHelpSupport = { showHelpSupportScreen = true },
                                        onOpenSupportChat = {
                                            viewModel.openSupportDeskChat()
                                            activeChatScreen = true
                                        },
                                        onSignOut = {
                                            appSignOut(
                                                context = context,
                                                credentialManager = credentialManager,
                                                scope = coroutineScope,
                                                onComplete = onSignOutRequested
                                            )
                                        }
                                    )
                                }
                            }

                            if (uiState.isLoading) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Patient Modals
    if (showAmbulanceModal) {
        AmbulanceDispatchModal(
            defaultAddress = uiState.userProfile?.savedAddress?.ifBlank { uiState.selectedLocationArea } ?: uiState.selectedLocationArea,
            defaultPhone = uiState.userProfile?.phone ?: "",
            onDismiss = { showAmbulanceModal = false },
            onConfirm = { pickup, emergencyType, ambulanceType, phone ->
                viewModel.requestAmbulance(pickup, emergencyType, ambulanceType, phone)
                showAmbulanceModal = false
            }
        )
    }

    if (bookingSpecialist != null) {
        BookSpecialistModal(
            specialist = bookingSpecialist!!,
            familyMembers = uiState.familyMembers,
            onDismiss = { bookingSpecialist = null },
            onConfirm = { date, slot, mode, notes, memberName, paymentMethod ->
                viewModel.scheduleAppointment(bookingSpecialist!!, date, slot, mode, notes, memberName, paymentMethod)
                bookingSpecialist = null
            }
        )
    }

    if (bookingDiagnostic != null) {
        val (center, test) = bookingDiagnostic!!
        BookDiagnosticModal(
            center = center,
            test = test,
            familyMembers = uiState.familyMembers,
            defaultAddress = uiState.userProfile?.savedAddress ?: "",
            onDismiss = { bookingDiagnostic = null },
            onConfirm = { date, timeWindow, collectionType, address, memberName, paymentMethod ->
                viewModel.bookDiagnosticTest(center, test, date, timeWindow, collectionType, address, memberName, paymentMethod)
                bookingDiagnostic = null
            }
        )
    }

    if (bookingHomeCare != null) {
        BookHomeCareModal(
            service = bookingHomeCare!!,
            familyMembers = uiState.familyMembers,
            defaultAddress = uiState.userProfile?.savedAddress ?: "",
            onDismiss = { bookingHomeCare = null },
            onConfirm = { memberName, address, date, window, notes, paymentMethod ->
                viewModel.bookHomeCare(bookingHomeCare!!, memberName, address, date, window, notes, paymentMethod)
                bookingHomeCare = null
            }
        )
    }

    if (rescheduleTarget != null) {
        val target = rescheduleTarget!!
        val (title, current) = when (target) {
            is RescheduleTarget.DoctorAppt -> target.appointment.specialistName to "${target.appointment.date} at ${target.appointment.timeSlot}"
            is RescheduleTarget.LabTest -> target.booking.testName to "${target.booking.scheduledDate} (${target.booking.scheduledTime})"
            is RescheduleTarget.HomeCare -> target.booking.serviceTitle to "${target.booking.scheduledDate} (${target.booking.timeWindow})"
        }
        RescheduleBookingModal(
            title = title,
            currentSchedule = current,
            onDismiss = { rescheduleTarget = null },
            onConfirm = { newDate, newSlot ->
                when (target) {
                    is RescheduleTarget.DoctorAppt -> viewModel.rescheduleAppointment(target.appointment.id, newDate, newSlot)
                    is RescheduleTarget.LabTest -> viewModel.rescheduleDiagnosticBooking(target.booking.id, newDate, newSlot)
                    is RescheduleTarget.HomeCare -> viewModel.rescheduleHomeCare(target.booking.id, newDate, newSlot)
                }
                rescheduleTarget = null
            }
        )
    }

    if (showClaimModal) {
        val defaultHospital = if (HealthcareCatalog.isBiswanathArea(uiState.selectedLocationArea)) {
            "Pragati Hospital & Research Centre, Biswanath"
        } else {
            "Baptist Christian Hospital, Tezpur"
        }
        SubmitInsuranceClaimModal(
            userPolicies = uiState.userInsurancePolicies,
            preselectedPolicy = claimPreselectedPolicy,
            defaultHospital = defaultHospital,
            onDismiss = {
                showClaimModal = false
                claimPreselectedPolicy = null
            },
            onConfirm = { policy, provider, hospital, procedure, amount, claimType, docRef ->
                viewModel.submitInsuranceClaim(policy, provider, hospital, procedure, amount, claimType, docRef)
                showClaimModal = false
                claimPreselectedPolicy = null
            }
        )
    }

    if (showAddPolicyModal) {
        val defaultMembers = if (uiState.familyMembers.isEmpty()) {
            "Self"
        } else {
            "Self + ${uiState.familyMembers.size} family member(s)"
        }
        AddInsurancePolicyModal(
            defaultMembers = defaultMembers,
            onDismiss = { showAddPolicyModal = false },
            onConfirm = { newPolicy ->
                viewModel.addUserInsurancePolicy(newPolicy)
                showAddPolicyModal = false
            }
        )
    }

    if (showBuyInquiryModal) {
        BuyInsuranceInquiryModal(
            partner = buyInquiryPartner,
            defaultPhone = uiState.userProfile?.phone ?: "",
            defaultLocation = uiState.selectedLocationArea,
            onDismiss = {
                showBuyInquiryModal = false
                buyInquiryPartner = null
            },
            onConfirm = { partnerName, planName, inquiryType, cover, members, phone, notes ->
                viewModel.submitInsuranceInquiry(partnerName, planName, inquiryType, cover, members, phone, notes)
                showBuyInquiryModal = false
                buyInquiryPartner = null
            }
        )
    }

    if (showAddRecordModal) {
        AddMedicalRecordModal(
            familyMembers = uiState.familyMembers,
            onDismiss = { showAddRecordModal = false },
            onConfirm = { title, cat, doc, date, summary, metrics, memberName, fileType, attachmentPath, attachmentName ->
                viewModel.addMedicalRecord(
                    title = title,
                    category = cat,
                    doctorOrFacility = doc,
                    recordDate = date,
                    summary = summary,
                    keyMetrics = metrics,
                    memberName = memberName,
                    fileType = fileType,
                    attachmentPath = attachmentPath,
                    attachmentName = attachmentName
                )
                showAddRecordModal = false
            }
        )
    }

    if (showEditProfileModal) {
        EditPatientProfileModal(
            currentProfile = uiState.userProfile ?: UserProfile(),
            onDismiss = { showEditProfileModal = false },
            onConfirm = { updated ->
                viewModel.updatePatientProfile(updated)
                showEditProfileModal = false
            }
        )
    }

    if (showAddFamilyMemberModal) {
        AddFamilyMemberModal(
            onDismiss = { showAddFamilyMemberModal = false },
            onConfirm = { member ->
                viewModel.addFamilyMember(member)
                showAddFamilyMemberModal = false
            }
        )
    }

    if (showLocationPickerModal) {
        LocationPickerModal(
            currentArea = uiState.selectedLocationArea,
            currentAddress = uiState.userProfile?.savedAddress ?: "",
            onDismiss = { showLocationPickerModal = false },
            onConfirm = { area, address ->
                viewModel.updateLocation(area, address)
                showLocationPickerModal = false
            }
        )
    }

    lockedChatDoctor?.let { doc ->
        AlertDialog(
            onDismissRequest = { lockedChatDoctor = null },
            title = {
                Text(
                    text = "Book Appointment to Message",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "To prevent doctor inbox overflow and keep clinical care organized, only booked patients can directly message ${doc.name} and share prescriptions. Book a consultation slot to unlock chat immediately."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = doc
                        lockedChatDoctor = null
                        bookingSpecialist = target
                    },
                    modifier = Modifier.testTag("locked_chat_book_now_button")
                ) {
                    Text("Book Appointment")
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedChatDoctor = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PatientBottomNavigationBar(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit
) {
    val strings = LocalAppStrings.current
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("patient_bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == MainNavTab.HOME,
            onClick = { onSelectTab(MainNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = strings.navHome
                )
            },
            label = { Text(strings.navHome, maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.BOOKING,
            onClick = { onSelectTab(MainNavTab.BOOKING) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.BOOKING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = strings.navBooking
                )
            },
            label = { Text(strings.navBooking, maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_booking")
        )

        val emergencySelected = selectedTab == MainNavTab.EMERGENCY
        NavigationBarItem(
            selected = emergencySelected,
            onClick = { onSelectTab(MainNavTab.EMERGENCY) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (emergencySelected) EmergencyRed else EmergencyRedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Emergency,
                        contentDescription = strings.navEmergency,
                        tint = if (emergencySelected) Color.White else EmergencyRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = null,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = strings.navEmergency }
                .testTag("nav_tab_emergency")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.RECORDS,
            onClick = { onSelectTab(MainNavTab.RECORDS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.RECORDS) Icons.Filled.FolderShared else Icons.Outlined.FolderShared,
                    contentDescription = strings.navRecords
                )
            },
            label = { Text(strings.navRecords, maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_records")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { onSelectTab(MainNavTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = strings.navProfile
                )
            },
            label = { Text(strings.navProfile, maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}

@Composable
private fun PatientNavigationRail(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit
) {
    val strings = LocalAppStrings.current
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationRailItem(
            selected = selectedTab == MainNavTab.HOME,
            onClick = { onSelectTab(MainNavTab.HOME) },
            icon = { Icon(if (selectedTab == MainNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, strings.navHome) },
            label = { Text(strings.navHome) }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.BOOKING,
            onClick = { onSelectTab(MainNavTab.BOOKING) },
            icon = { Icon(if (selectedTab == MainNavTab.BOOKING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, strings.navBooking) },
            label = { Text(strings.navBooking) }
        )
        val emergencySelected = selectedTab == MainNavTab.EMERGENCY
        NavigationRailItem(
            selected = emergencySelected,
            onClick = { onSelectTab(MainNavTab.EMERGENCY) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (emergencySelected) EmergencyRed else EmergencyRedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Emergency,
                        contentDescription = strings.navEmergency,
                        tint = if (emergencySelected) Color.White else EmergencyRed
                    )
                }
            }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.RECORDS,
            onClick = { onSelectTab(MainNavTab.RECORDS) },
            icon = { Icon(if (selectedTab == MainNavTab.RECORDS) Icons.Filled.FolderShared else Icons.Outlined.FolderShared, strings.navRecords) },
            label = { Text(strings.navRecords) }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { onSelectTab(MainNavTab.PROFILE) },
            icon = { Icon(if (selectedTab == MainNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, strings.navProfile) },
            label = { Text(strings.navProfile) }
        )
    }
}
