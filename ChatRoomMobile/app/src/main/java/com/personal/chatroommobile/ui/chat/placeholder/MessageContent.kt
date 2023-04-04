package com.personal.chatroommobile.ui.chat.placeholder

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.personal.chatroommobile.ui.chat.MessageItemView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object MessageContent {

    var ITEMS: MutableList<MessageItemView> = ArrayList()

    private val ITEM_MAP: MutableMap<Int, MessageItemView> = HashMap()

    /**
     * Search for messages
     *
     * @param messages
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    fun foundMessages(messages: ArrayList<MessageItemView>) {
        this.ITEMS = ArrayList()

        messages.forEach {
            addItem(it)
        }
    }

    /**
     * Add a new item in list.
     *
     * @param item
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun addItem(item: MessageItemView) {
        val dateString =
            item.time.replace("T", " ").replace("Z", "").split(".")[0]
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = LocalDateTime.parse(dateString, pattern)
        val time =
            SimpleDateFormat("HH:mm").format(Date.from(localDateTime.atZone(
                ZoneId.systemDefault()).toInstant()))

        item.time = time

        this.ITEMS.add(item)
        ITEM_MAP[item.position] = item
    }
}