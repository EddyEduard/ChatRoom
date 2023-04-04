package com.personal.chatroommobile.ui.account

data class ProfileView(
    val image: String,
    val name: String,
    val email: String,
    val groups: ArrayList<GroupItemView>
)
