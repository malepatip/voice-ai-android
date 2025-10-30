package com.voiceai.domain.repository

/**
 * Repository for Text-to-Speech operations.
 * Abstracts underlying TTS engines (Android TTS, Sonic 3).
 */
interface TTSRepository {
    /**
     * Synthesize speech using on-device Android TTS
     */
    suspend fun synthesizeOnDevice(
        text: String,
        language: String = "en-US",
        pitch: Float = 1.0f,
        rate: Float = 1.0f
    ): Result<TTSOutput>

    /**
     * Synthesize speech using Cartesia Sonic 3 API
     */
    suspend fun synthesizeSonic3(
        text: String,
        voiceId: String = "default",
        emotion: String? = null,
        speed: Float = 1.0f,
        outputFormat: AudioFormat = AudioFormat.PCM_16KHZ
    ): Result<TTSOutput>

    /**
     * Check if on-device TTS is available and initialized
     */
    suspend fun isOnDeviceTTSAvailable(): Boolean

    /**
     * Check if Sonic 3 API is available (network + API key)
     */
    suspend fun isSonic3Available(): Boolean

    /**
     * Get list of available voices for on-device TTS
     */
    suspend fun getAvailableVoices(): List<VoiceInfo>

    /**
     * Get list of available Sonic 3 voices
     */
    suspend fun getSonic3Voices(): Result<List<Sonic3Voice>>
}

/**
 * Output from TTS synthesis
 */
data class TTSOutput(
    val audioData: ByteArray,
    val format: AudioFormat,
    val durationMs: Long,
    val sampleRate: Int,
    val metadata: TTSMetadata = TTSMetadata()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TTSOutput

        if (!audioData.contentEquals(other.audioData)) return false
        if (format != other.format) return false
        if (durationMs != other.durationMs) return false
        if (sampleRate != other.sampleRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = audioData.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + sampleRate
        return result
    }
}

/**
 * Audio format for TTS output
 */
enum class AudioFormat {
    PCM_16KHZ,      // 16kHz PCM (standard quality)
    PCM_24KHZ,      // 24kHz PCM (high quality)
    PCM_44_1KHZ,    // 44.1kHz PCM (CD quality)
    MP3,            // MP3 compressed
    OGG,            // OGG compressed
    WAV             // WAV format
}

/**
 * Metadata about TTS synthesis
 */
data class TTSMetadata(
    val engine: String = "",
    val voiceId: String = "",
    val language: String = "",
    val charactersProcessed: Int = 0,
    val latencyMs: Long = 0,
    val cost: Float = 0.0f
)

/**
 * Information about a voice
 */
data class VoiceInfo(
    val id: String,
    val name: String,
    val language: String,
    val gender: VoiceGender = VoiceGender.NEUTRAL,
    val quality: VoiceQuality = VoiceQuality.NORMAL
)

enum class VoiceGender {
    MALE,
    FEMALE,
    NEUTRAL
}

enum class VoiceQuality {
    LOW,
    NORMAL,
    HIGH,
    VERY_HIGH
}

/**
 * Sonic 3 specific voice information
 */
data class Sonic3Voice(
    val id: String,
    val name: String,
    val description: String,
    val language: String,
    val accent: String? = null,
    val gender: VoiceGender,
    val ageRange: String? = null,
    val styles: List<String> = emptyList(), // e.g., ["cheerful", "sad", "angry"]
    val sampleUrl: String? = null,
    val isCloned: Boolean = false
)

/**
 * Configuration for Sonic 3 voice cloning
 */
data class VoiceCloneConfig(
    val name: String,
    val description: String,
    val audioSamples: List<ByteArray>,
    val language: String = "en",
    val enhance: Boolean = true
)

/**
 * Result of voice cloning operation
 */
sealed class VoiceCloneResult {
    data class Success(
        val voiceId: String,
        val voice: Sonic3Voice
    ) : VoiceCloneResult()

    data class Failure(
        val error: VoiceCloneError,
        val message: String
    ) : VoiceCloneResult()
}

enum class VoiceCloneError {
    INSUFFICIENT_AUDIO,
    AUDIO_QUALITY_POOR,
    RATE_LIMITED,
    QUOTA_EXCEEDED,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}
