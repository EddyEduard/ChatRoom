package com.personal.chatroommobile.data.model

data class Group(
    val id: Int,
    val idAdminUser: Int,
    val name: String,
    val image: String,
    val lastMessage: MessageGroup?
)
