package com.personal.chatroommobile.ui.chat

enum class MessageType {
    SENT, RECEIVE
}

data class MessageItemView(
    val image: String,
    val name: String,
    val content: String,
    var status: Int,
    var time: String,
    val type: MessageType,
    val position: Int,
)
