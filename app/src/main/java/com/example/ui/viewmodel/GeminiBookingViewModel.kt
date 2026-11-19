package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiagnosticCenter
import com.example.data.model.DiagnosticTest
import com.example.data.model.HomeCareService
import com.example.data.model.NearbyFacility
import com.example.data.model.Specialist
import com.example.data.repository.HealthcareCatalog
import com.example.ui.i18n.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "GeminiBookingVM"

sealed class AiSuggestedAction {
    data class BookDoctor(val specialist: Specialist) : AiSuggestedAction()
    data class BookLabTest(val center: DiagnosticCenter, val test: DiagnosticTest) : AiSuggestedAction()
    data class BookHomeCare(val service: HomeCareService) : AiSuggestedAction()
    data class OpenDirections(val facility: NearbyFacility) : AiSuggestedAction()
}

data class GeminiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
    val isVoiceMessage: Boolean = false,
    val voiceDurationSec: Int = 0,
    val suggestedActions: List<AiSuggestedAction> = emptyList()
)

sealed interface GeminiChatUiState {
    data object Idle : GeminiChatUiState
    data object Loading : GeminiChatUiState
    data class Streaming(val partialText: String) : GeminiChatUiState
    data object Success : GeminiChatUiState
    data class Error(val message: String) : GeminiChatUiState
}

data class GeminiBookingState(
    val messages: List<GeminiChatMessage> = emptyList(),
    val status: GeminiChatUiState = GeminiChatUiState.Idle,
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
    val lastFailedPrompt: String? = null,
    val autoSpeakEnabled: Boolean = true,
    val pendingAutoSpeakMessageId: String? = null
)

class GeminiBookingViewModel : ViewModel() {

