package com.personal.chatroommobile.ui.group.placeholder

import com.personal.chatroommobile.ui.group.MemberItemView
import java.util.ArrayList
import java.util.HashMap

object MemberContent {

    var ITEMS: MutableList<MemberItemView> = ArrayList()

    private val ITEM_MAP: MutableMap<Int, MemberItemView> = HashMap()

    /**
     * Search for members
     *
     * @param members
     * */
    fun foundMembers(members: ArrayList<MemberItemView>) {
        this.ITEMS = ArrayList()

        members.forEach {
            addItem(it)
        }
    }

    /**
     * Remove a member.
     *
     * @param item
     * */
    fun removeItem(item: MemberItemView) {
        this.ITEMS.remove(item)
    }

    /**
     * Add a new item in list.
     *
     * @param item
     * */
    fun addItem(item: MemberItemView) {
        this.ITEMS.add(item)
        ITEM_MAP[item.position] = item
    }
}