package com.personal.chatroommobile.ui.account

data class AccountFormState(
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val isDataValid: Boolean = false
)
