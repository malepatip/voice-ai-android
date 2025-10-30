package com.voiceai.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.voiceai.data.api.Sonic3Api
import com.voiceai.data.api.Sonic3Request
import com.voiceai.data.api.Sonic3Voice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Simple TTS Repository for Sonic 3
 * Handles API calls and audio playback
 */
class SimpleTTSRepository(private val context: Context) {

    private val apiKey = "YOUR_API_KEY_HERE"  // TODO: Replace with actual key

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.cartesia.ai/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(Sonic3Api::class.java)

    suspend fun speak(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Call Sonic 3 API
            val response = api.synthesize(
                apiKey = apiKey,
                request = Sonic3Request(
                    transcript = text,
                    voice = Sonic3Voice()
                )
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API Error: ${response.code()} - ${response.message()}")
                )
            }

            val audioData = response.body()?.bytes() ?: return@withContext Result.failure(
                Exception("No audio data received")
            )

            // Play audio
            playAudio(audioData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun playAudio(audioData: ByteArray) = withContext(Dispatchers.Main) {
        try {
            // Convert bytes to float samples (PCM_F32LE format)
            val buffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN)
            val floatSamples = FloatArray(audioData.size / 4)
            for (i in floatSamples.indices) {
                floatSamples[i] = buffer.float
            }

            // Create AudioTrack
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            // Play audio
            audioTrack.play()
            audioTrack.write(floatSamples, 0, floatSamples.size, AudioTrack.WRITE_BLOCKING)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            throw Exception("Audio playback failed: ${e.message}", e)
        }
    }
}
