package com.example.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.ChatMessage
import com.example.data.model.MedicalRecord
import com.example.ui.screens.patient.RecordAttachmentHelper
import com.example.ui.screens.patient.SavedAttachment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConsultationScreen(
    recipientName: String,
    recipientSpecialty: String = "Specialist Consultation",
    canSendMessages: Boolean = true,
    isSupportChat: Boolean = false,
    isInsuranceChat: Boolean = false,
    messages: List<ChatMessage>,
    savedLockerRecords: List<MedicalRecord> = emptyList(),
    onSendMessage: (
        message: String,
        attachmentType: String,
        attachmentPath: String,
        attachmentName: String,
        alsoSaveToLocker: Boolean
    ) -> Unit,
    onBookDoctor: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var pendingAttachment by remember { mutableStateOf<SavedAttachment?>(null) }
    var pendingFromLocker by remember { mutableStateOf(false) }
    var saveToLockerChecked by remember { mutableStateOf(true) }
    var showAttachDialog by remember { mutableStateOf(false) }
    var showLockerPickerDialog by remember { mutableStateOf(false) }
    var previewMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    val listState = rememberLazyListState()

    val lockerRecordsWithFiles = remember(savedLockerRecords) {
        savedLockerRecords.filter { it.attachmentPath.isNotBlank() }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null && file.exists()) {
            val saved = RecordAttachmentHelper.copyFileToAttachments(
                context = context,
                sourceFile = file,
                preferredName = "prescription_scan_${System.currentTimeMillis() % 10000}.jpg"
            )
            if (saved != null) {
                pendingAttachment = saved
                pendingFromLocker = false
                saveToLockerChecked = true
            } else {
                Toast.makeText(context, "Could not save scanned prescription.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, file) = RecordAttachmentHelper.createCameraOutputUri(context)
            pendingCameraFile = file
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to scan prescriptions. You can still upload from Gallery or PDF.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                pendingAttachment = saved
                pendingFromLocker = false
                saveToLockerChecked = true
            } else {
                Toast.makeText(context, "Could not attach selected photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val saved = RecordAttachmentHelper.copyUriToAttachments(context, uri)
            if (saved != null) {
                pendingAttachment = saved
                pendingFromLocker = false
                saveToLockerChecked = true
            } else {
                Toast.makeText(context, "Could not attach selected document.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCameraScan() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            val (uri, file) = RecordAttachmentHelper.createCameraOutputUri(context)
            pendingCameraFile = file
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickChips = when {
        isInsuranceChat -> listOf(
            "Compare family health plans (Tezpur & Biswanath)",
            "Check cashless hospital coverage",
            "Help with cashless pre-authorization",
            "Check PM-JAY / MMAAY eligibility"
        )
        isSupportChat -> listOf(
            "Help with appointment rescheduling",
            "Home sample pickup inquiry",
            "Cashless insurance pre-auth status",
            "Upload prescription for guidance"
        )
        else -> listOf(
            "Sharing my latest prescription",
            "Attached recent BP reading (120/80)",
            "Confirming pre-visit fasting hours",
            "Requesting prescription refill"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSupportChat || isInsuranceChat) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSupportChat || isInsuranceChat) Icons.Default.SupportAgent else Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = if (isSupportChat || isInsuranceChat) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = recipientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (canSendMessages) "Active • $recipientSpecialty" else "Booking Required • $recipientSpecialty",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (canSendMessages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (canSendMessages) Icons.Default.Description else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (canSendMessages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (canSendMessages) {
                                            "Secure Clinical Messaging"
                                        } else {
                                            "Appointment Required to Message"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (canSendMessages) {
                                            "Share symptoms, follow-up questions, or scan & upload prescriptions (photo or PDF) directly with $recipientName."
                                        } else {
                                            "To keep doctor inboxes focused on active patients, direct messaging unlocks as soon as you book an appointment with $recipientName."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id.ifBlank { "${it.createdAt?.seconds}_${it.message.hashCode()}" } }) { msg ->
                    val isMe = msg.senderId == currentUserId
                    MessageBubble(
                        message = msg,
                        isFromMe = isMe,
                        onOpenAttachment = { clickedMsg ->
                            if (clickedMsg.attachmentType.equals("PDF", ignoreCase = true)) {
                                val opened = RecordAttachmentHelper.openAttachmentExternally(
                                    context,
                                    clickedMsg.attachmentPath,
                                    clickedMsg.attachmentType
                                )
                                if (!opened) {
                                    Toast.makeText(
                                        context,
                                        "Prescription file not found on this device.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                previewMessage = clickedMsg
                            }
                        }
                    )
                }
            }

            if (!canSendMessages) {
                // Booking-required lock bar (prevents unbooked patients from messaging doctors)
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_booking_required_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Messaging is restricted to booked appointments",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Book a slot with $recipientName to chat and share prescriptions securely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (onBookDoctor != null) {
                            Button(
                                onClick = onBookDoctor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("chat_book_to_unlock_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Book Appointment to Message", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Pending prescription attachment preview bar
                pendingAttachment?.let { attachment ->
                    PendingAttachmentBar(
                        attachment = attachment,
                        showSaveToLockerOption = !pendingFromLocker,
                        saveToLockerChecked = saveToLockerChecked,
                        onToggleSaveToLocker = { saveToLockerChecked = it },
                        onRemove = {
                            pendingAttachment = null
                            pendingFromLocker = false
                        }
                    )
                }

                // Quick Prompt & Prescription Action Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = pendingAttachment != null,
                            onClick = { showAttachDialog = true },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Upload Prescription",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("chat_upload_prescription_chip")
                        )
                    }
                    items(quickChips) { chipText ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (chipText.contains("Upload prescription", ignoreCase = true) ||
                                    chipText.contains("Sharing my latest prescription", ignoreCase = true)
                                ) {
                                    textInput = chipText
                                    showAttachDialog = true
                                } else {
                                    onSendMessage(chipText, "", "", "", false)
                                }
                            },
                            label = {
                                Text(
                                    text = chipText,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }

                // Message Input Bar with Prescription Upload Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .testTag("chat_attach_prescription_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Upload or Scan Prescription",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = if (pendingAttachment != null) "Add note about prescription..." else "Type message or attach Rx...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val canTapSend = textInput.isNotBlank() || pendingAttachment != null
                    IconButton(
                        onClick = {
                            if (canTapSend) {
                                val attach = pendingAttachment
                                onSendMessage(
                                    textInput.trim(),
                                    attach?.fileType ?: "",
                                    attach?.localPath ?: "",
                                    attach?.displayName ?: "",
                                    attach != null && !pendingFromLocker && saveToLockerChecked
                                )
                                textInput = ""
                                pendingAttachment = null
                                pendingFromLocker = false
                            }
                        },
                        enabled = canTapSend,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (canTapSend) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canTapSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Attach Prescription Options Dialog
    if (showAttachDialog) {
        AlertDialog(
            onDismissRequest = { showAttachDialog = false },
            title = {
                Column {
                    Text("Send Prescription or Report", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Scan a paper prescription or upload a photo/PDF directly to $recipientName.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttachOptionRow(
                        icon = Icons.Default.CameraAlt,
                        title = "Scan Prescription (Camera)",
                        subtitle = "Snap a clear photo of your handwritten or printed Rx",
                        testTag = "chat_attach_camera_option",
                        onClick = {
                            showAttachDialog = false
                            launchCameraScan()
                        }
                    )
                    AttachOptionRow(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Upload Photo from Gallery",
                        subtitle = "Select an existing prescription or lab report image",
                        testTag = "chat_attach_gallery_option",
                        onClick = {
                            showAttachDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    AttachOptionRow(
                        icon = Icons.Default.PictureAsPdf,
                        title = "Upload PDF Document",
                        subtitle = "Attach lab test PDF or digital discharge summary",
                        testTag = "chat_attach_pdf_option",
                        onClick = {
                            showAttachDialog = false
                            pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                        }
                    )
                    if (lockerRecordsWithFiles.isNotEmpty()) {
                        HorizontalDivider()
                        AttachOptionRow(
                            icon = Icons.Default.FolderShared,
                            title = "Pick from Health Locker (${lockerRecordsWithFiles.size})",
                            subtitle = "Reuse a prescription or lab report already saved in Records",
                            testTag = "chat_attach_locker_option",
                            onClick = {
                                showAttachDialog = false
                                showLockerPickerDialog = true
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Pick from Health Locker Dialog
    if (showLockerPickerDialog) {
        AlertDialog(
            onDismissRequest = { showLockerPickerDialog = false },
            title = { Text("Select Saved Prescription / Report", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(lockerRecordsWithFiles) { rec ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingAttachment = SavedAttachment(
                                        localPath = rec.attachmentPath,
                                        displayName = rec.attachmentName.ifBlank { rec.title },
                                        fileType = rec.fileType.ifBlank { "IMAGE" }
                                    )
                                    pendingFromLocker = true
                                    saveToLockerChecked = false
                                    showLockerPickerDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (rec.fileType.equals("PDF", ignoreCase = true)) {
                                        Icons.Default.PictureAsPdf
                                    } else {
                                        Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${rec.category.replace("_", " ")} • ${rec.recordDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLockerPickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full-screen Prescription Image Preview Dialog
    previewMessage?.let { msg ->
        val fullBitmap = remember(msg.attachmentPath) {
            RecordAttachmentHelper.decodeSampledImageBitmap(msg.attachmentPath, maxDim = 1600)
        }
        AlertDialog(
            onDismissRequest = { previewMessage = null },
            title = {
                Text(
                    text = msg.attachmentName.ifBlank { "Shared Prescription" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (fullBitmap != null) {
                        Image(
                            bitmap = fullBitmap,
                            contentDescription = "Prescription image preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Text(
                            text = "Preview unavailable on this device. Tap Open File if available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (msg.message.isNotBlank()) {
                        Text(text = msg.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        val opened = RecordAttachmentHelper.openAttachmentExternally(
                            context,
                            msg.attachmentPath,
                            msg.attachmentType.ifBlank { "IMAGE" }
                        )
                        if (!opened) {
                            Toast.makeText(context, "Could not open file externally.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open File")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewMessage = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AttachOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PendingAttachmentBar(
    attachment: SavedAttachment,
    showSaveToLockerOption: Boolean,
    saveToLockerChecked: Boolean,
    onToggleSaveToLocker: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    val thumb = remember(attachment.localPath, attachment.fileType) {
        if (attachment.fileType.equals("IMAGE", ignoreCase = true)) {
            RecordAttachmentHelper.decodeSampledImageBitmap(attachment.localPath, maxDim = 320)
        } else {
            null
        }
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("chat_pending_attachment_bar")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = "Attached prescription thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Prescription",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prescription Attached (${attachment.fileType})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = attachment.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Remove attachment")
                }
            }
            if (showSaveToLockerOption) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleSaveToLocker(!saveToLockerChecked) }
                ) {
                    Checkbox(
                        checked = saveToLockerChecked,
                        onCheckedChange = onToggleSaveToLocker
                    )
                    Text(
                        text = "Also save a copy in Health Locker (Records)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isFromMe: Boolean,
    onOpenAttachment: (ChatMessage) -> Unit
) {
    val hasAttachment = message.attachmentPath.isNotBlank() || message.attachmentName.isNotBlank()
    val thumb = remember(message.attachmentPath, message.attachmentType) {
        if (message.attachmentPath.isNotBlank() && !message.attachmentType.equals("PDF", ignoreCase = true)) {
            RecordAttachmentHelper.decodeSampledImageBitmap(message.attachmentPath, maxDim = 640)
        } else {
            null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromMe) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isFromMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (hasAttachment) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFromMe) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAttachment(message) }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (thumb != null) {
                                Image(
                                    bitmap = thumb,
                                    contentDescription = "Prescription attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (message.attachmentType.equals("PDF", ignoreCase = true)) {
                                        Icons.Default.PictureAsPdf
                                    } else {
                                        Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.attachmentName.ifBlank { "Prescription Document" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Tap to view ${message.attachmentType.ifBlank { "FILE" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    if (message.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
