package com.personal.chatroommobile.ui.contacts

data class MessageItemView(
    var formUserId: Int,
    var content: String,
    var status: Int,
    var time: String,
)

enum class ContactType {
    USERS, GROUP
}

enum class ContactStatus {
    ONLINE, OFFLINE
}

data class ContactItemView(
    val id: Int,
    val name: String,
    val image: String,
    val lastMessage: MessageItemView?,
    val type: ContactType,
    var exist: Boolean,
    var status: ContactStatus,
    val position: Int,
)
