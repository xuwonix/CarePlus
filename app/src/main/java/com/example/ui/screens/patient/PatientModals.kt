package com.example.ui.screens.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.FamilyMember
import com.example.data.model.HomeCareService
import com.example.data.model.Specialist

@Composable
fun AmbulanceDispatchModal(
    defaultAddress: String = "",
    defaultPhone: String = "",
    onDismiss: () -> Unit,
    onConfirm: (pickup: String, emergencyType: String, ambulanceType: String, phone: String) -> Unit
) {
    var pickupAddress by remember { mutableStateOf(defaultAddress) }
    var patientPhone by remember { mutableStateOf(defaultPhone) }
    var selectedEmergencyType by remember { mutableStateOf("CARDIAC_OR_RESPIRATORY") }
    var selectedVehicleType by remember { mutableStateOf("ADVANCED_CARDIAC") }

    val emergencyTypes = listOf(
        "CARDIAC_OR_RESPIRATORY" to "Chest Pain / Heart / Breathing Trouble",
        "TRAUMA" to "Accident / Injury / Bone Fracture",
        "STROKE" to "Stroke / Sudden Weakness / Slurred Speech",
        "MATERNITY" to "Pregnancy & Maternity Emergency",
        "GENERAL" to "High Fever / General Medical Emergency"
    )

    val vehicleOptions = listOf(
        Triple(
            "ADVANCED_CARDIAC",
            "ICU / Cardiac Ambulance (ACLS)",
            "Mobile ICU with ventilator, heart monitor, defibrillator & paramedic — best for chest pain, heart attack, stroke, or unconscious patients."
        ),
        Triple(
            "BASIC_LIFE_SUPPORT",
            "Basic Medical Ambulance (BLS)",
            "Standard ambulance with oxygen cylinder, stretcher, first-aid & EMT — best for fractures, high fever, maternity, or safe hospital transfer."
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Request Emergency Ambulance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tezpur 108 & partner hospital ambulances dispatch immediately with ER pre-alert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Exact Pickup Location / Landmark") },
                    placeholder = { Text("e.g. Near Mission Chariali, Tezpur") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ambulance_pickup_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = patientPhone,
                    onValueChange = { patientPhone = it },
                    label = { Text("Contact Phone for Paramedic") },
                    placeholder = { Text("+91 98640 ...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ambulance_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Nature of Medical Emergency:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                emergencyTypes.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEmergencyType = key }
                    ) {
                        RadioButton(
                            selected = selectedEmergencyType == key,
                            onClick = { selectedEmergencyType = key }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Select Ambulance Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                vehicleOptions.forEach { (key, title, description) ->
                    val isSelected = selectedVehicleType == key
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVehicleType = key }
                            .testTag("ambulance_type_${key.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedVehicleType = key }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(pickupAddress, selectedEmergencyType, selectedVehicleType, patientPhone)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_dispatch_button")
            ) {
                Text("Confirm Immediate Dispatch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookSpecialistModal(
    specialist: Specialist,
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (
        date: String,
        timeSlot: String,
        mode: String,
        notes: String,
        familyMemberName: String,
        paymentMethod: String
    ) -> Unit
) {
    val memberOptions = remember(familyMembers) {
        listOf("Self") + familyMembers.map { "${it.name} (${it.relationship})" }
    }
    var selectedMember by remember { mutableStateOf("Self") }
    var selectedDay by remember { mutableStateOf(specialist.availableDays.firstOrNull() ?: "Tomorrow") }
    var selectedSlot by remember { mutableStateOf(specialist.timeSlots.firstOrNull() ?: "10:30 AM") }
    var selectedMode by remember { mutableStateOf("IN_CLINIC") }
    var notesText by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("UPI (GPay / PhonePe / Paytm)") }

    val modes = listOf("IN_CLINIC" to "In-Clinic", "VIDEO_CALL" to "Video Call", "CHAT" to "Chat")
    val paymentMethods = listOf(
        "UPI (GPay / PhonePe / Paytm)",
        "Debit / Credit / RuPay Card",
        "Insurance Cashless",
        "Pay at Clinic"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Schedule Consultation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${specialist.name} • ₹${specialist.fee.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Booking For:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memberOptions) { member ->
                        FilterChip(
                            selected = selectedMember == member,
                            onClick = { selectedMember = member },
                            label = { Text(member, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Consultation Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modes.forEach { (modeKey, modeTitle) ->
                        FilterChip(
                            selected = selectedMode == modeKey,
                            onClick = { selectedMode = modeKey },
                            label = { Text(modeTitle, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("mode_${modeKey.lowercase()}")
                        )
                    }
                }

                Text("Select Date:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(specialist.availableDays) { day ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { Text(day, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Select Time Slot:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(specialist.timeSlots) { slot ->
                        FilterChip(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                            label = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Symptoms / Reason for Visit") },
                    placeholder = { Text("e.g. Palpitations or follow-up checkup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("appointment_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Payment Checkout (₹${specialist.fee.toInt()}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                paymentMethods.forEach { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPayment == method,
                            onClick = { selectedPayment = method }
                        )
                        Text(method, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedDay, selectedSlot, selectedMode, notesText, selectedMember, selectedPayment)
                },
                modifier = Modifier.testTag("confirm_booking_button")
            ) {
                Text("Pay & Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BookDiagnosticModal(
    center: DiagnosticCenter,
    test: DiagnosticTest,
    familyMembers: List<FamilyMember>,
    defaultAddress: String,
    onDismiss: () -> Unit,
    onConfirm: (
        date: String,
        timeWindow: String,
        collectionType: String,
        address: String,
        familyMemberName: String,
        paymentMethod: String
    ) -> Unit
) {
    val memberOptions = remember(familyMembers) {
        listOf("Self") + familyMembers.map { "${it.name} (${it.relationship})" }
    }
    val dateOptions = listOf("Tomorrow", "Fri Nov 20", "Sat Nov 21")
    val timeWindows = listOf("07:00 AM - 08:30 AM", "09:00 AM - 10:30 AM", "04:00 PM - 05:30 PM")
    val paymentMethods = listOf(
        "UPI (GPay / PhonePe / Paytm)",
        "Debit / Credit / RuPay Card",
        "Pay on Doorstep / Lab"
    )

    var selectedMember by remember { mutableStateOf("Self") }
    var selectedDate by remember { mutableStateOf(dateOptions.first()) }
    var selectedTime by remember { mutableStateOf(timeWindows.first()) }
    var collectionType by remember { mutableStateOf(if (center.providesHomeCollection) "HOME_COLLECTION" else "LAB_VISIT") }
    var address by remember { mutableStateOf(defaultAddress) }
    var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Book Diagnostic Test", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "${test.name} • ₹${test.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Prep: ${test.preparationSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text("Patient:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memberOptions) { member ->
                        FilterChip(
                            selected = selectedMember == member,
                            onClick = { selectedMember = member },
                            label = { Text(member, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Collection Method:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                if (center.providesHomeCollection) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = collectionType == "HOME_COLLECTION",
                            onClick = { collectionType = "HOME_COLLECTION" }
                        )
                        Text("Home Sample Collection (Free Phlebotomist)", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = collectionType == "LAB_VISIT",
                        onClick = { collectionType = "LAB_VISIT" }
                    )
                    Text("Visit ${center.name}", style = MaterialTheme.typography.bodySmall)
                }

                if (collectionType == "HOME_COLLECTION") {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Home Collection Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("Preferred Date:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dateOptions) { d ->
                        FilterChip(selected = selectedDate == d, onClick = { selectedDate = d }, label = { Text(d) })
                    }
                }

                Text("Arrival / Lab Window:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timeWindows) { w ->
                        FilterChip(selected = selectedTime == w, onClick = { selectedTime = w }, label = { Text(w) })
                    }
                }

                Text("Payment Method:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                paymentMethods.forEach { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == method, onClick = { selectedPayment = method })
                        Text(method, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAddress = if (collectionType == "HOME_COLLECTION") address else center.address
                    onConfirm(selectedDate, selectedTime, collectionType, finalAddress, selectedMember, selectedPayment)
                },
                modifier = Modifier.testTag("confirm_diagnostic_button")
            ) {
                Text("Confirm (₹${test.price.toInt()})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BookHomeCareModal(
    service: HomeCareService,
    familyMembers: List<FamilyMember>,
    defaultAddress: String,
    onDismiss: () -> Unit,
    onConfirm: (
        memberName: String,
        visitAddress: String,
        scheduledDate: String,
        timeWindow: String,
        notes: String,
        paymentMethod: String
    ) -> Unit
) {
    val memberOptions = remember(familyMembers) {
        listOf("Self") + familyMembers.map { "${it.name} (${it.relationship})" }
    }
    val dateOptions = listOf("Today", "Tomorrow", "Fri Nov 20", "Sat Nov 21")
    val windows = listOf("09:00 AM - 11:00 AM", "12:00 PM - 02:00 PM", "04:00 PM - 06:00 PM")
    val paymentMethods = listOf(
        "UPI (GPay / PhonePe / Paytm)",
        "Debit / Credit / RuPay Card",
        "Pay at Doorstep"
    )

    var selectedMember by remember { mutableStateOf("Self") }
    var visitAddress by remember { mutableStateOf(defaultAddress) }
    var selectedDate by remember { mutableStateOf(dateOptions[1]) }
    var selectedWindow by remember { mutableStateOf(windows[0]) }
    var notes by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Book Home Healthcare", fontWeight = FontWeight.Bold)
                Text("${service.title} • ₹${service.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${service.providerTeam} • ${service.duration}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                Text("Care For:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memberOptions) { m ->
                        FilterChip(selected = selectedMember == m, onClick = { selectedMember = m }, label = { Text(m) })
                    }
                }

                OutlinedTextField(
                    value = visitAddress,
                    onValueChange = { visitAddress = it },
                    label = { Text("Home Visit Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Visit Date:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dateOptions) { d ->
                        FilterChip(selected = selectedDate == d, onClick = { selectedDate = d }, label = { Text(d) })
                    }
                }

                Text("Arrival Window:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(windows) { w ->
                        FilterChip(selected = selectedWindow == w, onClick = { selectedWindow = w }, label = { Text(w) })
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Care Instructions / Mobility Notes") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Payment Method:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                paymentMethods.forEach { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == method, onClick = { selectedPayment = method })
                        Text(method, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedMember, visitAddress, selectedDate, selectedWindow, notes, selectedPayment)
                },
                modifier = Modifier.testTag("confirm_home_care_button")
            ) {
                Text("Confirm Visit (₹${service.price.toInt()})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RescheduleBookingModal(
    title: String,
    currentSchedule: String,
    onDismiss: () -> Unit,
    onConfirm: (newDate: String, newSlot: String) -> Unit
) {
    val dates = listOf("Tomorrow", "Fri Nov 20", "Sat Nov 21", "Mon Nov 23")
    val slots = listOf("09:00 AM", "10:30 AM", "02:00 PM", "04:30 PM", "06:00 PM")

    var selectedDate by remember { mutableStateOf(dates.first()) }
    var selectedSlot by remember { mutableStateOf(slots[1]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Reschedule Booking", fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current: $currentSchedule", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("New Date:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dates) { d ->
                        FilterChip(selected = selectedDate == d, onClick = { selectedDate = d }, label = { Text(d) })
                    }
                }
                Text("New Time Slot:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(slots) { s ->
                        FilterChip(selected = selectedSlot == s, onClick = { selectedSlot = s }, label = { Text(s) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedSlot) },
                modifier = Modifier.testTag("confirm_reschedule_button")
            ) {
                Text("Save New Slot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
