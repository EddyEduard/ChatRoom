package com.personal.chatroommobile.ui.group

data class GroupChangeResult(
    val success: String? = null,
    val warning: String? = null,
    val error: Int? = null,
    val isForChangeDataGroup: Boolean = false,
    val isForAddMember: Boolean = false,
    val isForRemoveMember: Boolean = false,
    val isForDeleteGroup: Boolean = false,
)