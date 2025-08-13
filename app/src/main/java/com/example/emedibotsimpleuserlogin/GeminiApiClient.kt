package com.example.emedibotsimpleuserlogin



import com.example.emedibotsimpleuserlogin.datamodel.GeminiRequest
import com.example.emedibotsimpleuserlogin.datamodel.GeminiResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

//interface GeminiApi {
//    @POST("v1/chat/completions") // replace with your actual Gemini endpoint
//    suspend fun getChatResponse(@Body request: GeminiRequest): Response<GeminiResponse>
//}

object GeminiApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.gemini.com/") // replace with actual Gemini base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)
}
