package com.example.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AmbulanceRequest
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.InsuranceClaim
import com.example.data.model.MedicalRecord
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.ui.viewmodel.HealthcareUiState

@Composable
fun PatientHomeScreen(
    state: HealthcareUiState,
    onRequestAmbulance: (pickup: String, emergencyType: String, ambulanceType: String, phone: String) -> Unit,
    onCancelAmbulance: (id: String) -> Unit,
    onBookSpecialist: (specialist: Specialist, date: String, timeSlot: String, mode: String, notes: String) -> Unit,
    onBookDiagnostic: (center: DiagnosticCenter, test: DiagnosticTest, date: String, collectionType: String) -> Unit,
    onSubmitClaim: (policy: String, provider: String, hospital: String, procedure: String, amount: Double, claimType: String, docRef: String) -> Unit,
    onAddRecord: (title: String, category: String, doctor: String, date: String, summary: String, metrics: String) -> Unit,
    onSelectProcedure: (ProcedureGuide) -> Unit,
    onOpenChatWithDoctor: (doctorName: String, doctorId: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Specialists", "Ambulance", "Diagnostics", "Insurance", "Procedures", "Health Locker")

    var showAmbulanceModal by remember { mutableStateOf(false) }
    var bookingSpecialist by remember { mutableStateOf<Specialist?>(null) }
    var bookingDiagnostic by remember { mutableStateOf<Pair<DiagnosticCenter, DiagnosticTest>?>(null) }
    var showClaimModal by remember { mutableStateOf(false) }
    var showAddRecordModal by remember { mutableStateOf(false) }

    val activeAmbulance = state.patientAmbulanceRequests.firstOrNull { it.status != "COMPLETED" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Active Emergency Tracking Card if ambulance is requested
            if (activeAmbulance != null) {
                ActiveAmbulanceTrackerCard(
                    ambulance = activeAmbulance,
                    onCancel = { onCancelAmbulance(activeAmbulance.id) }
                )
            } else {
                // Emergency SOS 1-Tap Trigger Banner
                EmergencySOSBanner(
                    onTriggerSOS = { showAmbulanceModal = true }
                )
            }

            // Scrollable Navigation Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("patient_tab_${title.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Main Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> SpecialistsTabContent(
                        specialists = state.specialists,
                        myAppointments = state.patientAppointments,
                        onBookClicked = { bookingSpecialist = it },
                        onMessageClicked = { specialist ->
                            onOpenChatWithDoctor(specialist.name, specialist.id)
                        }
                    )
                    1 -> AmbulanceTabContent(
                        activeAmbulance = activeAmbulance,
                        allRequests = state.patientAmbulanceRequests,
                        onRequestAmbulance = { showAmbulanceModal = true }
                    )
                    2 -> DiagnosticsTabContent(
                        centers = state.diagnosticCenters,
                        bookings = state.patientDiagnosticBookings,
                        onBookTest = { center, test ->
                            bookingDiagnostic = Pair(center, test)
                        }
                    )
                    3 -> InsuranceTabContent(
                        claims = state.patientInsuranceClaims,
                        onSubmitClaim = { showClaimModal = true }
                    )
                    4 -> ProceduresTabContent(
                        guides = state.procedureGuides,
                        onSelectGuide = onSelectProcedure
                    )
                    5 -> HealthLockerTabContent(
                        records = state.medicalRecords,
                        onAddRecord = { showAddRecordModal = true }
                    )
                }
            }
        }

        // FAB for fast actions based on active tab
        if (selectedTab == 5) {
            FloatingActionButton(
                onClick = { showAddRecordModal = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_medical_record_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medical Record")
            }
        }
    }

    // -------------------------------------------------------------
    // MODALS & DIALOGS
    // -------------------------------------------------------------
    if (showAmbulanceModal) {
        AmbulanceDispatchModal(
            onDismiss = { showAmbulanceModal = false },
            onConfirm = { pickup, emergencyType, ambulanceType, phone ->
                onRequestAmbulance(pickup, emergencyType, ambulanceType, phone)
                showAmbulanceModal = false
            }
        )
    }

    if (bookingSpecialist != null) {
        BookSpecialistModal(
            specialist = bookingSpecialist!!,
            onDismiss = { bookingSpecialist = null },
            onConfirm = { date, slot, mode, notes ->
                onBookSpecialist(bookingSpecialist!!, date, slot, mode, notes)
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
                onBookDiagnostic(center, test, date, collectionType)
                bookingDiagnostic = null
            }
        )
    }

    if (showClaimModal) {
        SubmitInsuranceClaimModal(
            onDismiss = { showClaimModal = false },
            onConfirm = { policy, provider, hospital, procedure, amount, claimType, docRef ->
                onSubmitClaim(policy, provider, hospital, procedure, amount, claimType, docRef)
                showClaimModal = false
            }
        )
    }

    if (showAddRecordModal) {
        AddMedicalRecordModal(
            onDismiss = { showAddRecordModal = false },
            onConfirm = { title, cat, doc, date, summary, metrics ->
                onAddRecord(title, cat, doc, date, summary, metrics)
                showAddRecordModal = false
            }
        )
    }
}

// -----------------------------------------------------------------
// EMERGENCY SOS BANNER & ACTIVE EMS TRACKER
// -----------------------------------------------------------------
@Composable
private fun EmergencySOSBanner(onTriggerSOS: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("emergency_sos_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergency SOS",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Emergency Ambulance SOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Instant BLS/ACLS fleet • Avg ETA 7 mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onTriggerSOS,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("sos_button")
            ) {
                Text(
                    text = "Request EMS",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActiveAmbulanceTrackerCard(
    ambulance: AmbulanceRequest,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("active_ambulance_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ambulance En Route (${ambulance.vehicleNumber})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(ambulance.status) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = when (ambulance.status) {
                "DISPATCHED" -> 0.25f
                "EN_ROUTE" -> 0.65f
                "ARRIVED" -> 0.95f
                else -> 1f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Paramedic Alpha (${ambulance.driverPhone})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Pickup: ${ambulance.pickupAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ETA: ${ambulance.etaMinutes} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = ambulance.ambulanceType.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resolve / Cancel")
                }
            }
        }
    }
}
