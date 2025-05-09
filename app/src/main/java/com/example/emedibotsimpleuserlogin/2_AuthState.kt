package com.example.emedibotsimpleuserlogin

/**
 * Represents the authentication state of the user
 * @property isSignedIn Whether the user is currently signed in
 * @property isLoading Whether an authentication operation is in progress
 * @property errorMessage Error message if authentication failed, null otherwise
 */
data class AuthState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
    }
}

