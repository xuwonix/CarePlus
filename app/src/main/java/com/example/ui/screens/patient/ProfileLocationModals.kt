package com.example.ui.screens.patient

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.FamilyMember
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareCatalog
import java.util.Locale

@Composable
fun SubmitInsuranceClaimModal(
    onDismiss: () -> Unit,
    onConfirm: (policy: String, provider: String, hospital: String, procedure: String, amount: Double, claimType: String, docRef: String) -> Unit
) {
    var policyNumber by remember { mutableStateOf("BCS-9923842") }
    var hospitalName by remember { mutableStateOf("Metro General Trauma & Cardiac Center") }
    var procedureName by remember { mutableStateOf("Upper GI Endoscopy & Biopsy") }
    var amountText by remember { mutableStateOf("1250.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Cashless Claim / Pre-Auth", fontWeight = FontWeight.Bold) },
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
                    onConfirm(policyNumber, "BlueCross Shield", hospitalName, procedureName, amount, "CASHLESS", "DOC-REF-${(100..999).random()}")
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
    familyMembers: List<FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, doctor: String, date: String, summary: String, metrics: String, memberName: String) -> Unit
) {
    val memberOptions = remember(familyMembers) {
        listOf("Self") + familyMembers.map { "${it.name} (${it.relationship})" }
    }
    var selectedMember by remember { mutableStateOf("Self") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PRESCRIPTION") }
    var doctor by remember { mutableStateOf("Dr. Elena Rostova, MD") }
    var date by remember { mutableStateOf("Nov 18, 2026") }
    var summary by remember { mutableStateOf("") }
    var metrics by remember { mutableStateOf("BP: 120/80 mmHg | Heart Rate: 72 bpm | Glucose: 95 mg/dL") }

    val categories = listOf("PRESCRIPTION", "LAB_REPORT", "RADIOLOGY", "VITALS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record to Locker", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Record For:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memberOptions) { member ->
                        FilterChip(
                            selected = selectedMember == member,
                            onClick = { selectedMember = member },
                            label = { Text(member, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title (e.g. Cardiology Rx, Lipid Panel)") },
                    modifier = Modifier.fillMaxWidth().testTag("record_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
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
                    val safeTitle = title.ifBlank { "Clinical Summary Note" }
                    onConfirm(safeTitle, category, doctor, date, summary, metrics, selectedMember)
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

@Composable
fun EditPatientProfileModal(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var age by remember { mutableStateOf(currentProfile.age) }
    var gender by remember { mutableStateOf(currentProfile.gender) }
    var bloodGroup by remember { mutableStateOf(currentProfile.bloodGroup) }
    var allergies by remember { mutableStateOf(currentProfile.allergies) }
    var chronicConditions by remember { mutableStateOf(currentProfile.chronicConditions) }
    var emergencyContactName by remember { mutableStateOf(currentProfile.emergencyContactName) }
    var emergencyContactPhone by remember { mutableStateOf(currentProfile.emergencyContactPhone) }
    var savedAddress by remember { mutableStateOf(currentProfile.savedAddress) }

    val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Patient Health Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Text("Blood Group:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(bloodGroups) { bg ->
                        FilterChip(selected = bloodGroup == bg, onClick = { bloodGroup = bg }, label = { Text(bg) })
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = { Text("Chronic Conditions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text("Emergency Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = { Text("Emergency Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = savedAddress,
                    onValueChange = { savedAddress = it },
                    label = { Text("Home Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        currentProfile.copy(
                            displayName = displayName.trim(),
                            phone = phone.trim(),
                            age = age.trim(),
                            gender = gender.trim(),
                            bloodGroup = bloodGroup,
                            allergies = allergies.trim(),
                            chronicConditions = chronicConditions.trim(),
                            emergencyContactName = emergencyContactName.trim(),
                            emergencyContactPhone = emergencyContactPhone.trim(),
                            savedAddress = savedAddress.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_patient_profile_button")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddFamilyMemberModal(
    onDismiss: () -> Unit,
    onConfirm: (FamilyMember) -> Unit
) {
    val relationships = listOf("Spouse", "Child", "Parent", "Sibling")
    val bloodGroups = listOf("O+", "A+", "B+", "AB+", "O-", "A-")

    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Spouse") }
    var age by remember { mutableStateOf("29") }
    var gender by remember { mutableStateOf("Female") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var allergies by remember { mutableStateOf("None") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("family_member_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Relationship:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(relationships) { rel ->
                        FilterChip(selected = relationship == rel, onClick = { relationship = rel }, label = { Text(rel) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text("Blood Group:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(bloodGroups) { bg ->
                        FilterChip(selected = bloodGroup == bg, onClick = { bloodGroup = bg }, label = { Text(bg) })
                    }
                }

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies (if any)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val safeName = name.trim().ifBlank { "Family Member" }
                    onConfirm(
                        FamilyMember(
                            name = safeName,
                            relationship = relationship,
                            age = age.trim(),
                            gender = gender.trim(),
                            bloodGroup = bloodGroup,
                            allergies = allergies.trim().ifBlank { "None" },
                            phone = phone.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_family_member_button")
            ) {
                Text("Add Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun LocationPickerModal(
    currentArea: String,
    currentAddress: String,
    onDismiss: () -> Unit,
    onConfirm: (area: String, address: String) -> Unit
) {
    val context = LocalContext.current
    var selectedArea by remember { mutableStateOf(currentArea.ifBlank { "Downtown Metro" }) }
    var address by remember { mutableStateOf(currentAddress.ifBlank { "420 Maple Avenue, Apt 3B, Metro City" }) }
    var locationStatus by remember { mutableStateOf<String?>(null) }

    fun resolveDeviceCoarseLocation(ctx: Context) {
        try {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            @Suppress("MissingPermission")
            val loc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (loc != null) {
                val geocoder = Geocoder(ctx, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val resolved = addresses?.firstOrNull()
                if (resolved != null) {
                    val line = resolved.getAddressLine(0) ?: "${resolved.locality ?: "Metro City"}"
                    address = line
                    val areaMatch = HealthcareCatalog.metroAreas.firstOrNull {
                        line.contains(it.substringBefore(" "), ignoreCase = true)
                    } ?: "Midtown Medical District"
                    selectedArea = areaMatch
                    locationStatus = "Detected device location: $areaMatch"
                } else {
                    locationStatus = "GPS lock (${String.format(Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)}) mapped to Downtown Metro"
                }
            } else {
                locationStatus = "No cached GPS fix on emulator; pick a service zone below."
            }
        } catch (_: Exception) {
            locationStatus = "Could not query GPS; pick a service zone below."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            resolveDeviceCoarseLocation(context)
        } else {
            locationStatus = "Location permission denied. Select your area manually below."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Care Location", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            resolveDeviceCoarseLocation(context)
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("use_device_location_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detect Current Location")
                }

                if (locationStatus != null) {
                    Text(
                        text = locationStatus!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text("Service Zone:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                HealthcareCatalog.metroAreas.forEach { area ->
                    FilterChip(
                        selected = selectedArea == area,
                        onClick = { selectedArea = area },
                        label = { Text(area) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Home / Pickup Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedArea, address.trim()) },
                modifier = Modifier.testTag("confirm_location_button")
            ) {
                Text("Update Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
