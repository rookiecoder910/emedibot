package com.example.emedibotsimpleuserlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                errorMessage = "Username and password cannot be empty"
            )
            return
        }

        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val isSuccess = authRepository.login(username, password)
                _state.value = _state.value.copy(
                    isSignedIn = isSuccess,
                    isLoading = false,
                    errorMessage = if (!isSuccess) "Invalid credentials" else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.localizedMessage}"
                )
            }
        }
    }

}

