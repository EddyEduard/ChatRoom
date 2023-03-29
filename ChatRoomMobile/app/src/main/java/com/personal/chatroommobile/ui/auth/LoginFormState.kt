package com.personal.chatroommobile.ui.auth

/**
 * Data validation state of the sign in form.
 */
data class LoginFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)