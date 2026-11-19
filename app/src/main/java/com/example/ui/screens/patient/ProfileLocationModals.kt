package com.example.ui.screens.patient

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.FamilyMember
import com.example.data.model.UserInsurancePolicy
import com.example.data.model.UserProfile
import com.example.data.repository.HealthcareCatalog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubmitInsuranceClaimModal(
    userPolicies: List<UserInsurancePolicy> = emptyList(),
    preselectedPolicy: UserInsurancePolicy? = null,
    defaultHospital: String = "Baptist Christian Hospital, Tezpur",
    onDismiss: () -> Unit,
    onConfirm: (policy: String, provider: String, hospital: String, procedure: String, amount: Double, claimType: String, docRef: String) -> Unit
) {
    val initialPolicy = preselectedPolicy ?: userPolicies.firstOrNull()
    var selectedPolicyId by remember { mutableStateOf(initialPolicy?.id ?: "") }
    var insurerName by remember { mutableStateOf(initialPolicy?.insurerName ?: "") }
    var policyNumber by remember { mutableStateOf(initialPolicy?.policyNumber ?: "") }
    var hospitalName by remember { mutableStateOf(defaultHospital) }
    var procedureName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var claimType by remember { mutableStateOf("CASHLESS") }

    val localHospitals = listOf(
        "Baptist Christian Hospital, Tezpur",
        "Tezpur Medical College & Hospital (TMCH)",
        "Biswanath Chariali Civil Hospital",
        "Pragati Hospital & Research Centre, Biswanath"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Submit Insurance Claim / Pre-Auth", fontWeight = FontWeight.Bold)
                Text(
                    text = "File a cashless pre-authorization or reimbursement using your own policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (userPolicies.isNotEmpty()) {
                    Text("Select Your Saved Policy:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(userPolicies) { pol ->
                            FilterChip(
                                selected = selectedPolicyId == pol.id,
                                onClick = {
                                    selectedPolicyId = pol.id
                                    insurerName = pol.insurerName
                                    policyNumber = pol.policyNumber
                                },
                                label = { Text("${pol.insurerName} (${pol.policyNumber})", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tip: Save your policy in Health Insurance Hub so it auto-fills next time.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASHLESS", "REIMBURSEMENT").forEach { type ->
                        FilterChip(
                            selected = claimType == type,
                            onClick = { claimType = type },
                            label = { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                OutlinedTextField(
                    value = insurerName,
                    onValueChange = { insurerName = it },
                    label = { Text("Insurance Company *") },
                    placeholder = { Text("e.g. Star Health, HDFC ERGO, PM-JAY") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = policyNumber,
                    onValueChange = { policyNumber = it },
                    label = { Text("Policy Number / Card ID *") },
                    placeholder = { Text("Enter your policy number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Quick Pick Hospital:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(localHospitals) { hosp ->
                        FilterChip(
                            selected = hospitalName == hosp,
                            onClick = { hospitalName = hosp },
                            label = { Text(hosp.substringBefore(","), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital / Center Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = procedureName,
                    onValueChange = { procedureName = it },
                    label = { Text("Medical Procedure / Admission *") },
                    placeholder = { Text("e.g. Surgery, Hospitalization, Endoscopy") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Estimated / Billed Amount (₹) *") },
                    placeholder = { Text("e.g. 15000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        policyNumber.trim(),
                        insurerName.trim(),
                        hospitalName.trim(),
                        procedureName.trim().ifBlank { "Hospital Admission" },
                        amount,
                        claimType,
                        "DOC-REF-${(100..999).random()}"
                    )
                },
                enabled = insurerName.isNotBlank() && policyNumber.isNotBlank() && hospitalName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.testTag("submit_claim_confirm_button")
            ) {
                Text("Submit Claim")
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
    onConfirm: (
        title: String,
        category: String,
        doctor: String,
        date: String,
        summary: String,
        metrics: String,
        memberName: String,
        fileType: String,
        attachmentPath: String,
        attachmentName: String
    ) -> Unit
) {
    val context = LocalContext.current
    val memberOptions = remember(familyMembers) {
        listOf("Self") + familyMembers.map { "${it.name} (${it.relationship})" }
    }
    val todayLabel = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
    }

    var selectedMember by remember { mutableStateOf("Self") }
    var category by remember { mutableStateOf("PRESCRIPTION") }
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayLabel) }
    var summary by remember { mutableStateOf("") }
    var metrics by remember { mutableStateOf("") }
    var showOptionalNotes by remember { mutableStateOf(false) }

    var attachment by remember { mutableStateOf<SavedAttachment?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("PRESCRIPTION", "LAB_REPORT", "RADIOLOGY", "DISCHARGE_SUMMARY", "VITALS")

    // Photo Picker (Zero permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not load selected photo."
            }
        }
    }

    // Document Picker for PDFs & image scans (Zero permissions required)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not attach selected file."
            }
        }
    }

    // Camera capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedFile = pendingCameraFile
        if (success && capturedFile != null && capturedFile.exists()) {
            val saved = RecordAttachmentHelper.copyFileToAttachments(
                context = context,
                sourceFile = capturedFile,
                preferredName = "Scan_${category.lowercase()}_${System.currentTimeMillis()}.jpg"
            )
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not save scanned photo."
            }
        }
    }

    fun launchCameraScan() {
        val (uri, file) = RecordAttachmentHelper.createCameraOutputUri(context)
        pendingCameraFile = file
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraScan()
        } else {
            scanError = "Camera permission denied. Use Gallery or PDF / File upload instead."
        }
    }

    val previewBitmap = remember(attachment?.localPath, attachment?.fileType) {
        val att = attachment
        if (att != null && att.fileType == "IMAGE") {
            RecordAttachmentHelper.decodeSampledImageBitmap(att.localPath, maxDim = 600)
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Scan or Upload Record", fontWeight = FontWeight.Bold)
                Text(
                    text = "Snap a photo or upload a PDF of your prescription or lab report",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                // Scan / Upload Actions Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Scan or Attach Document",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        launchCameraScan()
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_camera_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_gallery_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photo", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    documentPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_pdf_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (scanError != null) {
                            Text(
                                text = scanError!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (attachment != null) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (previewBitmap != null) {
                                        Image(
                                            bitmap = previewBitmap,
                                            contentDescription = "Scanned document preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (attachment!!.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = attachment!!.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${attachment!!.fileType} Ready to Upload",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                        IconButton(onClick = { attachment = null }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove attachment", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Text("Document Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

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
                    label = { Text("Title (optional — auto-named if empty)") },
                    placeholder = {
                        val defaultCat = category.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                        Text("$defaultCat • $date")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("record_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor or Lab Name (optional)") },
                    placeholder = { Text("e.g. Apollo Tezpur or Dr. Baruah") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                TextButton(
                    onClick = { showOptionalNotes = !showOptionalNotes },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showOptionalNotes) "Hide optional notes" else "Add optional notes or vitals (optional)")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showOptionalNotes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (showOptionalNotes) {
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(74.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = metrics,
                        onValueChange = { metrics = it },
                        label = { Text("Vitals / Key Numbers (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catLabel = category.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }
                    val finalTitle = title.trim().ifBlank {
                        if (attachment != null) "Scanned $catLabel" else "$catLabel Note"
                    }
                    val finalDoctor = doctor.trim().ifBlank { "Uploaded Document" }
                    val finalFileType = attachment?.fileType ?: "NOTE"
                    onConfirm(
                        finalTitle,
                        category,
                        finalDoctor,
                        date,
                        summary.trim(),
                        metrics.trim(),
                        selectedMember,
                        finalFileType,
                        attachment?.localPath ?: "",
                        attachment?.displayName ?: ""
                    )
                },
                enabled = attachment != null || title.isNotBlank() || summary.isNotBlank() || metrics.isNotBlank(),
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
    var bloodGroup by remember { mutableStateOf(currentProfile.bloodGroup.ifBlank { "O+" }) }
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
                            savedAddress = savedAddress.trim(),
                            onboardingCompleted = true
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
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Female") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var allergies by remember { mutableStateOf("") }
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
    var selectedArea by remember { mutableStateOf(currentArea.ifBlank { "Mission Chariali, Tezpur" }) }
    var address by remember { mutableStateOf(currentAddress) }
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
                    val line = resolved.getAddressLine(0) ?: (resolved.locality ?: "Tezpur, Assam")
                    address = line
                    val areaMatch = HealthcareCatalog.metroAreas.firstOrNull {
                        line.contains(it.substringBefore(","), ignoreCase = true)
                    } ?: "Mission Chariali, Tezpur"
                    selectedArea = areaMatch
                    locationStatus = "Detected device location: $areaMatch"
                } else {
                    locationStatus = "GPS lock (${String.format(Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)}) mapped to Mission Chariali, Tezpur"
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
                    placeholder = { Text("Street, Apt, City") },
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
