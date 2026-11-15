package com.example.ui.screens.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.Specialist

@Composable
fun AmbulanceDispatchModal(
    onDismiss: () -> Unit,
    onConfirm: (pickup: String, emergencyType: String, ambulanceType: String, phone: String) -> Unit
) {
    var pickupAddress by remember { mutableStateOf("420 Maple Avenue, Apt 3B") }
    var patientPhone by remember { mutableStateOf("+1 (555) 911-3829") }
    var selectedEmergencyType by remember { mutableStateOf("CARDIAC_OR_RESPIRATORY") }
    var selectedVehicleType by remember { mutableStateOf("ADVANCED_CARDIAC") }

    val emergencyTypes = listOf(
        "CARDIAC_OR_RESPIRATORY" to "Cardiac / Chest Pain / Shortness of Breath",
        "TRAUMA" to "Trauma / Accident / Fracture",
        "STROKE" to "Stroke / Sudden Weakness / Slurred Speech",
        "MATERNITY" to "Labor & Maternity Emergency",
        "GENERAL" to "General Acute Emergency"
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
                    text = "Paramedics dispatch immediately upon confirmation with hospital triage notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Exact Pickup Location / Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ambulance_pickup_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = patientPhone,
                    onValueChange = { patientPhone = it },
                    label = { Text("Contact Phone for Paramedic") },
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedEmergencyType == key,
                            onClick = { selectedEmergencyType = key }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Ambulance Vehicle Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedVehicleType == "ADVANCED_CARDIAC",
                        onClick = { selectedVehicleType = "ADVANCED_CARDIAC" },
                        label = { Text("ACLS Unit") }
                    )
                    FilterChip(
                        selected = selectedVehicleType == "BASIC_LIFE_SUPPORT",
                        onClick = { selectedVehicleType = "BASIC_LIFE_SUPPORT" },
                        label = { Text("BLS Unit") }
                    )
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
    onDismiss: () -> Unit,
    onConfirm: (date: String, timeSlot: String, mode: String, notes: String) -> Unit
) {
    var selectedDate by remember { mutableStateOf("Tomorrow (10:30 AM)") }
    var selectedSlot by remember { mutableStateOf("10:30 AM") }
    var selectedMode by remember { mutableStateOf("IN_CLINIC") }
    var notesText by remember { mutableStateOf("") }

    val slots = specialist.timeSlots
    val modes = listOf("IN_CLINIC" to "In-Clinic Visit", "VIDEO_CALL" to "Video Consultation", "CHAT" to "Clinical Chat")

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
                    text = "${specialist.name} • ${specialist.specialty}",
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
                Text(
                    text = "Clinic: ${specialist.clinicHospital} ($${specialist.fee.toInt()} fee)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Select Consultation Mode:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

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

                Text(
                    text = "Select Available Time Slot:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slots.take(3).forEach { slot ->
                        FilterChip(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                            label = { Text(slot) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Symptoms / Reason for Visit") },
                    placeholder = { Text("e.g. Experiencing persistent palpitations after workouts.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("appointment_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm("Tomorrow", selectedSlot, selectedMode, notesText)
                },
                modifier = Modifier.testTag("confirm_booking_button")
            ) {
                Text("Confirm Appointment")
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
fun BookDiagnosticModal(
    center: DiagnosticCenter,
    test: DiagnosticTest,
    onDismiss: () -> Unit,
    onConfirm: (date: String, collectionType: String) -> Unit
) {
    var collectionType by remember { mutableStateOf(if (center.providesHomeCollection) "HOME_COLLECTION" else "LAB_VISIT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Book Diagnostic Test", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = test.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Facility: ${center.name} • Price: $${test.price.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Preparation: ${test.preparationSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "Sample Collection Method:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (center.providesHomeCollection) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = collectionType == "HOME_COLLECTION",
                            onClick = { collectionType = "HOME_COLLECTION" }
                        )
                        Text("Phlebotomist Home Sample Collection")
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = collectionType == "LAB_VISIT",
                        onClick = { collectionType = "LAB_VISIT" }
                    )
                    Text("Visit Diagnostic Lab Center")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm("Tomorrow at 08:30 AM", collectionType) },
                modifier = Modifier.testTag("confirm_diagnostic_button")
            ) {
                Text("Confirm Test Slot")
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
fun SubmitInsuranceClaimModal(
    onDismiss: () -> Unit,
    onConfirm: (policy: String, provider: String, hospital: String, procedure: String, amount: Double, claimType: String, docRef: String) -> Unit
) {
    var policyNumber by remember { mutableStateOf("BCS-9923842") }
    var hospitalName by remember { mutableStateOf("Metro General Hospital") }
    var procedureName by remember { mutableStateOf("Upper GI Endoscopy & Biopsy") }
    var amountText by remember { mutableStateOf("1250.0") }
    var claimType by remember { mutableStateOf("CASHLESS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Cashless Claim / Pre-Auth") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = policyNumber,
                    onValueChange = { policyNumber = it },
                    label = { Text("Policy ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital / Center Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = procedureName,
                    onValueChange = { procedureName = it },
                    label = { Text("Medical Procedure") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Estimated / Billed Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 1000.0
                    onConfirm(policyNumber, "BlueCross Shield", hospitalName, procedureName, amount, claimType, "DOC-REF-${(100..999).random()}")
                },
                modifier = Modifier.testTag("submit_claim_confirm_button")
            ) {
                Text("Submit for Pre-Auth")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddMedicalRecordModal(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, doctor: String, date: String, summary: String, metrics: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PRESCRIPTION") }
    var doctor by remember { mutableStateOf("Dr. Elena Rostova") }
    var date by remember { mutableStateOf("Nov 12, 2026") }
    var summary by remember { mutableStateOf("") }
    var metrics by remember { mutableStateOf("BP: 120/80 mmHg | Heart Rate: 72 bpm | Glucose: 95 mg/dL") }

    val categories = listOf("PRESCRIPTION", "LAB_REPORT", "RADIOLOGY", "DISCHARGE_SUMMARY", "VITALS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record to Locker") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title (e.g. Cardiology Rx, Lipid Panel)") },
                    modifier = Modifier.fillMaxWidth().testTag("record_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor or Facility Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Clinical Notes / Instructions") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = metrics,
                    onValueChange = { metrics = it },
                    label = { Text("Vital Signs & Key Values") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, category, doctor, date, summary, metrics)
                    }
                },
                modifier = Modifier.testTag("save_record_button")
            ) {
                Text("Save to Locker")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
