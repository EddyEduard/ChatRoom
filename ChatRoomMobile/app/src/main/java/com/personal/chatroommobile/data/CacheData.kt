package com.personal.chatroommobile.data

import com.personal.chatroommobile.ui.account.GroupItemView
import com.personal.chatroommobile.ui.account.ProfileView
import com.personal.chatroommobile.ui.contacts.ContactItemView

object CacheData {
    var userId: Int = 0

    lateinit var name: String

    lateinit var token: String

    lateinit var contact: ContactItemView

    lateinit var group: GroupItemView

    lateinit var profile: ProfileView
}
