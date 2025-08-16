package com.example.emedibotsimpleuserlogin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch





data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {

        _messages.value = listOf(
            ChatMessage(
                text = "Hello! I'm EmediBot. I can answer basic questions. How can I help?",
                isUser = false
            )
        )
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return


        _messages.value += ChatMessage(userText, true)
        _isTyping.value = true


        viewModelScope.launch {
            delay(1500) // simulate thinking
            val botResponse = getMockResponse(userText)
            _messages.value += ChatMessage(botResponse, false)
            _isTyping.value = false
        }
    }

    /**
     * This function contains the mock conversation logic.
     */
    private fun getMockResponse(userText: String): String {
        val normalizedText = userText.trim().lowercase()

        return when {

            normalizedText in listOf("hi", "hello") -> {
                "Hi, how can I assist you?"
            }
            normalizedText in listOf("thanks","ok thanks") -> {
                "It's my pleasure to assist u "
            }

            "fever" in normalizedText && ("medicine" in normalizedText || "preferable" in normalizedText) -> {
                "For fever, common over-the-counter options include Paracetamol or Ibuprofen. " +
                        "However, I am an AI assistant and not a medical professional. " +
                        "It is always best to consult with a doctor or pharmacist for medical advice."
            }

            "headache" in normalizedText -> {
                "For a headache, people often take Aspirin or Paracetamol. Please consult a doctor for persistent issues."
            }
            "how are you" in normalizedText -> {
                "I'm a bot, so I'm always running at 100%! How can I help you today?"
            }

            else -> {
                "I can only respond to a few specific questions right now. Try asking me about medicine for a fever or a headache."
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EmediBot AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }
                if (isTyping) {
                    item {
                        TypingBubble()
                    }
                }
            }


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type your question...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(userInput)
                            userInput = ""
                        },
                        enabled = userInput.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (userInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (message.isUser) 40.dp else 0.dp,
                end = if (message.isUser) 0.dp else 40.dp
            ),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = textColor,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .background(backgroundColor, shape = bubbleShape)
                .padding(12.dp)
        )
    }
}

@Composable
fun TypingBubble() {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dotCount = (dotCount + 1) % 4
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Typing${".".repeat(dotCount)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 16.dp)
        )
    }
}
