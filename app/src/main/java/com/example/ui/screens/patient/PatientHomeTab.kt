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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HealthServiceCategory
import com.example.data.model.NearbyFacility
import com.example.data.model.ProcedureGuide
import com.example.data.model.Specialist
import com.example.ui.viewmodel.HealthcareUiState
import com.example.ui.viewmodel.MainNavTab

@Composable
fun PatientHomeTab(
    state: HealthcareUiState,
    onSearchQueryChange: (String) -> Unit,
    onNavigateTab: (MainNavTab) -> Unit,
    onNavigateToBooking: (subTab: Int, specialty: String) -> Unit,
    onOpenDoctorProfile: (Specialist) -> Unit,
    onOpenFacilityDetail: (NearbyFacility) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onBookSpecialist: (Specialist) -> Unit,
    onBookDiagnostic: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onOpenChatWithDoctor: (doctorName: String, doctorId: String) -> Unit,
    onSelectProcedure: (ProcedureGuide) -> Unit,
    onQuickEmergencySos: () -> Unit
) {
    val activeAmbulance = state.patientAmbulanceRequests.firstOrNull { it.status != "COMPLETED" }
    val upcomingAppointments = state.patientAppointments.filter { it.status == "SCHEDULED" }
    val query = state.searchQuery.trim()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_home_tab"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Greeting, Location Pill & Universal Search Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val firstName = state.userProfile?.displayName
                    ?.substringBefore(" ")
                    ?.takeIf { it.isNotBlank() }
                    ?: "there"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, $firstName",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Doctors, labs, home care & hospitals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clickable { onOpenLocationPicker() }
                            .testTag("home_location_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Change Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.selectedLocationArea.substringBefore(" "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search doctors, tests, home care, hospitals...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        if (activeAmbulance != null) {
            item {
                ActiveAmbulanceMiniCard(
                    vehicleNumber = activeAmbulance.vehicleNumber,
                    status = activeAmbulance.status,
                    etaMinutes = activeAmbulance.etaMinutes,
                    onViewDetails = { onNavigateTab(MainNavTab.EMERGENCY) }
                )
            }
        }

        if (query.isNotEmpty()) {
            val matchedDoctors = state.specialists.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.specialty.contains(query, ignoreCase = true) ||
                    it.clinicHospital.contains(query, ignoreCase = true)
            }
            val matchedTests = state.diagnosticCenters.flatMap { center ->
                center.testsOffered
                    .filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true) ||
                            center.name.contains(query, ignoreCase = true)
                    }
                    .map { test -> center to test }
            }
            val matchedFacilities = state.nearbyFacilities.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.departments.any { dept -> dept.contains(query, ignoreCase = true) }
            }
            val matchedGuides = state.procedureGuides.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.summary.contains(query, ignoreCase = true)
            }
            val matchedCategories = HealthServiceCategory.entries.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
            }

            val totalMatches = matchedDoctors.size + matchedTests.size +
                matchedFacilities.size + matchedGuides.size + matchedCategories.size

            item {
                Text(
                    text = "Search Results ($totalMatches)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (totalMatches == 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matches found for \"$query\"", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Try Cardiology, MRI, Nursing, Metro General, or Endoscopy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                if (matchedDoctors.isNotEmpty()) {
                    item { SectionTitle("Doctors (${matchedDoctors.size})") }
                    items(matchedDoctors) { doctor ->
                        SearchDoctorResultCard(
                            specialist = doctor,
                            onOpenProfile = { onOpenDoctorProfile(doctor) },
                            onBook = { onBookSpecialist(doctor) },
                            onChat = { onOpenChatWithDoctor(doctor.name, doctor.id) }
                        )
                    }
                }
                if (matchedTests.isNotEmpty()) {
                    item { SectionTitle("Diagnostic Tests (${matchedTests.size})") }
                    items(matchedTests) { (center, test) ->
                        SearchTestResultCard(center = center, test = test, onBook = { onBookDiagnostic(center, test) })
                    }
                }
                if (matchedFacilities.isNotEmpty()) {
                    item { SectionTitle("Hospitals & Diagnostic Centres (${matchedFacilities.size})") }
                    items(matchedFacilities) { facility ->
                        NearbyFacilityCard(
                            facility = facility,
                            onAction = { onOpenFacilityDetail(facility) }
                        )
                    }
                }
                if (matchedGuides.isNotEmpty()) {
                    item { SectionTitle("Procedure Guides (${matchedGuides.size})") }
                    items(matchedGuides) { guide ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onSelectProcedure(guide) }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(guide.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(guide.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                if (matchedCategories.isNotEmpty()) {
                    item {
                        SectionTitle("Healthcare Services (${matchedCategories.size})")
                        Spacer(modifier = Modifier.height(6.dp))
                        HealthServiceGrid(
                            categories = matchedCategories,
                            onCategoryClick = { cat ->
                                handleCategoryNavigation(cat, onNavigateTab, onNavigateToBooking, onQuickEmergencySos)
                            }
                        )
                    }
                }
            }
        } else {
            // Upcoming Appointments
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeaderRow(
                        title = "Upcoming Appointments",
                        actionLabel = if (upcomingAppointments.isNotEmpty()) "View All" else "Book Now",
                        onAction = { onNavigateToBooking(if (upcomingAppointments.isNotEmpty()) 3 else 0, "All") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upcomingAppointments.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onNavigateToBooking(0, "All") }
                                .testTag("empty_upcoming_appointments_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("No upcoming visits scheduled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Book an in-clinic or video visit with a local specialist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Book appointment", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(upcomingAppointments) { appt ->
                                UpcomingAppointmentCard(
                                    appointment = appt,
                                    onChatClick = { onOpenChatWithDoctor(appt.specialistName, appt.specialistId) },
                                    onCardClick = { onNavigateToBooking(3, "All") }
                                )
                            }
                        }
                    }
                }
            }

            // Health Service Categories
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle("Health Service Categories")
                    Spacer(modifier = Modifier.height(10.dp))
                    HealthServiceGrid(
                        categories = HealthServiceCategory.entries,
                        onCategoryClick = { cat ->
                            handleCategoryNavigation(cat, onNavigateTab, onNavigateToBooking, onQuickEmergencySos)
                        }
                    )
                }
            }

            // Popular Medical Specialties
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeaderRow(
                        title = "Popular Medical Specialties",
                        actionLabel = "See All",
                        onAction = { onNavigateToBooking(0, "All") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.popularSpecialties) { specialty ->
                            SpecialtyTileCard(
                                specialty = specialty,
                                onClick = { onNavigateToBooking(0, specialty.name) }
                            )
                        }
                    }
                }
            }

            // Nearby Hospitals & Diagnostic Centres
            item {
                Column {
                    SectionHeaderRow(
                        title = "Nearby Hospitals & Diagnostic Centres",
                        actionLabel = "Book Test",
                        onAction = { onNavigateToBooking(1, "All") }
                    )
                    Text(
                        text = "Showing care facilities near ${state.selectedLocationArea}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            items(state.nearbyFacilities) { facility ->
                NearbyFacilityCard(
                    facility = facility,
                    onAction = { onOpenFacilityDetail(facility) }
                )
            }
        }
    }
}

private fun handleCategoryNavigation(
    category: HealthServiceCategory,
    onNavigateTab: (MainNavTab) -> Unit,
    onNavigateToBooking: (Int, String) -> Unit,
    onQuickEmergencySos: () -> Unit
) {
    when (category) {
        HealthServiceCategory.SPECIALIST_CONSULT -> onNavigateToBooking(0, "All")
        HealthServiceCategory.LAB_DIAGNOSTICS -> onNavigateToBooking(1, "All")
        HealthServiceCategory.HOME_CARE -> onNavigateToBooking(2, "All")
        HealthServiceCategory.EMERGENCY_EMS -> onQuickEmergencySos()
        HealthServiceCategory.HEALTH_INSURANCE -> onNavigateTab(MainNavTab.PROFILE)
        HealthServiceCategory.PROCEDURE_GUIDES -> onNavigateTab(MainNavTab.RECORDS)
    }
}
