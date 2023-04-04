package com.personal.chatroommobile.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.personal.chatroommobile.R
import com.personal.chatroommobile.ui.chat.placeholder.MessageContent
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class MessageRecyclerViewAdapter(
    private val context: Context,
    private val messages: ArrayList<MessageItemView>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewTypeMessageSent = 1
    private val viewTypeMessageReceived = 2

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return if (message.type == MessageType.SENT) {
            viewTypeMessageSent
        } else {
            viewTypeMessageReceived
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View

        return if (viewType == viewTypeMessageSent) {
            view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_message_sent,
                        parent,
                        false)
            SentMessageHolder(view)
        } else {
            view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.fragment_message_received,
                        parent,
                        false)
            ReceivedMessageHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = messages[position]
        val previewItem = if (position - 1 >= 0) messages[position - 1] else null

        when (holder.itemViewType) {
            viewTypeMessageSent -> (holder as SentMessageHolder).bind(item)
            viewTypeMessageReceived -> (holder as ReceivedMessageHolder).bind(context,
                item,
                previewItem,
                messages.count { it.type == MessageType.RECEIVE && it.status == 0 })
        }
    }

    override fun getItemCount(): Int = messages.size

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun receiveMessage(image: String, name: String, message: String) {
        MessageContent.addItem(MessageItemView(image,
            name,
            message,
            1,
            LocalDateTime.now().toString(),
            MessageType.RECEIVE,
            MessageContent.ITEMS.size))
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(message: String) {
        MessageContent.addItem(MessageItemView("",
            "",
            message,
            0,
            LocalDateTime.now().toString(),
            MessageType.SENT,
            MessageContent.ITEMS.size))
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun markAsSeenMessages() {
        MessageContent.ITEMS.filter { it.status == 0 }.forEach {
            it.status = 1
        }
        notifyDataSetChanged()
    }

    private class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageFromContent: TextView
        var messageFromTime: TextView
        var messageStatus: TextView

        fun bind(message: MessageItemView) {
            messageFromContent.text = message.content
            messageFromTime.text = message.time
            messageStatus.text = if (message.status == 1) "Seen" else "Unseen"
        }

        init {
            messageFromContent =
                itemView.findViewById(R.id.message_from_content)
            messageFromTime =
                itemView.findViewById(R.id.message_from_time)
            messageStatus =
                itemView.findViewById(R.id.message_status)
        }
    }

    private class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageToImage: ImageView
        var messageToName: TextView
        var messageToContent: TextView
        var messageToTime: TextView
        var unreadMessagesPopup: CardView
        var unreadMessage: TextView

        @SuppressLint("SetTextI18n")
        fun bind(
            context: Context,
            message: MessageItemView,
            previewMessage: MessageItemView?,
            countUnreadMessages: Int,
        ) {
            Glide.with(context)
                .load(message.image)
                .into(messageToImage)

            messageToName.text = message.name
            messageToContent.text = message.content
            messageToTime.text = message.time
            unreadMessagesPopup.visibility = View.GONE
            unreadMessage.text = "0 UNREAD MESSAGES"

            if (message.status == 0 && (previewMessage != null && previewMessage.status == 1)) {
                unreadMessagesPopup.visibility = View.VISIBLE
                unreadMessage.text = "$countUnreadMessages UNREAD MESSAGES"
            }
        }

        init {
            messageToImage =
                itemView.findViewById(R.id.message_to_image)
            messageToName =
                itemView.findViewById(R.id.message_to_name)
            messageToContent =
                itemView.findViewById(R.id.message_to_content)
            messageToTime =
                itemView.findViewById(R.id.message_to_time)
            unreadMessagesPopup =
                itemView.findViewById(R.id.unread_messages_popup)
            unreadMessage =
                itemView.findViewById(R.id.unread_message)
        }
    }
}