    private val specialists by lazy { HealthcareCatalog.getLocalSpecialists() }
    private val diagnosticCenters by lazy { HealthcareCatalog.getLocalDiagnosticCenters() }
    private val homeCareServices by lazy { HealthcareCatalog.getHomeCareServices() }
    private val facilities by lazy { HealthcareCatalog.getNearbyHospitalsAndCenters("Mission Chariali, Tezpur") }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(buildSystemInstruction())
            }
        )
    }

    private var chatSession: Chat? = null

    private val _state = MutableStateFlow(
        GeminiBookingState(
            messages = listOf(welcomeMessageFor(AppLanguage.ENGLISH)),
            status = GeminiChatUiState.Idle,
            preferredLanguage = AppLanguage.ENGLISH
        )
    )
    val state: StateFlow<GeminiBookingState> = _state.asStateFlow()

    private fun getOrStartChat(): Chat {
        return chatSession ?: model.startChat().also { chatSession = it }
    }

    fun syncLanguage(language: AppLanguage) {
        val current = _state.value
        if (current.preferredLanguage == language) return
        val onlyWelcome = current.messages.size <= 1 && current.messages.firstOrNull()?.isFromUser == false
        _state.update {
            it.copy(
                preferredLanguage = language,
                messages = if (onlyWelcome) listOf(welcomeMessageFor(language)) else it.messages
            )
        }
    }

    fun resetConversation() {
        chatSession = null
        val current = _state.value
        _state.update {
            GeminiBookingState(
                messages = listOf(welcomeMessageFor(current.preferredLanguage)),
                status = GeminiChatUiState.Idle,
                preferredLanguage = current.preferredLanguage,
                lastFailedPrompt = null,
                autoSpeakEnabled = current.autoSpeakEnabled,
                pendingAutoSpeakMessageId = null
            )
        }
    }

    fun toggleAutoSpeak(): Boolean {
        val next = !_state.value.autoSpeakEnabled
        _state.update {
            it.copy(
                autoSpeakEnabled = next,
                pendingAutoSpeakMessageId = if (!next) null else it.pendingAutoSpeakMessageId
            )
        }
        return next
    }

    fun consumeAutoSpeak() {
        _state.update { it.copy(pendingAutoSpeakMessageId = null) }
    }

    fun reportVoiceError(message: String) {
        _state.update { it.copy(status = GeminiChatUiState.Error(message)) }
    }

    fun dismissError() {
        _state.update { it.copy(status = GeminiChatUiState.Idle) }
    }

    fun retryLastMessage(selectedLocationArea: String) {
        val prompt = _state.value.lastFailedPrompt ?: return
        sendMessage(prompt, selectedLocationArea, isRetry = true)
    }

    fun sendVoiceMessage(
        audioBytes: ByteArray,
        durationSeconds: Int,
        selectedLocationArea: String,
        voiceBubbleFallbackLabel: String
    ) {
        if (audioBytes.isEmpty()) return
        if (_state.value.status is GeminiChatUiState.Loading || _state.value.status is GeminiChatUiState.Streaming) {
            return
        }

        val activeLang = _state.value.preferredLanguage
        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        _state.update { current ->
            current.copy(
                messages = current.messages +
                    GeminiChatMessage(
                        id = userMsgId,
                        isFromUser = true,
                        text = voiceBubbleFallbackLabel,
                        isVoiceMessage = true,
                        voiceDurationSec = durationSeconds
                    ) +
                    GeminiChatMessage(
                        id = assistantMsgId,
                        isFromUser = false,
                        text = "",
                        isStreaming = true
                    ),
                status = GeminiChatUiState.Loading,
                lastFailedPrompt = null,
                pendingAutoSpeakMessageId = null
            )
        }

        val multimodalPrompt = content("user") {
            inlineData(audioBytes, "audio/mp4")
            text(
                buildString {
                    append("[Voice Message Context: Patient area = ")
                    append(selectedLocationArea)
                    append(", App UI language = ")
                    append(activeLang.englishName)
                    append(" (")
                    append(activeLang.nativeName)
                    append("). Listen carefully to the patient's voice in Assamese, Hindi, or English. ")
                    append("First, on line 1 output [[TRANSCRIPT:<brief accurate transcription of what the patient said in their spoken language>]]. ")
                    append("Then reply warmly and concisely in the EXACT language the patient spoke (Assamese অসমীয়া, Hindi हिन्दी, or English) so it sounds natural when read aloud, and append any matching [[BOOK_DOCTOR:...]], [[BOOK_LAB:...]], [[BOOK_HOMECARE:...]], or [[DIRECTIONS:...]] tags at the end.]")
                }
            )
        }

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val chat = getOrStartChat()
                withTimeout(60_000L) {
                    chat.sendMessageStream(multimodalPrompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        if (piece.isNotEmpty()) {
                            accumulated.append(piece)
                            val rawSoFar = accumulated.toString()
                            val transcript = extractTranscript(rawSoFar)
                            val displaySoFar = stripActionTags(rawSoFar)
                            _state.update { current ->
                                current.copy(
                                    status = GeminiChatUiState.Streaming(displaySoFar),
                                    messages = current.messages.map { msg ->
                                        when (msg.id) {
                                            userMsgId -> if (!transcript.isNullOrBlank()) msg.copy(text = transcript) else msg
                                            assistantMsgId -> msg.copy(text = displaySoFar, isStreaming = true)
                                            else -> msg
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                val fullRaw = accumulated.toString().trim()
                if (fullRaw.isEmpty()) {
                    handleFailure(
                        assistantMsgId = assistantMsgId,
                        failedPrompt = null,
                        errorMessage = "Could not hear clearly. Please tap the mic and speak again."
                    )
                } else {
                    val transcript = extractTranscript(fullRaw)
                    val actions = parseSuggestedActions(fullRaw)
                    val cleanDisplay = stripActionTags(fullRaw).ifBlank {
                        when (activeLang) {
                            AppLanguage.ASSAMESE -> "তলৰ বুটামত টিপি পোনপটীয়াকৈ বুক কৰক।"
                            AppLanguage.HINDI -> "नीचे दिए गए बटन पर टैप करके सीधे बुकिंग करें।"
                            AppLanguage.ENGLISH -> "Tap the button below to continue with your booking."
                        }
                    }
                    _state.update { current ->
                        current.copy(
                            status = GeminiChatUiState.Success,
                            lastFailedPrompt = null,
                            pendingAutoSpeakMessageId = if (current.autoSpeakEnabled) assistantMsgId else null,
                            messages = current.messages.map { msg ->
                                when (msg.id) {
                                    userMsgId -> if (!transcript.isNullOrBlank()) msg.copy(text = transcript) else msg
                                    assistantMsgId -> msg.copy(
                                        text = cleanDisplay,
                                        isStreaming = false,
                                        suggestedActions = actions
                                    )
                                    else -> msg
                                }
                            }
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini voice response timed out", e)
                handleFailure(
                    assistantMsgId = assistantMsgId,
                    failedPrompt = null,
                    errorMessage = "Voice request timed out after 60 seconds. Please try speaking again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini voice booking chat failed", e)
                handleFailure(
                    assistantMsgId = assistantMsgId,
                    failedPrompt = null,
                    errorMessage = e.localizedMessage ?: "Could not process voice message right now."
                )
            }
        }
    }

    fun sendMessage(
        rawInput: String,
        selectedLocationArea: String,
        isRetry: Boolean = false
    ) {
        val userText = rawInput.trim()
        if (userText.isEmpty()) return
        if (_state.value.status is GeminiChatUiState.Loading || _state.value.status is GeminiChatUiState.Streaming) {
            return
        }

        val activeLang = _state.value.preferredLanguage
        val assistantMsgId = UUID.randomUUID().toString()

        _state.update { current ->
            val updatedMessages = if (isRetry) {
                current.messages + GeminiChatMessage(
                    id = assistantMsgId,
                    isFromUser = false,
                    text = "",
                    isStreaming = true
                )
            } else {
                current.messages +
                    GeminiChatMessage(isFromUser = true, text = userText) +
                    GeminiChatMessage(
                        id = assistantMsgId,
                        isFromUser = false,
                        text = "",
                        isStreaming = true
                    )
            }
            current.copy(
                messages = updatedMessages,
                status = GeminiChatUiState.Loading,
                lastFailedPrompt = null
            )
        }

        val promptWithContext = buildString {
            append("[Context: Patient area = ")
            append(selectedLocationArea)
            append(", App UI language = ")
            append(activeLang.englishName)
            append(" (")
            append(activeLang.nativeName)
            append("). Reply in the language the user writes in; if ambiguous, reply in ")
            append(activeLang.englishName)
            append(".]\n")
            append(userText)
        }

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val chat = getOrStartChat()
                withTimeout(60_000L) {
                    chat.sendMessageStream(promptWithContext).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        if (piece.isNotEmpty()) {
                            accumulated.append(piece)
                            val displaySoFar = stripActionTags(accumulated.toString())
                            _state.update { current ->
                                current.copy(
                                    status = GeminiChatUiState.Streaming(displaySoFar),
                                    messages = current.messages.map { msg ->
                                        if (msg.id == assistantMsgId) {
                                            msg.copy(text = displaySoFar, isStreaming = true)
                                        } else {
                                            msg
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                val fullRaw = accumulated.toString().trim()
                if (fullRaw.isEmpty()) {
                    handleFailure(
                        assistantMsgId = assistantMsgId,
                        failedPrompt = userText,
                        errorMessage = "Assistant returned an empty response. Please try again."
                    )
                } else {
                    val actions = parseSuggestedActions(fullRaw)
                    val cleanDisplay = stripActionTags(fullRaw).ifBlank {
                        when (activeLang) {
                            AppLanguage.ASSAMESE -> "তলৰ বুটামত টিপি পোনপটীয়াকৈ বুক কৰক।"
                            AppLanguage.HINDI -> "नीचे दिए गए बटन पर टैप करके सीधे बुकिंग करें।"
                            AppLanguage.ENGLISH -> "Tap the button below to continue with your booking."
                        }
                    }
                    _state.update { current ->
                        current.copy(
                            status = GeminiChatUiState.Success,
                            lastFailedPrompt = null,
                            pendingAutoSpeakMessageId = if (current.autoSpeakEnabled) assistantMsgId else null,
                            messages = current.messages.map { msg ->
                                if (msg.id == assistantMsgId) {
                                    msg.copy(
                                        text = cleanDisplay,
                                        isStreaming = false,
                                        suggestedActions = actions
                                    )
                                } else {
                                    msg
                                }
                            }
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini booking response timed out", e)
                handleFailure(
                    assistantMsgId = assistantMsgId,
                    failedPrompt = userText,
                    errorMessage = "Request timed out after 60 seconds. Please check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini booking chat failed", e)
                handleFailure(
                    assistantMsgId = assistantMsgId,
                    failedPrompt = userText,
                    errorMessage = e.localizedMessage ?: "Could not reach Nidanmitra AI right now."
                )
            }
        }
    }

    private fun handleFailure(
        assistantMsgId: String,
        failedPrompt: String?,
        errorMessage: String
    ) {
        _state.update { current ->
            current.copy(
                status = GeminiChatUiState.Error(errorMessage),
                lastFailedPrompt = failedPrompt,
                messages = current.messages.filterNot { it.id == assistantMsgId && it.text.isBlank() }
                    .map { msg ->
                        if (msg.id == assistantMsgId) msg.copy(isStreaming = false) else msg
                    }
            )
        }
    }

    private fun welcomeMessageFor(language: AppLanguage): GeminiChatMessage {
        val text = when (language) {
            AppLanguage.ASSAMESE ->
                "নমস্কাৰ! মই নিদানমিত্ৰ AI বুকিং সহায়ক। আপুনি যদি নিজে ডাক্তৰ, লেব টেষ্ট বা হোম কেয়াৰ বুক কৰিব নাজানে, মোক সাধাৰণ অসমীয়া ভাষাত কওক — যেনে \"মোৰ জ্বৰ হৈছে, তেজপুৰত ডাক্তৰ লাগে\" বা \"বিশ্বনাথ চাৰিআলিত তেজ পৰীক্ষা বুক কৰিব লাগে\"। মই আপোনাক সঠিক ডাক্তৰ বাছি দি পোনপটীয়া বুকিং বুটাম দিম।"
            AppLanguage.HINDI ->
                "नमस्ते! मैं निदानमित्र AI बुकिंग सहायक हूँ। अगर आपको खुद डॉक्टर, लैब टेस्ट या होम केयर बुक करना नहीं आता, तो बस मुझे अपनी समस्या या शहर (तेजपुर / विश्वनाथ चारियाली) बताएँ — मैं सही डॉक्टर चुनकर आपको सीधा बुकिंग बटन दूँगा।"
            AppLanguage.ENGLISH ->
                "Hello! I'm your Nidanmitra AI Booking Helper. If you're not sure how to book on your own, just tell me your symptom, test needed, or city (Tezpur / Biswanath Chariali) in English, Hindi, or Assamese (অসমীয়া) — I'll guide you step-by-step and give you a one-tap booking button right here."
        }
        return GeminiChatMessage(
            id = "welcome_${language.code}",
            isFromUser = false,
            text = text
        )
    }

    private fun extractTranscript(rawText: String): String? {
        val match = Regex("""\[\[TRANSCRIPT:([^\]]+)\]\]""").find(rawText)
        return match?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun stripActionTags(text: String): String {
        return text
            .replace(Regex("""\[\[(TRANSCRIPT|BOOK_DOCTOR|BOOK_LAB|BOOK_HOMECARE|DIRECTIONS):[^\]]*\]\]"""), "")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun parseSuggestedActions(rawText: String): List<AiSuggestedAction> {
        val actions = mutableListOf<AiSuggestedAction>()
        val seenKeys = mutableSetOf<String>()
        val regex = Regex("""\[\[(BOOK_DOCTOR|BOOK_LAB|BOOK_HOMECARE|DIRECTIONS):([^\]]+)\]\]""")

        regex.findAll(rawText).forEach { match ->
            val type = match.groupValues[1].trim()
            val payload = match.groupValues[2].trim()
            val dedupKey = "$type:$payload"
            if (!seenKeys.add(dedupKey)) return@forEach

            when (type) {
                "BOOK_DOCTOR" -> {
                    specialists.firstOrNull { it.id == payload }?.let {
                        actions.add(AiSuggestedAction.BookDoctor(it))
                    }
                }
                "BOOK_LAB" -> {
                    val parts = payload.split("|")
                    if (parts.size == 2) {
                        val center = diagnosticCenters.firstOrNull { it.id == parts[0].trim() }
                        val test = center?.testsOffered?.firstOrNull { it.id == parts[1].trim() }
                        if (center != null && test != null) {
                            actions.add(AiSuggestedAction.BookLabTest(center, test))
                        }
                    } else {
                        // Allow testId-only fallback
                        for (center in diagnosticCenters) {
                            val test = center.testsOffered.firstOrNull { it.id == payload }
                            if (test != null) {
                                actions.add(AiSuggestedAction.BookLabTest(center, test))
                                break
                            }
                        }
                    }
                }
                "BOOK_HOMECARE" -> {
                    homeCareServices.firstOrNull { it.id == payload }?.let {
                        actions.add(AiSuggestedAction.BookHomeCare(it))
                    }
                }
                "DIRECTIONS" -> {
                    facilities.firstOrNull { it.id == payload }?.let {
                        actions.add(AiSuggestedAction.OpenDirections(it))
                    }
                }
            }
        }
        return actions.take(3)
    }

    private fun buildSystemInstruction(): String {
        val doctorsCatalog = specialists.joinToString("\n") { doc ->
            "- id=${doc.id} | ${doc.name} | Specialty: ${doc.specialty} | Hospital: ${doc.clinicHospital} (hospitalId=${doc.hospitalId}) | Address: ${doc.address} | Fee: ₹${doc.fee.toInt()} | Next: ${doc.nextAvailableSlot} | Video: ${if (doc.offersVideoConsult) "Yes" else "No"}"
        }
        val labsCatalog = diagnosticCenters.joinToString("\n") { center ->
            val tests = center.testsOffered.joinToString("; ") { t ->
                "testId=${t.id}: ${t.name} (₹${t.price.toInt()}, ${t.turnaroundTime})"
            }
            "- centerId=${center.id} | ${center.name} | Address: ${center.address} | HomePickup: ${center.providesHomeCollection} | Tests: $tests"
        }
        val homeCareCatalog = homeCareServices.joinToString("\n") { svc ->
            "- serviceId=${svc.id} | ${svc.title} (${svc.duration}) | ₹${svc.price.toInt()} | ${svc.description}"
        }
        val facilitiesCatalog = facilities.joinToString("\n") { fac ->
            "- facilityId=${fac.id} | ${fac.name} (${fac.facilityType}) | Address: ${fac.address} | 24x7ER: ${fac.hasEmergencyUnit} | Phone: ${fac.phone}"
        }

        return """
            You are Nidanmitra AI Booking Helper, a warm, patient, easy-to-understand healthcare booking assistant for patients in Tezpur and Biswanath Chariali, Assam, India.

            PRIMARY PURPOSE:
            Help people who do not know how to book appointments, diagnostic lab tests, or home healthcare on their own—especially elderly patients, families, and Assamese/Hindi/English speakers.

            LANGUAGE RULES (CRITICAL):
            - You are fluent in natural, polite Assamese (অসমীয়া), Hindi (हिन्दी), and English.
            - If the user writes in Assamese script (অসমীয়া) OR Romanized Assamese (e.g., "mur bokhar hoise", "doctor book koribo lage", "Tezpur ot hospital"), reply warmly in clear, simple Assamese (অসমীয়া).
            - If the user writes in Hindi or Hinglish, reply in clear Hindi (हिन्दी).
            - Otherwise match the user's message language or the App UI language provided in [Context].
            - Keep explanations short, friendly, jargon-free, and easy to read on a mobile screen (use short bullet points or 1-2-3 steps).

            SAFETY & MEDICAL BOUNDARY:
            - You help with finding doctors, choosing specialties, booking tests/home care, and finding hospital directions—you do NOT diagnose serious conditions or prescribe medicines.
            - If the user mentions an emergency (chest pain, stroke, severe bleeding, breathing difficulty, unconsciousness), immediately advise calling 108 / tapping Emergency Alert SOS in Nidanmitra and include the nearest 24/7 hospital direction tag.

            ONE-TAP BOOKING & DIRECTIONS ACTION TAGS:
            Whenever you recommend a specific doctor, lab test, home care service, or hospital/clinic direction from the catalog below, append the matching exact tag(s) on new line(s) at the very end of your reply (the app converts these tags into big one-tap buttons for the patient!). Never invent IDs—use ONLY exact IDs from the catalog below:
            - To offer a one-tap doctor booking button: [[BOOK_DOCTOR:<doctor_id>]]
            - To offer a one-tap lab test booking button: [[BOOK_LAB:<center_id>|<test_id>]]
            - To offer a one-tap home care booking button: [[BOOK_HOMECARE:<service_id>]]
            - To offer a Google Maps directions button: [[DIRECTIONS:<facility_id>]]
            Tell the user gently that they can simply tap the button below your message to confirm their slot.

            AVAILABLE DOCTORS IN NIDANMITRA:
            $doctorsCatalog

            AVAILABLE DIAGNOSTIC LABS & TESTS:
            $labsCatalog

            AVAILABLE HOME CARE SERVICES:
            $homeCareCatalog

            HOSPITALS & CLINICS FOR DIRECTIONS:
            $facilitiesCatalog
        """.trimIndent()
    }
}
