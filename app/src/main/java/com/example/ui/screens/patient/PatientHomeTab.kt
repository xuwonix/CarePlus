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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HealthServiceCategory
import com.example.data.model.MedicalSpecialtyItem
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
        // 1. Greeting & Universal Search Header
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

                Text(
                    text = "Hello, $firstName",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Find doctors, lab tests, nearby hospitals & care",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Universal Search Field
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search doctors, tests, hospitals & services...",
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
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
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

        // Active Emergency Tracker Banner if an ambulance is dispatched
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

        // 2. If User is Searching, show live filtered results across Doctors, Tests, Facilities, Services
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
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
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matches found for \"$query\"",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try searching Cardiology, MRI, Blood Panel, Metro General, or Endoscopy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                if (matchedDoctors.isNotEmpty()) {
                    item {
                        SectionTitle("Doctors (${matchedDoctors.size})")
                    }
                    items(matchedDoctors) { doctor ->
                        SearchDoctorResultCard(
                            specialist = doctor,
                            onBook = { onBookSpecialist(doctor) },
                            onChat = { onOpenChatWithDoctor(doctor.name, doctor.id) }
                        )
                    }
                }
                if (matchedTests.isNotEmpty()) {
                    item {
                        SectionTitle("Diagnostic Tests (${matchedTests.size})")
                    }
                    items(matchedTests) { (center, test) ->
                        SearchTestResultCard(
                            center = center,
                            test = test,
                            onBook = { onBookDiagnostic(center, test) }
                        )
                    }
                }
                if (matchedFacilities.isNotEmpty()) {
                    item {
                        SectionTitle("Hospitals & Diagnostic Centres (${matchedFacilities.size})")
                    }
                    items(matchedFacilities) { facility ->
                        NearbyFacilityCard(
                            facility = facility,
                            onAction = {
                                if (facility.facilityType == "DIAGNOSTIC_CENTER") {
                                    onNavigateToBooking(1, "All")
                                } else {
                                    onNavigateToBooking(0, "All")
                                }
                            }
                        )
                    }
                }
                if (matchedGuides.isNotEmpty()) {
                    item {
                        SectionTitle("Procedure Guides (${matchedGuides.size})")
                    }
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
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
                                handleCategoryNavigation(
                                    category = cat,
                                    onNavigateTab = onNavigateTab,
                                    onNavigateToBooking = onNavigateToBooking,
                                    onQuickEmergencySos = onQuickEmergencySos
                                )
                            }
                        )
                    }
                }
            }
        } else {
            // 3. Upcoming Appointments Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeaderRow(
                        title = "Upcoming Appointments",
                        actionLabel = if (upcomingAppointments.isNotEmpty()) "View All" else "Book Now",
                        onAction = { onNavigateToBooking(if (upcomingAppointments.isNotEmpty()) 2 else 0, "All") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upcomingAppointments.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
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
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "No upcoming visits scheduled",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Book an in-clinic or video visit with a local specialist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Book appointment",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
                                    onChatClick = {
                                        onOpenChatWithDoctor(appt.specialistName, appt.specialistId)
                                    },
                                    onCardClick = { onNavigateToBooking(2, "All") }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Health Service Categories Grid
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle("Health Service Categories")
                    Spacer(modifier = Modifier.height(10.dp))
                    HealthServiceGrid(
                        categories = HealthServiceCategory.entries,
                        onCategoryClick = { cat ->
                            handleCategoryNavigation(
                                category = cat,
                                onNavigateTab = onNavigateTab,
                                onNavigateToBooking = onNavigateToBooking,
                                onQuickEmergencySos = onQuickEmergencySos
                            )
                        }
                    )
                }
            }

            // 5. Popular Medical Specialties Carousel
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
                                onClick = {
                                    onNavigateToBooking(0, specialty.name)
                                }
                            )
                        }
                    }
                }
            }

            // 6. Nearby Hospitals and Diagnostic Centres
            item {
                SectionHeaderRow(
                    title = "Nearby Hospitals & Diagnostic Centres",
                    actionLabel = "Book Test",
                    onAction = { onNavigateToBooking(1, "All") }
                )
            }

            items(state.nearbyFacilities) { facility ->
                NearbyFacilityCard(
                    facility = facility,
                    onAction = {
                        if (facility.facilityType == "DIAGNOSTIC_CENTER") {
                            onNavigateToBooking(1, "All")
                        } else {
                            onNavigateToBooking(0, "All")
                        }
                    }
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
        HealthServiceCategory.EMERGENCY_EMS -> onQuickEmergencySos()
        HealthServiceCategory.HEALTH_INSURANCE -> onNavigateTab(MainNavTab.PROFILE)
        HealthServiceCategory.PROCEDURE_GUIDES -> onNavigateTab(MainNavTab.RECORDS)
        HealthServiceCategory.HEALTH_LOCKER -> onNavigateTab(MainNavTab.RECORDS)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAction) {
            Text(
                text = actionLabel,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActiveAmbulanceMiniCard(
    vehicleNumber: String,
    status: String,
    etaMinutes: Long,
    onViewDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewDetails() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ambulance $vehicleNumber ($status)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Live ETA: $etaMinutes mins • Tap to track",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Track ambulance",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun UpcomingAppointmentCard(
    appointment: Appointment,
    onChatClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .width(290.dp)
            .clickable { onCardClick() }
            .testTag("upcoming_appointment_card_${appointment.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${appointment.date} • ${appointment.timeSlot}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message Doctor",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = appointment.specialistName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${appointment.specialty} • ${appointment.mode.replace("_", " ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            if (appointment.clinicHospital.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.clinicHospital,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HealthServiceGrid(
    categories: List<HealthServiceCategory>,
    onCategoryClick: (HealthServiceCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { category ->
                    val (icon, tint, bg) = when (category) {
                        HealthServiceCategory.SPECIALIST_CONSULT -> Triple(
                            Icons.Default.MedicalServices,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        )
                        HealthServiceCategory.LAB_DIAGNOSTICS -> Triple(
                            Icons.Default.Biotech,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                        HealthServiceCategory.EMERGENCY_EMS -> Triple(
                            Icons.Default.Emergency,
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        )
                        HealthServiceCategory.HEALTH_INSURANCE -> Triple(
                            Icons.Default.VerifiedUser,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        )
                        HealthServiceCategory.PROCEDURE_GUIDES -> Triple(
                            Icons.Default.Healing,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                        HealthServiceCategory.HEALTH_LOCKER -> Triple(
                            Icons.Default.FolderShared,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCategoryClick(category) }
                            .testTag("service_category_${category.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = category.title,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = category.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialtyTileCard(
    specialty: MedicalSpecialtyItem,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (specialty.name) {
        "Cardiology" -> Icons.Default.Favorite
        "Neurology" -> Icons.Default.Psychology
        "Orthopedics" -> Icons.Default.Healing
        "Pediatrics" -> Icons.Default.ChildCare
        "Dermatology" -> Icons.Default.Face
        else -> Icons.Default.LocalHospital
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .width(142.dp)
            .clickable { onClick() }
            .testTag("specialty_card_${specialty.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = specialty.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = specialty.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = specialty.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${specialty.doctorCount} Doctors",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun NearbyFacilityCard(
    facility: NearbyFacility,
    onAction: () -> Unit
) {
    val isHospital = facility.facilityType == "HOSPITAL"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onAction() }
            .testTag("nearby_facility_${facility.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isHospital) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isHospital) Icons.Default.LocalHospital else Icons.Default.Biotech,
                            contentDescription = null,
                            tint = if (isHospital) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = facility.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${facility.distanceKm} km • ${facility.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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
                        text = "${facility.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isHospital) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (facility.isOpen24x7) "24/7 Emergency ER" else if (facility.providesHomeCollection) "Home Sample Pickup" else "Open Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isHospital) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "~${facility.waitTimeMinutes} min wait",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Services: ${facility.departments.joinToString(" • ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isHospital) "Browse Doctors at Hospital" else "Book Diagnostic Test",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SearchDoctorResultCard(
    specialist: Specialist,
    onBook: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(specialist.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${specialist.specialty} • ${specialist.clinicHospital}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Next: ${specialist.nextAvailableSlot} • $${specialist.fee.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Row {
                IconButton(onClick = onChat) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onBook) {
                    Text("Book")
                }
            }
        }
    }
}

@Composable
private fun SearchTestResultCard(
    center: DiagnosticCenter,
    test: DiagnosticTest,
    onBook: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(test.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${center.name} • ${test.turnaroundTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$${test.price.toInt()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = onBook) {
                Text("Book Test")
            }
        }
    }
}
