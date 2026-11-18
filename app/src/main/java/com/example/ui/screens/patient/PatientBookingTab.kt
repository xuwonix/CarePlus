package com.example.ui.screens.patient

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticBooking
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.Specialist
import com.example.ui.viewmodel.HealthcareUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientBookingTab(
    state: HealthcareUiState,
    onSelectSubTab: (Int) -> Unit,
    onSelectSpecialty: (String) -> Unit,
    onBookDoctor: (Specialist) -> Unit,
    onBookTest: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onOpenChatWithDoctor: (doctorName: String, doctorId: String) -> Unit
) {
    val subTabs = listOf("Doctors", "Lab Tests", "My Bookings")
    var localSearch by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_booking_tab")
    ) {
        SecondaryTabRow(
            selectedTabIndex = state.bookingSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.bookingSubTab == index,
                    onClick = { onSelectSubTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (state.bookingSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("booking_subtab_${title.lowercase().replace(" ", "_")}")
                )
            }
        }

        when (state.bookingSubTab) {
            0 -> DoctorsBookingSection(
                specialists = state.specialists,
                selectedSpecialty = state.selectedSpecialtyFilter,
                searchQuery = localSearch,
                onSearchChange = { localSearch = it },
                onSelectSpecialty = onSelectSpecialty,
                onBook = onBookDoctor,
                onChat = { onOpenChatWithDoctor(it.name, it.id) }
            )
            1 -> DiagnosticsBookingSection(
                centers = state.diagnosticCenters,
                searchQuery = localSearch,
                onSearchChange = { localSearch = it },
                onBookTest = onBookTest
            )
            2 -> MyBookingsSection(
                appointments = state.patientAppointments,
                diagnosticBookings = state.patientDiagnosticBookings,
                onCancelAppointment = onCancelAppointment,
                onOpenChatWithDoctor = onOpenChatWithDoctor,
                onGoToDoctors = { onSelectSubTab(0) }
            )
        }
    }
}

@Composable
private fun DoctorsBookingSection(
    specialists: List<Specialist>,
    selectedSpecialty: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectSpecialty: (String) -> Unit,
    onBook: (Specialist) -> Unit,
    onChat: (Specialist) -> Unit
) {
    val specialties = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology", "General Medicine")

    val filtered = remember(specialists, selectedSpecialty, searchQuery) {
        specialists.filter { doc ->
            val matchesSpec = selectedSpecialty == "All" || doc.specialty.equals(selectedSpecialty, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                doc.name.contains(searchQuery, ignoreCase = true) ||
                doc.specialty.contains(searchQuery, ignoreCase = true) ||
                doc.clinicHospital.contains(searchQuery, ignoreCase = true)
            matchesSpec && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter doctors or hospitals...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(specialties) { spec ->
                    FilterChip(
                        selected = selectedSpecialty.equals(spec, ignoreCase = true),
                        onClick = { onSelectSpecialty(spec) },
                        label = { Text(spec) },
                        modifier = Modifier.testTag("booking_filter_${spec.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        items(filtered) { specialist ->
            BookingSpecialistCard(
                specialist = specialist,
                onBook = { onBook(specialist) },
                onChat = { onChat(specialist) }
            )
        }
    }
}

@Composable
private fun BookingSpecialistCard(
    specialist: Specialist,
    onBook: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("specialist_card_${specialist.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = specialist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${specialist.specialty} • ${specialist.experienceYears}y exp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${specialist.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = specialist.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${specialist.clinicHospital} • ${specialist.address}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next: ${specialist.nextAvailableSlot}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$${specialist.fee.toInt()} / visit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onChat,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_doctor_button_${specialist.id}")
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat")
                }
                Button(
                    onClick = onBook,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("book_doctor_button_${specialist.id}")
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Book Slot")
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsBookingSection(
    centers: List<DiagnosticCenter>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBookTest: (DiagnosticCenter, DiagnosticTest) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search blood panels, MRI, ECG, ultrasound...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(centers) { center ->
            val matchingTests = if (searchQuery.isBlank()) {
                center.testsOffered
            } else {
                center.testsOffered.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true) ||
                        center.name.contains(searchQuery, ignoreCase = true)
                }
            }

            if (matchingTests.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = center.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${center.address} • ${center.distanceKm} km away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (center.providesHomeCollection) {
                                SuggestionChip(onClick = {}, label = { Text("Home Pickup") })
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        matchingTests.forEach { test ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = test.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${test.sampleType} • ${test.turnaroundTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Prep: ${test.preparationSummary}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$${test.price.toInt()}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { onBookTest(center, test) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("book_test_${test.id}")
                                        ) {
                                            Text("Book", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyBookingsSection(
    appointments: List<Appointment>,
    diagnosticBookings: List<DiagnosticBooking>,
    onCancelAppointment: (String) -> Unit,
    onOpenChatWithDoctor: (String, String) -> Unit,
    onGoToDoctors: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Doctor Appointments (${appointments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No appointments booked yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = onGoToDoctors) {
                            Text("Browse Specialists")
                        }
                    }
                }
            }
        } else {
            items(appointments) { appt ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(appt.specialistName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${appt.specialty} • ${appt.date} at ${appt.timeSlot}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(appt.clinicHospital, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            SuggestionChip(onClick = {}, label = { Text(appt.status) })
                        }
                        if (appt.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Notes: ${appt.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (appt.prescription.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Rx: ${appt.prescription}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        if (appt.status == "SCHEDULED") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenChatWithDoctor(appt.specialistName, appt.specialistId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat")
                                }
                                OutlinedButton(
                                    onClick = { onCancelAppointment(appt.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Diagnostic Lab Bookings (${diagnosticBookings.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (diagnosticBookings.isEmpty()) {
            item {
                Text(
                    text = "No diagnostic tests scheduled yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(diagnosticBookings) { booking ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${booking.centerName} • ${booking.scheduledDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            SuggestionChip(onClick = {}, label = { Text(booking.status.replace("_", " ")) })
                        }
                        if (booking.resultSummary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = booking.resultSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (booking.abnormalFlag) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
