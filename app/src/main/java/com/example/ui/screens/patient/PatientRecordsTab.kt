package com.example.ui.screens.patient

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DiagnosticBooking
import com.example.data.model.MedicalRecord
import com.example.data.model.ProcedureGuide

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordsTab(
    records: List<MedicalRecord>,
    diagnosticBookings: List<DiagnosticBooking>,
    procedureGuides: List<ProcedureGuide>,
    onAddRecord: () -> Unit,
    onSelectProcedure: (ProcedureGuide) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Health Locker", "Procedure Guides")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_records_tab")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryTabRow(
                selectedTabIndex = selectedSection,
                modifier = Modifier.fillMaxWidth()
            ) {
                sections.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("records_subtab_${title.lowercase().replace(" ", "_")}")
                    )
                }
            }

            if (selectedSection == 0) {
                HealthLockerSection(
                    records = records,
                    diagnosticBookings = diagnosticBookings,
                    onAddRecord = onAddRecord
                )
            } else {
                ProcedureGuidesSection(
                    guides = procedureGuides,
                    onSelectGuide = onSelectProcedure
                )
            }
        }

        if (selectedSection == 0) {
            FloatingActionButton(
                onClick = onAddRecord,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_medical_record_fab")
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan or Upload Medical Record")
            }
        }
    }
}

@Composable
private fun HealthLockerSection(
    records: List<MedicalRecord>,
    diagnosticBookings: List<DiagnosticBooking>,
    onAddRecord: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var viewingRecord by remember { mutableStateOf<MedicalRecord?>(null) }
    val categories = listOf("All", "PRESCRIPTION", "LAB_REPORT", "RADIOLOGY", "VITALS")

    val filteredRecords = remember(records, selectedCategory) {
        if (selectedCategory == "All") records
        else records.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scan & Store Health Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Snap a photo or upload PDFs of prescriptions, lab reports, and scans—no manual typing required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onAddRecord,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_record_button")
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan or Upload Prescription / Report")
                    }
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

        if (filteredRecords.isEmpty() && diagnosticBookings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Prescriptions or Reports Yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Tap 'Scan or Upload Prescription / Report' above to photograph a paper prescription or upload a lab PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredRecords) { record ->
                val thumbBitmap = remember(record.attachmentPath, record.fileType) {
                    if (record.attachmentPath.isNotBlank() && record.fileType == "IMAGE") {
                        RecordAttachmentHelper.decodeSampledImageBitmap(record.attachmentPath, maxDim = 500)
                    } else {
                        null
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (record.attachmentPath.isNotBlank()) {
                                viewingRecord = record
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionChip(onClick = {}, label = { Text(record.category.replace("_", " ")) })
                        }
                        Text(
                            text = "${record.doctorOrFacility} • ${record.recordDate} • For: ${record.memberName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (thumbBitmap != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = thumbBitmap,
                                contentDescription = "Scanned ${record.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else if (record.attachmentPath.isNotBlank() && record.fileType == "PDF") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = record.attachmentName.ifBlank { "Uploaded PDF Document" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (record.summary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(record.summary, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (record.keyMetrics.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Vitals: ${record.keyMetrics}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (record.attachmentPath.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (record.fileType == "PDF") {
                                        RecordAttachmentHelper.openAttachmentExternally(
                                            context,
                                            record.attachmentPath,
                                            record.fileType
                                        )
                                    } else {
                                        viewingRecord = record
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (record.fileType == "PDF") "Open PDF Document" else "View Scanned Document")
                            }
                        }
                    }
                }
            }

            if (diagnosticBookings.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Diagnostic Test Results (${diagnosticBookings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                                Text(booking.testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                SuggestionChip(onClick = {}, label = { Text(booking.status.replace("_", " ")) })
                            }
                            Text(
                                text = "${booking.centerName} • ${booking.scheduledDate} • For: ${booking.familyMemberName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (booking.resultSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
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

    if (viewingRecord != null) {
        val rec = viewingRecord!!
        val fullBitmap = remember(rec.attachmentPath) {
            RecordAttachmentHelper.decodeSampledImageBitmap(rec.attachmentPath, maxDim = 1600)
        }
        AlertDialog(
            onDismissRequest = { viewingRecord = null },
            title = {
                Column {
                    Text(rec.title, fontWeight = FontWeight.Bold)
                    Text("${rec.doctorOrFacility} • ${rec.recordDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (fullBitmap != null) {
                        Image(
                            bitmap = fullBitmap,
                            contentDescription = rec.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Text(
                            text = "Attachment: ${rec.attachmentName.ifBlank { "Document" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (rec.summary.isNotBlank()) {
                        Text(rec.summary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewingRecord = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        RecordAttachmentHelper.openAttachmentExternally(context, rec.attachmentPath, rec.fileType)
                    }
                ) {
                    Text("Open External")
                }
            }
        )
    }
}

@Composable
private fun ProcedureGuidesSection(
    guides: List<ProcedureGuide>,
    onSelectGuide: (ProcedureGuide) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Clinical Procedure Guidance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step-by-step preparation checklists, fasting protocols, and post-care instructions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(guides) { guide ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("procedure_guide_card_${guide.id}")
                    .clickable { onSelectGuide(guide) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(guide.category.replace("_", " ")) }
                        )
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(guide.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration: ${guide.estimatedDuration}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("${guide.recoveryDays}d Recovery", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSelectGuide(guide) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Preparation Checklist")
                    }
                }
            }
        }
    }
}
