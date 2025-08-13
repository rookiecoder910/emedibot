package com.example.emedibotsimpleuserlogin


data class GeminiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
