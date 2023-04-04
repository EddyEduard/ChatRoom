package com.personal.chatroommobile.ui.auth

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val id: Int,
    val name: String,
    val token: String
)