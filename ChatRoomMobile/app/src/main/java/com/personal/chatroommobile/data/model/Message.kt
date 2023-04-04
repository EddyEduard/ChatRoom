package com.personal.chatroommobile.data.model

data class MessageUser(
    val id: Int,
    val idUserFrom: Int,
    val idUserTo: Int,
    val content: String,
    val status: Int,
    val dateTime: String
)

data class MessageGroup(
    val id: Int,
    val idUser: Int,
    val idGroup: Int,
    val content: String,
    val status: Int,
    val seenMembers: String,
    val dateTime: String,
    val user: User?
)
