package com.example.emedibotsimpleuserlogin

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emedibotsimpleuserlogin.datamodel.GeminiRequest
import kotlinx.coroutines.launch

class VoiceViewModel : ViewModel() {
    private val geminiApi = GeminiApiClient.api
    data class Message(
        val role: String,     // "user" or "assistant"
        val content: String
    )
    // List of chat messages
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    fun sendPromptToGemini(prompt: String) {
        // Add user message immediately
        _messages.add(Message(role = "user", content = prompt))

        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    model = "models/gemini-2.5-pro",
                    messages = _messages // send full conversation
                )
                val response = geminiApi.getChatResponse(request)
                if (response.isSuccessful) {
                    val botReply = response.body()
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?: "No response"

                    _messages.add(Message(role = "assistant", content = botReply))
                } else {
                    _messages.add(Message(role = "assistant", content = "Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                _messages.add(Message(role = "assistant", content = "Exception: ${e.localizedMessage}"))
            }
        }
    }
}

@Composable
fun VoiceScreen(viewModel: VoiceViewModel = viewModel()) {
    val chatMessages = viewModel.messages

    Column {
        // Chat list
        for (msg in chatMessages) {
            Text(
                text = "${msg.role}: ${msg.content}"
            )
        }

        // Voice input (or text input)
        VoiceInput { spokenText ->
            viewModel.sendPromptToGemini(spokenText)
        }
    }
}
