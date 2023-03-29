package com.personal.chatroommobile.ui.auth

/**
 * Data validation state of the sign up form.
 */
data class RegisterFormState(
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isDataValid: Boolean = false
)
