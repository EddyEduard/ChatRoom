package com.personal.chatroommobile.ui.contacts.placeholder

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.personal.chatroommobile.ui.contacts.ContactItemView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object ContactContent {

    var ITEMS: MutableList<ContactItemView> = ArrayList()

    private val ITEM_MAP: MutableMap<Int, ContactItemView> = HashMap()

    /**
     * Search for contacts
     *
     * @param contacts
     * */
    fun foundContacts(contacts: ArrayList<ContactItemView>) {
        this.ITEMS = ArrayList()

        contacts.forEach {
            addItem(it)
        }
    }

    /**
     * Add a new item in list.
     *
     * @param item
     * */
    private fun addItem(item: ContactItemView) {
        ITEMS.add(item)
        ITEM_MAP[item.position] = item
    }

    /**
     * Add an item in list.
     *
     * @param item
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateItem(item: ContactItemView) {
        if (item.lastMessage != null && (item.lastMessage.time.contains("T") || item.lastMessage.time.contains(
                "Z"))
        ) {
            val dateString =
                item.lastMessage.time.replace("T", " ").replace("Z", "").split(".")[0]
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(dateString, pattern)
            val time =
                SimpleDateFormat("HH:mm").format(Date.from(localDateTime.atZone(
                    ZoneId.systemDefault()).toInstant()))

            item.lastMessage.time = time
        }

        this.ITEMS[item.position] = item
        ITEM_MAP[item.position] = item
    }
}