package com.personal.chatroommobile.ui.account.placeholder

import com.personal.chatroommobile.ui.account.GroupItemView
import com.personal.chatroommobile.ui.contacts.ContactItemView
import java.util.ArrayList
import java.util.HashMap

object GroupContent {

    var ITEMS: MutableList<GroupItemView> = ArrayList()

    private val ITEM_MAP: MutableMap<Int, GroupItemView> = HashMap()

    /**
     * Search for groups
     *
     * @param groups
     * */
    fun foundGroups(groups: ArrayList<GroupItemView>) {
        this.ITEMS = ArrayList()

        groups.forEach {
            addItem(it)
        }
    }

    /**
     * Add a new item in list.
     *
     * @param item
     * */
    fun addItem(item: GroupItemView) {
        this.ITEMS.add(item)
        ITEM_MAP[item.position] = item
    }
}