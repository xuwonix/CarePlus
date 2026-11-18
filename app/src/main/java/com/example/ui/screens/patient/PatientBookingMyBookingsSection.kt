package com.example.ui.screens.patient

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticBooking
import com.example.data.model.HomeCareBooking
import com.example.data.model.HomeCareService

@Composable
fun HomeCareBookingSection(
    services: List<HomeCareService>,
    onBookHomeCare: (HomeCareService) -> Unit
) {
    val categories = listOf("All", "NURSING", "PHYSIOTHERAPY", "ELDERLY_CARE", "IV_THERAPY", "DOCTOR_VISIT")
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = remember(services, selectedCategory) {
        if (selectedCategory == "All") services
        else services.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_care_booking_section"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Home Healthcare Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Licensed nurses, physiotherapists, IV infusions, and physician house calls delivered right to your home.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(if (cat == "All") "All" else cat.replace("_", " ")) }
                    )
                }
            }
        }

        items(filtered) { service ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_care_card_${service.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(onClick = {}, label = { Text(service.category.replace("_", " ")) })
                        Text(
                            text = "₹${service.price.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${service.providerTeam} • ${service.duration}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onBookHomeCare(service) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_home_care_${service.id}")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Schedule Home Visit")
                    }
                }
            }
        }
    }
}

@Composable
fun MyBookingsSection(
    appointments: List<Appointment>,
    diagnosticBookings: List<DiagnosticBooking>,
    homeCareBookings: List<HomeCareBooking>,
    onRescheduleAppointment: (Appointment) -> Unit,
    onCancelAppointment: (String) -> Unit,
    onRescheduleDiagnostic: (DiagnosticBooking) -> Unit,
    onCancelDiagnostic: (String) -> Unit,
    onRescheduleHomeCare: (HomeCareBooking) -> Unit,
    onCancelHomeCare: (String) -> Unit,
    onOpenChatWithDoctor: (String, String) -> Unit,
    onGoToDoctors: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_bookings_section"),
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
                        Button(onClick = onGoToDoctors) { Text("Browse Specialists") }
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
                                Text("Patient: ${appt.familyMemberName} • ${appt.paymentStatus}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            SuggestionChip(onClick = {}, label = { Text(appt.status) })
                        }
                        if (appt.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Notes: ${appt.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (appt.status == "SCHEDULED") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenChatWithDoctor(appt.specialistName, appt.specialistId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { onRescheduleAppointment(appt) },
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reschedule", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { onCancelAppointment(appt.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Diagnostic Lab & Home Collection (${diagnosticBookings.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (diagnosticBookings.isEmpty()) {
            item {
                Text("No diagnostic tests scheduled yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("${booking.centerName} • ${booking.scheduledDate} (${booking.scheduledTime})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Patient: ${booking.familyMemberName} • ${booking.collectionType.replace("_", " ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            SuggestionChip(onClick = {}, label = { Text(booking.status.replace("_", " ")) })
                        }
                        if (booking.collectionAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Location: ${booking.collectionAddress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (booking.status == "BOOKED") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onRescheduleDiagnostic(booking) }, modifier = Modifier.weight(1f)) {
                                    Text("Reschedule")
                                }
                                OutlinedButton(onClick = { onCancelDiagnostic(booking.id) }, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Home Healthcare Visits (${homeCareBookings.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (homeCareBookings.isEmpty()) {
            item {
                Text("No home healthcare visits scheduled yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(homeCareBookings) { booking ->
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
                                Text(booking.serviceTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${booking.providerTeam} • ${booking.scheduledDate} (${booking.timeWindow})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("For: ${booking.memberName} • ${booking.visitAddress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            SuggestionChip(onClick = {}, label = { Text(booking.status) })
                        }
                        if (booking.status == "SCHEDULED") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onRescheduleHomeCare(booking) }, modifier = Modifier.weight(1f)) {
                                    Text("Reschedule")
                                }
                                OutlinedButton(onClick = { onCancelHomeCare(booking.id) }, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
