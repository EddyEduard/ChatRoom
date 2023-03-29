package com.personal.chatroommobile.ui.auth

/**
 * Sign in result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val warning: String? = null,
    val error: Int? = null
)