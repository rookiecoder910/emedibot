//package com.example.emedibotsimpleuserlogin
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.emedibotsimpleuserlogin.datamodel.GeminiRequest
//import com.example.emedibotsimpleuserlogin.datamodel.Message
//
//import kotlinx.coroutines.launch
//
//class VoiceViewModel : ViewModel() {
//    private val geminiApi = GeminiApiClient.api
//    private val _responseText = mutableStateOf("")
//    val responseText: State<String> = _responseText
//
//    fun sendPromptToGemini(prompt: String) {
//        viewModelScope.launch {
//            try {
//                val request = GeminiRequest(
//                    model = "models/gemini-2.5-pro",  // or your assigned model
//                    messages = listOf(Message(role = "user", content = prompt))
//                )
//                val response = geminiApi.getChatResponse(request)
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    _responseText.value = body?.choices?.firstOrNull()?.message?.content ?: "No response"
//                } else {
//                    _responseText.value = "Error: ${response.code()}"
//                }
//            } catch (e: Exception) {
//                _responseText.value = "Exception: ${e.localizedMessage}"
//            }
//        }
//    }
//}
//@Composable
//fun VoiceScreen(viewModel: VoiceViewModel = viewModel()) {
//    val response by viewModel.responseText
//
//    Column {
//        VoiceInput { spokenText ->
//            viewModel.sendPromptToGemini(spokenText)
//        }
//        Text(text = "Response: $response")
//    }
//}
//
