package com.example.ui.screens.patient

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.InsurancePartner
import com.example.data.model.UserInsurancePolicy
import java.io.File

@Composable
fun AddInsurancePolicyModal(
    defaultMembers: String = "Self",
    onDismiss: () -> Unit,
    onConfirm: (UserInsurancePolicy) -> Unit
) {
    val context = LocalContext.current
    val quickInsurers = listOf(
        "Star Health Insurance",
        "HDFC ERGO Health",
        "Niva Bupa",
        "Care Health",
        "ICICI Lombard",
        "New India Assurance",
        "PM-JAY / MMAAY (Assam)"
    )
    val policyTypes = listOf("Family Floater", "Individual", "Corporate / Group", "Govt. Scheme")
    val quickCovers = listOf(
        "₹3L" to "300000",
        "₹5L" to "500000",
        "₹10L" to "1000000",
        "₹15L" to "1500000",
        "₹25L" to "2500000"
    )

    var insurerName by remember { mutableStateOf("") }
    var planName by remember { mutableStateOf("") }
    var policyNumber by remember { mutableStateOf("") }
    var policyType by remember { mutableStateOf("Family Floater") }
    var sumInsuredText by remember { mutableStateOf("500000") }
    var validUntil by remember { mutableStateOf("Mar 2027") }
    var coveredMembers by remember { mutableStateOf(defaultMembers) }

    var attachment by remember { mutableStateOf<SavedAttachment?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not load selected policy card image."
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not attach policy PDF."
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null && file.exists()) {
            val saved = RecordAttachmentHelper.copyFileToAttachments(
                context = context,
                sourceFile = file,
                preferredName = "policy_ecard_${System.currentTimeMillis() % 10000}.jpg"
            )
            if (saved != null) {
                attachment = saved
                scanError = null
            } else {
                scanError = "Could not save scanned e-card."
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
            scanError = "Camera permission denied. Use Photo or PDF upload instead."
        }
    }

    val previewBitmap = remember(attachment?.localPath, attachment?.fileType) {
        val att = attachment
        if (att != null && att.fileType == "IMAGE") {
            RecordAttachmentHelper.decodeSampledImageBitmap(att.localPath, maxDim = 500)
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add My Health Insurance", fontWeight = FontWeight.Bold)
                Text(
                    text = "Save your purchased health insurance policy or Ayushman card for fast cashless hospital claims.",
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
                Text("Quick Select Insurer:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickInsurers) { company ->
                        FilterChip(
                            selected = insurerName.equals(company, ignoreCase = true),
                            onClick = {
                                insurerName = company
                                if (company.contains("PM-JAY")) {
                                    policyType = "Govt. Scheme"
                                    sumInsuredText = "500000"
                                }
                            },
                            label = { Text(company, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = insurerName,
                    onValueChange = { insurerName = it },
                    label = { Text("Insurance Company Name *") },
                    placeholder = { Text("e.g. Star Health, HDFC ERGO, LIC...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_insurer_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name (optional)") },
                    placeholder = { Text("e.g. Family Health Optima, Optima Secure") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_plan_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = policyNumber,
                    onValueChange = { policyNumber = it },
                    label = { Text("Policy Number / Health Card ID *") },
                    placeholder = { Text("e.g. P/141112/01/2026/004821") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_number_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Policy Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(policyTypes) { type ->
                        FilterChip(
                            selected = policyType == type,
                            onClick = { policyType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Annual Sum Insured (₹):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickCovers) { (label, raw) ->
                        FilterChip(
                            selected = sumInsuredText == raw,
                            onClick = { sumInsuredText = raw },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = sumInsuredText,
                    onValueChange = { sumInsuredText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Sum Insured Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = validUntil,
                        onValueChange = { validUntil = it },
                        label = { Text("Valid Until") },
                        placeholder = { Text("Mar 2027") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = coveredMembers,
                        onValueChange = { coveredMembers = it },
                        label = { Text("Members") },
                        placeholder = { Text("Self, Family") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Optional Policy e-Card / Document Scan & Upload
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Attach Policy e-Card or PDF (optional)",
                            style = MaterialTheme.typography.labelMedium,
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
                                    if (granted) launchCameraScan()
                                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photo", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*")) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
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
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Policy e-card preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = attachment!!.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                                IconButton(onClick = { attachment = null }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove policy card", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sum = sumInsuredText.toDoubleOrNull() ?: 500000.0
                    onConfirm(
                        UserInsurancePolicy(
                            insurerName = insurerName.trim(),
                            planName = planName.trim().ifBlank { "$policyType Plan" },
                            policyNumber = policyNumber.trim(),
                            policyType = policyType,
                            sumInsured = sum,
                            validUntil = validUntil.trim().ifBlank { "Active" },
                            coveredMembers = coveredMembers.trim().ifBlank { "Self" },
                            cardAttachmentPath = attachment?.localPath ?: "",
                            cardAttachmentName = attachment?.displayName ?: "",
                            cardFileType = attachment?.fileType ?: ""
                        )
                    )
                },
                enabled = insurerName.isNotBlank() && policyNumber.isNotBlank(),
                modifier = Modifier.testTag("save_insurance_policy_button")
            ) {
                Text("Save Policy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BuyInsuranceInquiryModal(
    partner: InsurancePartner?,
    defaultPhone: String,
    defaultLocation: String,
    onDismiss: () -> Unit,
    onConfirm: (
        partnerName: String,
        planName: String,
        inquiryType: String,
        coverageAmount: String,
        membersInfo: String,
        preferredPhone: String,
        notes: String
    ) -> Unit
) {
    val coverOptions = listOf("₹5 Lakh", "₹10 Lakh", "₹15 Lakh", "₹25 Lakh+")
    val memberOptions = listOf("Self", "Self + Spouse", "Family of 4", "Senior Parents")

    var selectedCover by remember { mutableStateOf(if (partner?.isGovtScheme == true) "₹5 Lakh (PM-JAY / MMAAY)" else "₹10 Lakh") }
    var selectedMembers by remember { mutableStateOf("Family of 4") }
    var phone by remember { mutableStateOf(defaultPhone) }
    var notes by remember { mutableStateOf("") }

    val partnerTitle = partner?.name ?: "Compare All Tied-Up Insurers"
    val planTitle = partner?.featuredPlan ?: "Best Family / Individual Health Plan"
    val inquiryType = when {
        partner == null -> "Advisor Callback"
        partner.isGovtScheme -> "Eligibility Assistance"
        else -> "Purchase Quote"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (partner?.isGovtScheme == true) "Check PM-JAY / MMAAY Eligibility"
                    else if (partner != null) "Buy / Get Quote: ${partner.name}"
                    else "Request Free Insurance Callback",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$planTitle • Served in $defaultLocation",
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
                if (partner != null && !partner.isGovtScheme) {
                    Text(
                        text = "Starts from ₹${partner.startingAnnualPremium}/year • ${partner.claimSettlementRatio}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (partner?.isGovtScheme != true) {
                    Text("Desired Coverage Amount:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(coverOptions) { opt ->
                            FilterChip(
                                selected = selectedCover == opt,
                                onClick = { selectedCover = opt },
                                label = { Text(opt) }
                            )
                        }
                    }
                }

                Text("Members to Cover:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memberOptions) { opt ->
                        FilterChip(
                            selected = selectedMembers == opt,
                            onClick = { selectedMembers = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Callback Phone Number *") },
                    placeholder = { Text("+91 98640 ...") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buy_insurance_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Age / Existing Conditions / Questions (optional)") },
                    placeholder = { Text("e.g. Eldest member age 42, want cashless in Tezpur & Biswanath") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "A Nidanmitra Insurance Advisor will call you with an official quote and zero-paperwork enrollment assistance.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        partnerTitle,
                        planTitle,
                        inquiryType,
                        selectedCover,
                        selectedMembers,
                        phone.trim().ifBlank { "Registered Account Phone" },
                        notes.trim()
                    )
                },
                modifier = Modifier.testTag("confirm_buy_insurance_inquiry_button")
            ) {
                Text(if (partner?.isGovtScheme == true) "Request Assistance" else "Request Quote & Callback")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
