package com.personal.chatroommobile.ui.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.databinding.FragmentContactItemBinding
import com.personal.chatroommobile.ui.chat.ChatActivity
import com.personal.chatroommobile.ui.contacts.placeholder.ContactContent
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class ContactRecyclerViewAdapter(
    private val context: Context,
    private val contacts: List<ContactItemView>,
) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>(), Filterable {
    var contactsFilter = ArrayList<ContactItemView>()

    init {
        contactsFilter = contacts as ArrayList<ContactItemView>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentContactItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged", "ResourceAsColor", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactsFilter[position]
        holder.contactName.text = contact.name
        holder.contactLastMessageContent.text =
            if (contact.lastMessage?.content != "null") contact.lastMessage?.content else "No message yet!"
        holder.contactLastMessageTime.text =
            if (contact.lastMessage?.content != "null") contact.lastMessage?.time else ""
        holder.itemView.setOnClickListener {
            CacheData.contact = contact

            if (contact.lastMessage?.formUserId == contact.id) {
                contact.lastMessage.status = 1
                notifyItemChanged(contact.position)
                notifyItemChanged(contact.position, "payload")
            }

            val intent = Intent(context, ChatActivity::class.java)
            context.startActivity(intent)
        }

        Glide.with(context)
            .load(contact.image)
            .into(holder.contactImage)

        if (contact.lastMessage?.content != "null") {
            when (contact.lastMessage?.status) {
                0 -> {
                    holder.contactLastMessageContent.setTextColor(Color.BLACK)
                    holder.contactLastMessageContent.setTypeface(null, Typeface.BOLD)
                }
                1 -> {
                    holder.contactLastMessageContent.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        holder.statusUser.visibility = if (contact.exist) View.VISIBLE else View.GONE

        if (contact.status == ContactStatus.ONLINE) {
            holder.statusUser.text = "ONLINE"
            holder.statusUser.setTextColor(Color.GREEN)
        } else {
            holder.statusUser.text = "OFFLINE"
            holder.statusUser.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int = contactsFilter.size

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun newMessageFromUserOrGroup(id: Int, message: String) {
        val contact = contactsFilter.find { it.id == id }

        if (contact != null && contact.exist) {
            contact.lastMessage?.content = message
            contact.lastMessage?.status = 0
            contact.lastMessage?.time = LocalDateTime.now().toString()

            ContactContent.updateItem(contact)

            notifyItemChanged(contact.position)
            notifyItemChanged(contact.position, "payload")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun checkForOnlineUsers(ids: IntArray) {
        contactsFilter.filter { it.type == ContactType.USERS }.forEach { contact ->
            if (contact.status == ContactStatus.OFFLINE && ids.contains(contact.id)) {
                contact.status = ContactStatus.ONLINE
                ContactContent.updateItem(contact)

                notifyItemChanged(contact.position)
                notifyItemChanged(contact.position, "payload")
            } else if (contact.status == ContactStatus.ONLINE && !ids.contains(contact.id)) {
                contact.status = ContactStatus.OFFLINE
                ContactContent.updateItem(contact)

                notifyItemChanged(contact.position)
                notifyItemChanged(contact.position, "payload")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun checkForOnlineGroups(ids: IntArray) {
        contactsFilter.filter { it.type == ContactType.GROUP }.forEach { contact ->
            if (contact.status == ContactStatus.OFFLINE && ids.contains(contact.id)) {
                contact.status = ContactStatus.ONLINE
                ContactContent.updateItem(contact)

                notifyItemChanged(contact.position)
                notifyItemChanged(contact.position, "payload")
            } else if (contact.status == ContactStatus.ONLINE && !ids.contains(contact.id)) {
                contact.status = ContactStatus.OFFLINE
                ContactContent.updateItem(contact)

                notifyItemChanged(contact.position)
                notifyItemChanged(contact.position, "payload")
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                contactsFilter = if (charSearch.isEmpty()) ({
                    contacts
                }) as ArrayList<ContactItemView> else {
                    val resultList = ArrayList<ContactItemView>()

                    for (row in contacts) {
                        if (row.name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }

                val filterResults = FilterResults()
                filterResults.values = contactsFilter
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                contactsFilter = if (results?.values != null)
                    results.values as ArrayList<ContactItemView>
                else
                    contacts as ArrayList<ContactItemView>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(binding: FragmentContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var contactImage: ShapeableImageView = binding.contactImage
        var contactName: TextView = binding.contactName
        var contactLastMessageContent: TextView = binding.contactLastMessageContent
        var contactLastMessageTime: TextView = binding.contactLastMessageTime
        var statusUser: TextView = binding.statusUser

        init {
            contactImage = binding.root.findViewById(R.id.contact_image)
            contactName = binding.root.findViewById(R.id.contact_name)
            contactLastMessageContent = binding.root.findViewById(R.id.contact_last_message_content)
            contactLastMessageTime = binding.root.findViewById(R.id.contact_last_message_time)
            statusUser = binding.root.findViewById(R.id.status_user)
        }
    }
}