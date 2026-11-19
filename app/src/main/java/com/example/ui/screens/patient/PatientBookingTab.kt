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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
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
import com.example.data.model.HomeCareBooking
import com.example.data.model.HomeCareService
import com.example.data.model.Specialist
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.viewmodel.HealthcareUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientBookingTab(
    state: HealthcareUiState,
    onSelectSubTab: (Int) -> Unit,
    onSelectSpecialty: (String) -> Unit,
    onOpenDoctorProfile: (Specialist) -> Unit,
    onBookDoctor: (Specialist) -> Unit,
    onBookTest: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onBookHomeCare: (HomeCareService) -> Unit,
    onRescheduleAppointment: (Appointment) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onRescheduleDiagnostic: (DiagnosticBooking) -> Unit,
    onCancelDiagnostic: (String) -> Unit,
    onRescheduleHomeCare: (HomeCareBooking) -> Unit,
    onCancelHomeCare: (String) -> Unit,
    onOpenChatWithDoctor: (doctorName: String, doctorId: String) -> Unit,
    onLockedChatAttempt: (Specialist) -> Unit
) {
    val strings = LocalAppStrings.current
    val subTabs = listOf(
        "doctors" to strings.bookingTabDoctors,
        "lab_tests" to strings.bookingTabLabs,
        "home_care" to strings.bookingTabHomeCare,
        "my_bookings" to strings.bookingTabMyBookings
    )
    var localSearch by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_booking_tab")
    ) {
        SecondaryTabRow(
            selectedTabIndex = state.bookingSubTab.coerceIn(0, subTabs.lastIndex),
            modifier = Modifier.fillMaxWidth()
        ) {
            subTabs.forEachIndexed { index, (tagKey, title) ->
                Tab(
                    selected = state.bookingSubTab == index,
                    onClick = { onSelectSubTab(index) },
                    text = {
                        Text(
                            text = title,
                            maxLines = 1,
                            fontWeight = if (state.bookingSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("booking_subtab_$tagKey")
                )
            }
        }

        when (state.bookingSubTab) {
            0 -> DoctorsBookingSection(
                specialists = state.specialists,
                bookedSpecialistIds = state.bookedSpecialistIds,
                selectedSpecialty = state.selectedSpecialtyFilter,
                searchQuery = localSearch,
                onSearchChange = { localSearch = it },
                onSelectSpecialty = onSelectSpecialty,
                onOpenProfile = onOpenDoctorProfile,
                onBook = onBookDoctor,
                onChat = { onOpenChatWithDoctor(it.name, it.id) },
                onLockedChatAttempt = onLockedChatAttempt
            )
            1 -> DiagnosticsBookingSection(
                centers = state.diagnosticCenters,
                searchQuery = localSearch,
                onSearchChange = { localSearch = it },
                onBookTest = onBookTest
            )
            2 -> HomeCareBookingSection(
                services = state.homeCareServices,
                onBookHomeCare = onBookHomeCare
            )
            else -> MyBookingsSection(
                appointments = state.patientAppointments,
                diagnosticBookings = state.patientDiagnosticBookings,
                homeCareBookings = state.homeCareBookings,
                onRescheduleAppointment = onRescheduleAppointment,
                onCancelAppointment = onCancelAppointment,
                onRescheduleDiagnostic = onRescheduleDiagnostic,
                onCancelDiagnostic = onCancelDiagnostic,
                onRescheduleHomeCare = onRescheduleHomeCare,
                onCancelHomeCare = onCancelHomeCare,
                onOpenChatWithDoctor = onOpenChatWithDoctor,
                onGoToDoctors = { onSelectSubTab(0) }
            )
        }
    }
}

@Composable
private fun DoctorsBookingSection(
    specialists: List<Specialist>,
    bookedSpecialistIds: Set<String>,
    selectedSpecialty: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectSpecialty: (String) -> Unit,
    onOpenProfile: (Specialist) -> Unit,
    onBook: (Specialist) -> Unit,
    onChat: (Specialist) -> Unit,
    onLockedChatAttempt: (Specialist) -> Unit
) {
    val strings = LocalAppStrings.current
    val specialties = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Gynecology", "Dermatology", "General Medicine")
    var videoOnly by remember { mutableStateOf(false) }
    var sortByFee by remember { mutableStateOf(false) }

    val filtered = remember(specialists, selectedSpecialty, searchQuery, videoOnly, sortByFee) {
        val list = specialists.filter { doc ->
            val matchesSpec = selectedSpecialty == "All" || doc.specialty.equals(selectedSpecialty, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                doc.name.contains(searchQuery, ignoreCase = true) ||
                doc.specialty.contains(searchQuery, ignoreCase = true) ||
                doc.clinicHospital.contains(searchQuery, ignoreCase = true)
            val matchesVideo = !videoOnly || doc.offersVideoConsult
            matchesSpec && matchesQuery && matchesVideo
        }
        if (sortByFee) list.sortedBy { it.fee } else list
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
                placeholder = { Text(strings.filterDoctorsPlaceholder) },
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

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = videoOnly,
                    onClick = { videoOnly = !videoOnly },
                    label = { Text(strings.videoConsult) },
                    leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = sortByFee,
                    onClick = { sortByFee = !sortByFee },
                    label = { Text(strings.feeLowToHigh) }
                )
            }
        }

        items(filtered) { specialist ->
            val isBooked = specialist.id in bookedSpecialistIds
            BookingSpecialistCard(
                specialist = specialist,
                isBooked = isBooked,
                onOpenProfile = { onOpenProfile(specialist) },
                onBook = { onBook(specialist) },
                onChat = {
                    if (isBooked) onChat(specialist) else onLockedChatAttempt(specialist)
                }
            )
        }
    }
}

@Composable
private fun BookingSpecialistCard(
    specialist: Specialist,
    isBooked: Boolean,
    onOpenProfile: () -> Unit,
    onBook: () -> Unit,
    onChat: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProfile() }
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
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(specialist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${specialist.specialty} • ${specialist.experienceYears}y exp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${specialist.rating}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(specialist.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("${specialist.clinicHospital} • ${specialist.address}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Next: ${specialist.nextAvailableSlot}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("₹${specialist.fee.toInt()} / visit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Icon(
                        imageVector = if (isBooked) Icons.Default.Chat else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isBooked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBooked) strings.chatAction else strings.bookedOnly,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBooked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onBook,
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("book_doctor_button_${specialist.id}")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.bookSlot)
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
    val strings = LocalAppStrings.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(strings.filterLabsPlaceholder) },
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
                                Text(center.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${center.address} • ${center.distanceKm} km • ${center.accreditation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (center.providesHomeCollection) {
                                SuggestionChip(onClick = {}, label = { Text("Home Pickup") })
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        matchingTests.forEach { test ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
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
                                        Text(test.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${test.sampleType} • ${test.turnaroundTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Prep: ${test.preparationSummary}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${test.price.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
