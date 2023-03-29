package com.personal.chatroommobile.ui.auth

/**
 * Sign up result : success (user details) or error message.
 */
data class RegisterResult(
    val success: String? = null,
    val warning: String? = null,
    val error: Int? = null
)
