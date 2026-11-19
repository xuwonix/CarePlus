package com.example.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HomeCareService
import com.example.data.model.NearbyFacility
import com.example.data.model.Specialist
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.EmergencyRed
import com.example.ui.viewmodel.AiSuggestedAction
import com.example.ui.viewmodel.GeminiBookingViewModel
import com.example.ui.viewmodel.GeminiChatMessage
import com.example.ui.viewmodel.GeminiChatUiState
import com.example.util.VoiceAudioRecorder
import com.example.util.VoiceSpeaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiBookingChatScreen(
    geminiViewModel: GeminiBookingViewModel,
    selectedLocationArea: String,
    onSelectAppLanguage: (AppLanguage) -> Unit,
    onBookDoctor: (Specialist) -> Unit,
    onBookLabTest: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onBookHomeCare: (HomeCareService) -> Unit,
    onOpenFacilityDirections: (NearbyFacility) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appLanguage = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    val state by geminiViewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val audioRecorder = remember(context) { VoiceAudioRecorder(context) }
    val voiceSpeaker = remember(context) { VoiceSpeaker(context) }
    val speakingMessageId by voiceSpeaker.speakingMessageId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.cancelRecording()
            voiceSpeaker.shutdown()
        }
    }

    BackHandler {
        audioRecorder.cancelRecording()
        voiceSpeaker.stop()
        onBack()
    }

    fun startVoiceCapture() {
        voiceSpeaker.stop()
        val started = audioRecorder.startRecording()
        if (started) {
            recordingSeconds = 0
            isRecording = true
        } else {
            geminiViewModel.reportVoiceError("Could not start microphone recording on this device.")
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceCapture()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(strings.voicePermissionDenied)
            }
        }
    }

    fun requestOrStartVoice() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            startVoiceCapture()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Recording duration ticker (auto-sends at 45s to keep audio payloads compact)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000L)
                recordingSeconds += 1
                if (recordingSeconds >= 45) {
                    isRecording = false
                    val captured = audioRecorder.stopAndReadBytes()
                    if (captured != null) {
                        val (bytes, duration) = captured
                        geminiViewModel.sendVoiceMessage(
                            audioBytes = bytes,
                            durationSeconds = duration,
                            selectedLocationArea = selectedLocationArea,
                            voiceBubbleFallbackLabel = strings.voiceMessageLabel
                        )
                    }
                    break
                }
            }
        }
    }

    LaunchedEffect(appLanguage) {
        geminiViewModel.syncLanguage(appLanguage)
    }

    // Automatically read aloud finished AI replies when auto-speak is enabled
    LaunchedEffect(state.pendingAutoSpeakMessageId) {
        val targetId = state.pendingAutoSpeakMessageId ?: return@LaunchedEffect
        val message = state.messages.firstOrNull { it.id == targetId }
        if (message != null && message.text.isNotBlank() && state.autoSpeakEnabled) {
            voiceSpeaker.speak(message.id, message.text, state.preferredLanguage)
        }
        geminiViewModel.consumeAutoSpeak()
    }

    val isBusy = state.status is GeminiChatUiState.Loading || state.status is GeminiChatUiState.Streaming

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text?.length) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    val starterPrompts = remember(state.preferredLanguage) {
        when (state.preferredLanguage) {
            AppLanguage.ASSAMESE -> listOf(
                "মোৰ জ্বৰ হৈছে, এজন ভাল ডাক্তৰ বুক কৰাত সহায় কৰক",
                "বিশ্বনাথ চাৰিআলিত শিশুৰ ডাক্তৰ লাগে",
                "ঘৰৰ পৰা তেজ পৰীক্ষা (Blood Test) বুক কৰিব লাগে",
                "তেজপুৰৰ ওচৰৰ হাস্পতালৰ দিশা-নিৰ্দেশ দিয়ক",
                "ঘৰতে নাৰ্চিং সেৱা বুক কৰক"
            )
            AppLanguage.HINDI -> listOf(
                "मुझे बुखार है, डॉक्टर बुक करने में मदद करें",
                "विश्वनाथ चारियाली में बच्चों के डॉक्टर चाहिए",
                "घर से ब्लड टेस्ट (CBC) बुक करना है",
                "नज़दीकी अस्पताल का रास्ता बताइए",
                "होम नर्सिंग सेवा बुक करें"
            )
            AppLanguage.ENGLISH -> listOf(
                "Help me book a doctor for fever",
                "অসমীয়াত ডাক্তৰ বুক কৰাত সহায় কৰক",
                "Book a blood test with home pickup",
                "Pediatrician in Biswanath Chariali",
                "Directions to nearest hospital"
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gemini_booking_chat_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.aiAssistantTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.aiAssistantSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            audioRecorder.cancelRecording()
                            voiceSpeaker.stop()
                            onBack()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("gemini_chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val nowOn = geminiViewModel.toggleAutoSpeak()
                            if (!nowOn) {
                                voiceSpeaker.stop()
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (nowOn) strings.voiceAutoSpeakOn else strings.voiceAutoSpeakOff
                                )
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("gemini_chat_voice_toggle")
                    ) {
                        Icon(
                            imageVector = if (state.autoSpeakEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = if (state.autoSpeakEnabled) {
                                strings.voiceAutoSpeakOn
                            } else {
                                strings.voiceAutoSpeakOff
                            },
                            tint = if (state.autoSpeakEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            audioRecorder.cancelRecording()
                            isRecording = false
                            voiceSpeaker.stop()
                            geminiViewModel.resetConversation()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("gemini_chat_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = strings.aiAssistantNewChat
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isRecording) {
                        // Quick-tap prompt chips for non-technical / Assamese / Hindi / English users
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(starterPrompts) { prompt ->
                                SuggestionChip(
                                    onClick = {
                                        if (!isBusy) {
                                            voiceSpeaker.stop()
                                            geminiViewModel.sendMessage(prompt, selectedLocationArea)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = strings.aiAssistantInputPlaceholder,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gemini_chat_input")
                            )

                            // Prominent Voice Assistance Microphone Button (always visible)
                            FilledTonalIconButton(
                                onClick = {
                                    if (!isBusy) {
                                        requestOrStartVoice()
                                    }
                                },
                                enabled = !isBusy,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("gemini_chat_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = strings.voiceTapToSpeak
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    val textToSend = inputText.trim()
                                    if (textToSend.isNotEmpty() && !isBusy) {
                                        voiceSpeaker.stop()
                                        inputText = ""
                                        geminiViewModel.sendMessage(textToSend, selectedLocationArea)
                                    }
                                },
                                enabled = inputText.isNotBlank() && !isBusy,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("gemini_chat_send_button")
                            ) {
                                if (isBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send"
                                    )
                                }
                            }
                        }
                    } else {
                        // Live Voice Recording Bar
                        LiveVoiceRecordingBar(
                            recordingSeconds = recordingSeconds,
                            listeningHint = strings.voiceListeningHint,
                            cancelLabel = strings.voiceCancel,
                            sendLabel = strings.voiceStopAndSend,
                            onCancel = {
                                isRecording = false
                                audioRecorder.cancelRecording()
                            },
                            onStopAndSend = {
                                isRecording = false
                                val captured = audioRecorder.stopAndReadBytes()
                                if (captured != null) {
                                    val (bytes, duration) = captured
                                    geminiViewModel.sendVoiceMessage(
                                        audioBytes = bytes,
                                        durationSeconds = duration,
                                        selectedLocationArea = selectedLocationArea,
                                        voiceBubbleFallbackLabel = strings.voiceMessageLabel
                                    )
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Recording was too short. Please hold or speak a little longer.")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Language quick-switch bar so patients can switch to Assamese / Hindi / English in 1 tap
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = state.preferredLanguage == lang,
                            onClick = {
                                voiceSpeaker.stop()
                                onSelectAppLanguage(lang)
                                geminiViewModel.syncLanguage(lang)
                            },
                            label = {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (state.preferredLanguage == lang) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("gemini_lang_chip_${lang.code}")
                        )
                    }
                }
            }

            // Transparent Error Banner (no mock fallbacks)
            val currentStatus = state.status
            if (currentStatus is GeminiChatUiState.Error) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("gemini_chat_error_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = currentStatus.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        if (state.lastFailedPrompt != null) {
                            TextButton(onClick = { geminiViewModel.retryLastMessage(selectedLocationArea) }) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextButton(onClick = { geminiViewModel.dismissError() }) {
                                Text("OK", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Scrollable Conversation Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("gemini_chat_message_list"),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    GeminiMessageBubble(
                        message = msg,
                        isSpeaking = speakingMessageId == msg.id,
                        bookActionLabel = strings.aiAssistantBookAction,
                        directionsLabel = strings.directionsButton,
                        listenLabel = strings.voiceListenLabel,
                        stopSpeakingLabel = strings.voiceStopSpeakingLabel,
                        onToggleSpeak = {
                            if (speakingMessageId == msg.id) {
                                voiceSpeaker.stop()
                            } else {
                                voiceSpeaker.speak(msg.id, msg.text, state.preferredLanguage)
                            }
                        },
                        onBookDoctor = onBookDoctor,
                        onBookLabTest = onBookLabTest,
                        onBookHomeCare = onBookHomeCare,
                        onOpenFacilityDirections = onOpenFacilityDirections
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveVoiceRecordingBar(
    recordingSeconds: Int,
    listeningHint: String,
    cancelLabel: String,
    sendLabel: String,
    onCancel: () -> Unit,
    onStopAndSend: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulseScale"
    )
    val formattedTime = "%02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("gemini_voice_recording_bar"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(EmergencyRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listeningHint,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmergencyRed
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("gemini_voice_cancel_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(cancelLabel, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onStopAndSend,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("gemini_voice_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(sendLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GeminiMessageBubble(
    message: GeminiChatMessage,
    isSpeaking: Boolean,
    bookActionLabel: String,
    directionsLabel: String,
    listenLabel: String,
    stopSpeakingLabel: String,
    onToggleSpeak: () -> Unit,
    onBookDoctor: (Specialist) -> Unit,
    onBookLabTest: (DiagnosticCenter, DiagnosticTest) -> Unit,
    onBookHomeCare: (HomeCareService) -> Unit,
    onOpenFacilityDirections: (NearbyFacility) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromUser) 18.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 18.dp
            ),
            color = if (message.isFromUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            },
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!message.isFromUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Nidanmitra AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!message.isStreaming && message.text.isNotBlank()) {
                            TextButton(
                                onClick = onToggleSpeak,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("gemini_speak_msg_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) stopSpeakingLabel else listenLabel,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSpeaking) stopSpeakingLabel else listenLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                } else if (message.isVoiceMessage) {
                    val dur = "%d:%02d".format(message.voiceDurationSec / 60, message.voiceDurationSec % 60)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Voice • $dur",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.text.isBlank() && message.isStreaming) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isFromUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        // One-tap interactive booking / Google Maps directions cards suggested by Gemini
        if (!message.isFromUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                message.suggestedActions.forEach { action ->
                    when (action) {
                        is AiSuggestedAction.BookDoctor -> {
                            AiActionBookingCard(
                                icon = Icons.Default.MedicalServices,
                                title = action.specialist.name,
                                subtitle = "${action.specialist.specialty} • ${action.specialist.clinicHospital}",
                                meta = "₹${action.specialist.fee.toInt()} • ${action.specialist.nextAvailableSlot}",
                                ctaLabel = bookActionLabel,
                                isDirection = false,
                                testTag = "ai_action_book_doctor_${action.specialist.id}",
                                onClick = { onBookDoctor(action.specialist) }
                            )
                        }
                        is AiSuggestedAction.BookLabTest -> {
                            AiActionBookingCard(
                                icon = Icons.Default.Biotech,
                                title = action.test.name,
                                subtitle = action.center.name,
                                meta = "₹${action.test.price.toInt()} • ${action.test.turnaroundTime}",
                                ctaLabel = bookActionLabel,
                                isDirection = false,
                                testTag = "ai_action_book_lab_${action.test.id}",
                                onClick = { onBookLabTest(action.center, action.test) }
                            )
                        }
                        is AiSuggestedAction.BookHomeCare -> {
                            AiActionBookingCard(
                                icon = Icons.Default.HomeWork,
                                title = action.service.title,
                                subtitle = action.service.providerTeam,
                                meta = "₹${action.service.price.toInt()} • ${action.service.duration}",
                                ctaLabel = bookActionLabel,
                                isDirection = false,
                                testTag = "ai_action_book_homecare_${action.service.id}",
                                onClick = { onBookHomeCare(action.service) }
                            )
                        }
                        is AiSuggestedAction.OpenDirections -> {
                            AiActionBookingCard(
                                icon = Icons.Default.LocalHospital,
                                title = action.facility.name,
                                subtitle = action.facility.address,
                                meta = "${action.facility.phone} • ~${action.facility.waitTimeMinutes} min wait",
                                ctaLabel = directionsLabel,
                                isDirection = true,
                                testTag = "ai_action_directions_${action.facility.id}",
                                onClick = { onOpenFacilityDirections(action.facility) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiActionBookingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    meta: String,
    ctaLabel: String,
    isDirection: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDirection) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDirection) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (isDirection) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = ctaLabel, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = ctaLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
