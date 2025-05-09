package com.example.emedibotsimpleuserlogin

import kotlinx.coroutines.delay

class AuthRepository() {

    private val validUsers = listOf(
        Pair("user1", "password1"),
        Pair("user2", "password2"),
        Pair("admin", "admin123")
    )

    suspend fun login(username: String, password: String): Boolean {

        if (username.isBlank() || password.isBlank()) {
            return false
        }

        delay(1000)
        return validUsers.any { it.first == username && it.second == password}
        }
}