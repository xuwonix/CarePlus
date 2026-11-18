package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.appSignOut
import com.example.ui.screens.chat.ChatConsultationScreen
import com.example.ui.screens.patient.AddMedicalRecordModal
import com.example.ui.screens.patient.AmbulanceDispatchModal
import com.example.ui.screens.patient.BookDiagnosticModal
import com.example.ui.screens.patient.BookSpecialistModal
import com.example.ui.screens.patient.PatientBookingTab
import com.example.ui.screens.patient.PatientEmergencyTab
import com.example.ui.screens.patient.PatientHomeTab
import com.example.ui.screens.patient.PatientProfileTab
import com.example.ui.screens.patient.PatientRecordsTab
import com.example.ui.screens.patient.SubmitInsuranceClaimModal
import com.example.ui.screens.procedures.ProcedureDetailScreen
import com.example.ui.theme.CarePulseTheme
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.viewmodel.CarePulseViewModel
import com.example.ui.viewmodel.MainNavTab
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

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
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            val viewModel: CarePulseViewModel = viewModel()
            PatientMainApp(
                viewModel = viewModel,
                onSignOutRequested = {
                    currentUser = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMainApp(
    viewModel: CarePulseViewModel,
    onSignOutRequested: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeProcedureScreen by remember { mutableStateOf<ProcedureGuide?>(null) }
    var activeChatScreen by remember { mutableStateOf(false) }

    var showAmbulanceModal by remember { mutableStateOf(false) }
    var bookingSpecialist by remember { mutableStateOf<Specialist?>(null) }
    var bookingDiagnostic by remember { mutableStateOf<Pair<DiagnosticCenter, DiagnosticTest>?>(null) }
    var showClaimModal by remember { mutableStateOf(false) }
    var showAddRecordModal by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    when {
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
            ChatConsultationScreen(
                recipientName = uiState.activeRecipientName,
                messages = uiState.currentChatMessages,
                onSendMessage = { msg -> viewModel.sendChatMessage(msg) },
                onBack = { activeChatScreen = false }
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
                                    Column {
                                        Text(
                                            text = "CarePulse",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Patient Care Portal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showAmbulanceModal = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("top_bar_sos_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Emergency,
                                        contentDescription = "Quick Emergency SOS",
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
                                        onBookSpecialist = { bookingSpecialist = it },
                                        onBookDiagnostic = { center, test ->
                                            bookingDiagnostic = Pair(center, test)
                                        },
                                        onOpenChatWithDoctor = { doctorName, doctorId ->
                                            viewModel.openConsultationChat("consult_$doctorId", doctorName, doctorId)
                                            activeChatScreen = true
                                        },
                                        onSelectProcedure = { activeProcedureScreen = it },
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
                                        onBookDoctor = { bookingSpecialist = it },
                                        onBookTest = { center, test ->
                                            bookingDiagnostic = Pair(center, test)
                                        },
                                        onCancelAppointment = { viewModel.cancelAppointment(it) },
                                        onOpenChatWithDoctor = { doctorName, doctorId ->
                                            viewModel.openConsultationChat("consult_$doctorId", doctorName, doctorId)
                                            activeChatScreen = true
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
                                        claims = uiState.patientInsuranceClaims,
                                        onSubmitClaim = { showClaimModal = true },
                                        onOpenSupportChat = {
                                            viewModel.openConsultationChat(
                                                "consult_doc_cardio_1",
                                                "Dr. Elena Rostova, MD",
                                                "doc_cardio_1"
                                            )
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
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary
                                        )
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
            onDismiss = { bookingSpecialist = null },
            onConfirm = { date, slot, mode, notes ->
                viewModel.scheduleAppointment(bookingSpecialist!!, date, slot, mode, notes)
                bookingSpecialist = null
            }
        )
    }

    if (bookingDiagnostic != null) {
        val (center, test) = bookingDiagnostic!!
        BookDiagnosticModal(
            center = center,
            test = test,
            onDismiss = { bookingDiagnostic = null },
            onConfirm = { date, collectionType ->
                viewModel.bookDiagnosticTest(center, test, date, collectionType)
                bookingDiagnostic = null
            }
        )
    }

    if (showClaimModal) {
        SubmitInsuranceClaimModal(
            onDismiss = { showClaimModal = false },
            onConfirm = { policy, provider, hospital, procedure, amount, claimType, docRef ->
                viewModel.submitInsuranceClaim(policy, provider, hospital, procedure, amount, claimType, docRef)
                showClaimModal = false
            }
        )
    }

    if (showAddRecordModal) {
        AddMedicalRecordModal(
            onDismiss = { showAddRecordModal = false },
            onConfirm = { title, cat, doc, date, summary, metrics ->
                viewModel.addMedicalRecord(title, cat, doc, date, summary, metrics)
                showAddRecordModal = false
            }
        )
    }
}

@Composable
private fun PatientBottomNavigationBar(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit
) {
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
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.BOOKING,
            onClick = { onSelectTab(MainNavTab.BOOKING) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.BOOKING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Booking"
                )
            },
            label = { Text("Booking", maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_booking")
        )

        // Center Emergency Alert (Symbol) Tab
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
                        contentDescription = "Emergency Alert",
                        tint = if (emergencySelected) Color.White else EmergencyRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = null,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .semantics { contentDescription = "Emergency Alert" }
                .testTag("nav_tab_emergency")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.RECORDS,
            onClick = { onSelectTab(MainNavTab.RECORDS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.RECORDS) Icons.Filled.FolderShared else Icons.Outlined.FolderShared,
                    contentDescription = "Records"
                )
            },
            label = { Text("Records", maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_records")
        )

        NavigationBarItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { onSelectTab(MainNavTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", maxLines = 1) },
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}

@Composable
private fun PatientNavigationRail(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationRailItem(
            selected = selectedTab == MainNavTab.HOME,
            onClick = { onSelectTab(MainNavTab.HOME) },
            icon = { Icon(if (selectedTab == MainNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, "Home") },
            label = { Text("Home") }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.BOOKING,
            onClick = { onSelectTab(MainNavTab.BOOKING) },
            icon = { Icon(if (selectedTab == MainNavTab.BOOKING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, "Booking") },
            label = { Text("Booking") }
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
                        contentDescription = "Emergency Alert",
                        tint = if (emergencySelected) Color.White else EmergencyRed
                    )
                }
            }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.RECORDS,
            onClick = { onSelectTab(MainNavTab.RECORDS) },
            icon = { Icon(if (selectedTab == MainNavTab.RECORDS) Icons.Filled.FolderShared else Icons.Outlined.FolderShared, "Records") },
            label = { Text("Records") }
        )
        NavigationRailItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { onSelectTab(MainNavTab.PROFILE) },
            icon = { Icon(if (selectedTab == MainNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, "Profile") },
            label = { Text("Profile") }
        )
    }
}
