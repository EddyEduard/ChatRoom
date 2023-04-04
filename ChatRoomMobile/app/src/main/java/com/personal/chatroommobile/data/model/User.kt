package com.personal.chatroommobile.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val image: String,
    val lastMessage: MessageUser?
)
