package com.example.emedibotsimpleuserlogin

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.emedibotsimpleuserlogin.datamodel.GeminiRequest
import com.example.emedibotsimpleuserlogin.datamodel.GeminiResponse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Response

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApi {
    @POST("v1/chat/completions")
    suspend fun getChatResponse(@Body request: GeminiRequest): Response<GeminiResponse>
}

class datamodel {


    data class Message(
        val role: String,
        val content: String
    )

    data class GeminiRequest(
        val model: String,
        val messages: SnapshotStateList<VoiceViewModel.Message>
    )

    data class Choice(
        val message: Message
    )

    data class GeminiResponse(
        val choices: List<Choice>
    )
    fun createGeminiApi(): GeminiApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "AIzaSyATElY5aMcAKosC_GnGOh0oLCDHcEVFKLs")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

}