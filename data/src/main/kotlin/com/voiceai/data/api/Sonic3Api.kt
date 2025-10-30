package com.voiceai.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Cartesia Sonic 3 TTS API
 * https://docs.cartesia.ai/build-with-cartesia/tts-models/latest
 */
interface Sonic3Api {

    @POST("tts/bytes")
    suspend fun synthesize(
        @Header("X-API-Key") apiKey: String,
        @Header("Cartesia-Version") version: String = "2024-06-10",
        @Body request: Sonic3Request
    ): Response<ResponseBody>
}

data class Sonic3Request(
    val model_id: String = "sonic-english",
    val transcript: String,
    val voice: Sonic3Voice,
    val output_format: Sonic3OutputFormat = Sonic3OutputFormat()
)

data class Sonic3Voice(
    val mode: String = "id",
    val id: String = "a0e99841-438c-4a64-b679-ae501e7d6091"  // Default: Calm British narrator
)

data class Sonic3OutputFormat(
    val container: String = "raw",
    val encoding: String = "pcm_f32le",
    val sample_rate: Int = 22050
)
