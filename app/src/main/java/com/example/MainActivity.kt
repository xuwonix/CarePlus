package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ProcedureGuide
import com.example.data.model.UserRole
import com.example.ui.components.RoleSwitchDialog
import com.example.ui.screens.ambulance.AmbulanceDispatcherScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.appSignOut
import com.example.ui.screens.chat.ChatConsultationScreen
import com.example.ui.screens.diagnostics.DiagnosticsCenterScreen
import com.example.ui.screens.insurance.InsurancePortalScreen
import com.example.ui.screens.patient.PatientHomeScreen
import com.example.ui.screens.procedures.ProcedureDetailScreen
import com.example.ui.screens.specialist.SpecialistDashboardScreen
import com.example.ui.theme.CarePulseTheme
import com.example.ui.viewmodel.CarePulseViewModel
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
            MainPortalContainer(
                currentUserId = uid,
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
fun MainPortalContainer(
    currentUserId: String,
    viewModel: CarePulseViewModel,
    onSignOutRequested: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRoleDialog by remember { mutableStateOf(false) }
    var activeProcedureScreen by remember { mutableStateOf<ProcedureGuide?>(null) }
    var activeChatScreen by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                text = "Healthcare Network",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    // Role Switch Pill Button
                    SuggestionChip(
                        onClick = { showRoleDialog = true },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val roleIcon: ImageVector = when (uiState.currentRole) {
                                    UserRole.PATIENT -> Icons.Default.Person
                                    UserRole.SPECIALIST -> Icons.Default.MedicalServices
                                    UserRole.AMBULANCE_DISPATCHER -> Icons.Default.Emergency
                                    UserRole.DIAGNOSTICS_TECH -> Icons.Default.Biotech
                                    UserRole.INSURANCE_OFFICER -> Icons.Default.VerifiedUser
                                }
                                Icon(
                                    imageVector = roleIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.currentRole.title.substringBefore(" "),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Role",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("role_switch_chip")
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Sign Out button
                    IconButton(
                        onClick = {
                            appSignOut(
                                context = context,
                                credentialManager = credentialManager,
                                scope = coroutineScope,
                                onComplete = onSignOutRequested
                            )
                        },
                        modifier = Modifier.testTag("app_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                activeProcedureScreen != null -> {
                    ProcedureDetailScreen(
                        guide = activeProcedureScreen!!,
                        completedSteps = uiState.completedChecklistSteps,
                        onToggleStep = { viewModel.toggleProcedureStep(it) },
                        onRequestEmergencyAmbulance = {
                            activeProcedureScreen = null
                            viewModel.switchRole(UserRole.PATIENT)
                            viewModel.requestAmbulance(
                                pickupAddress = "420 Maple Avenue, Apt 3B",
                                emergencyType = "POST_PROCEDURE_COMPLICATION",
                                ambulanceType = "ADVANCED_CARDIAC",
                                patientPhone = "+1 (555) 911-3829"
                            )
                        },
                        onBack = { activeProcedureScreen = null }
                    )
                }

                activeChatScreen -> {
                    ChatConsultationScreen(
                        recipientName = uiState.activeRecipientName,
                        messages = uiState.currentChatMessages,
                        onSendMessage = { msg ->
                            viewModel.sendChatMessage(msg)
                        },
                        onBack = { activeChatScreen = false }
                    )
                }

                else -> {
                    when (uiState.currentRole) {
                        UserRole.PATIENT -> {
                            PatientHomeScreen(
                                state = uiState,
                                onRequestAmbulance = { pickup, emergencyType, ambulanceType, phone ->
                                    viewModel.requestAmbulance(pickup, emergencyType, ambulanceType, phone)
                                },
                                onCancelAmbulance = { id ->
                                    viewModel.updateAmbulanceStatus(id, "COMPLETED")
                                },
                                onBookSpecialist = { specialist, date, slot, mode, notes ->
                                    viewModel.scheduleAppointment(specialist, date, slot, mode, notes)
                                },
                                onBookDiagnostic = { center, test, date, collectionType ->
                                    viewModel.bookDiagnosticTest(center, test, date, collectionType)
                                },
                                onSubmitClaim = { policy, provider, hospital, procedure, amount, claimType, docRef ->
                                    viewModel.submitInsuranceClaim(policy, provider, hospital, procedure, amount, claimType, docRef)
                                },
                                onAddRecord = { title, cat, doc, date, summary, metrics ->
                                    viewModel.addMedicalRecord(title, cat, doc, date, summary, metrics)
                                },
                                onSelectProcedure = { guide ->
                                    activeProcedureScreen = guide
                                },
                                onOpenChatWithDoctor = { doctorName, doctorId ->
                                    viewModel.openConsultationChat("consult_${doctorId}", doctorName, doctorId)
                                    activeChatScreen = true
                                }
                            )
                        }

                        UserRole.SPECIALIST -> {
                            SpecialistDashboardScreen(
                                appointments = uiState.patientAppointments,
                                onUpdateStatus = { appointmentId, status, rx ->
                                    viewModel.updateAppointmentStatus(appointmentId, status, rx)
                                },
                                onOpenChat = { patientName, patientId ->
                                    viewModel.openConsultationChat("consult_${patientId}", patientName, patientId)
                                    activeChatScreen = true
                                }
                            )
                        }

                        UserRole.AMBULANCE_DISPATCHER -> {
                            AmbulanceDispatcherScreen(
                                requests = uiState.patientAmbulanceRequests,
                                onUpdateStatus = { requestId, status, eta ->
                                    viewModel.updateAmbulanceStatus(requestId, status, eta)
                                }
                            )
                        }

                        UserRole.DIAGNOSTICS_TECH -> {
                            DiagnosticsCenterScreen(
                                bookings = uiState.patientDiagnosticBookings,
                                onUpdateBooking = { bookingId, status, summary, abnormal ->
                                    viewModel.updateDiagnosticStatus(bookingId, status, summary, abnormal)
                                }
                            )
                        }

                        UserRole.INSURANCE_OFFICER -> {
                            InsurancePortalScreen(
                                claims = uiState.patientInsuranceClaims,
                                onReviewClaim = { claimId, status, amount, notes ->
                                    viewModel.reviewInsuranceClaim(claimId, status, amount, notes)
                                }
                            )
                        }
                    }
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

    if (showRoleDialog) {
        RoleSwitchDialog(
            currentRole = uiState.currentRole,
            onRoleSelected = { newRole ->
                viewModel.switchRole(newRole)
                activeProcedureScreen = null
                activeChatScreen = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}
