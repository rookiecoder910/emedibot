package com.example.emedibotsimpleuserlogin



data class GeminiRequest(
    val model: String,
    val messages: List<Message>
)
