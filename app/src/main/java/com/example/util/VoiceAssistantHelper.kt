package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

private const val VOICE_TAG = "VoiceAssistantHelper"

/**
 * Records short AAC/M4A voice clips directly into app cache for multimodal Gemini audio understanding
 * (supports Assamese, Hindi, and English speech natively without requiring device STT packs).
 */
class VoiceAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordingStartMs: Long = 0L

    fun startRecording(): Boolean {
        cancelRecording()
        return try {
            val file = File(context.cacheDir, "nidanmitra_voice_${System.currentTimeMillis()}.m4a")
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(32_000)
                setAudioSamplingRate(16_000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            outputFile = file
            recordingStartMs = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e(VOICE_TAG, "Failed to start audio recording", e)
            cancelRecording()
            false
        }
    }

    /**
     * Stops recording and returns Pair(audioBytes, durationSeconds) or null on failure / too-short clip.
     */
    fun stopAndReadBytes(): Pair<ByteArray, Int>? {
        val currentRecorder = recorder ?: return null
        val file = outputFile
        val elapsedSec = ((System.currentTimeMillis() - recordingStartMs) / 1000L).toInt().coerceAtLeast(1)
        recorder = null
        outputFile = null
        return try {
            currentRecorder.stop()
            currentRecorder.release()
            if (file != null && file.exists() && file.length() > 512L) {
                val bytes = file.readBytes()
                file.delete()
                bytes to elapsedSec
            } else {
                file?.delete()
                null
            }
        } catch (e: Exception) {
            Log.e(VOICE_TAG, "Failed to stop audio recording cleanly", e)
            try {
                currentRecorder.release()
            } catch (_: Exception) {
            }
            file?.delete()
            null
        }
    }

    fun cancelRecording() {
        val currentRecorder = recorder
        recorder = null
        if (currentRecorder != null) {
            try {
                currentRecorder.stop()
            } catch (_: Exception) {
            }
            try {
                currentRecorder.release()
            } catch (_: Exception) {
            }
        }
        outputFile?.delete()
        outputFile = null
    }
}

/**
 * Wraps Android TextToSpeech with automatic script-aware language routing (Assamese, Hindi, English)
 * and lifecycle-safe playback state.
 */
class VoiceSpeaker(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false
    private var pendingSpeech: Triple<String, String, AppLanguage>? = null

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speakingMessageId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }
                })
                pendingSpeech?.let { (id, text, lang) ->
                    pendingSpeech = null
                    speak(id, text, lang)
                }
            } else {
                Log.w(VOICE_TAG, "TextToSpeech initialization failed with status=$status")
            }
        }
    }

    fun speak(messageId: String, rawText: String, preferredLanguage: AppLanguage) {
        val cleaned = cleanForSpeech(rawText)
        if (cleaned.isBlank()) return
        val engine = tts ?: return
        if (!isReady) {
            pendingSpeech = Triple(messageId, cleaned, preferredLanguage)
            return
        }
        applyBestLocale(engine, cleaned, preferredLanguage)
        engine.setSpeechRate(0.95f)
        _speakingMessageId.value = messageId
        engine.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        pendingSpeech = null
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _speakingMessageId.value = null
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        isReady = false
    }

    private fun applyBestLocale(engine: TextToSpeech, text: String, preferred: AppLanguage) {
        val hasAssameseScript = text.any { it in '\u0980'..'\u09FF' }
        val hasDevanagariScript = text.any { it in '\u0900'..'\u097F' }

        val candidates = when {
            hasAssameseScript -> listOf(
                Locale.forLanguageTag("as-IN"),
                Locale.forLanguageTag("bn-IN"),
                Locale.forLanguageTag("hi-IN"),
                Locale.forLanguageTag("en-IN"),
                Locale.US
            )
            hasDevanagariScript -> listOf(
                Locale.forLanguageTag("hi-IN"),
                Locale.forLanguageTag("en-IN"),
                Locale.US
            )
            preferred == AppLanguage.ASSAMESE -> listOf(
                Locale.forLanguageTag("as-IN"),
                Locale.forLanguageTag("bn-IN"),
                Locale.forLanguageTag("en-IN"),
                Locale.US
            )
            preferred == AppLanguage.HINDI -> listOf(
                Locale.forLanguageTag("hi-IN"),
                Locale.forLanguageTag("en-IN"),
                Locale.US
            )
            else -> listOf(
                Locale.forLanguageTag("en-IN"),
                Locale.US
            )
        }

        for (locale in candidates) {
            val result = engine.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                return
            }
        }
    }

    private fun cleanForSpeech(raw: String): String {
        return raw
            .replace(Regex("""[*#_`~]"""), "")
            .replace(Regex("""\[[^\]]*\]"""), "")
            .replace("₹", "rupees ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
