package com.personal.chatroommobile.ui.account

/**
 * Account change result : success (user details) or error message.
 */
data class AccountChangeResult(
    val success: String? = null,
    val warning: String? = null,
    val error: Int? = null,
    val isForChangeDataAccount: Boolean = false,
    val isForCreateGroup: Boolean = false,
    val isForDeleteAccount: Boolean = false
)
