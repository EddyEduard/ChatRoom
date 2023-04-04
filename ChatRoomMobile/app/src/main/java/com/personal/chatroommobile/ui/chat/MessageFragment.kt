package com.personal.chatroommobile.ui.chat

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.data.repositories.MessageRepository
import com.personal.chatroommobile.data.source.MessageDataSource
import com.personal.chatroommobile.data.source.RelationshipDataSource
import com.personal.chatroommobile.services.SocketHandler
import com.personal.chatroommobile.ui.chat.placeholder.MessageContent
import com.personal.chatroommobile.ui.contacts.ContactType
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MessageFragment : Fragment() {

    private var columnCount = 1
    private lateinit var currentActivity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            currentActivity = it
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_chat)
        val sendMessage = view.findViewById<ImageButton>(R.id.send_message)
        val newMessage = view.findViewById<TextInputEditText>(R.id.new_message)
        val warning = view.findViewById<CardView>(R.id.warning)

        val handler = Handler(Looper.getMainLooper())
        val socket = SocketHandler.getSocket()

        if (!CacheData.contact.exist)
            warning.visibility = View.VISIBLE

        val messageViewModel = MessageViewModel(
            MessageRepository(
                MessageDataSource(CacheData.token),
                RelationshipDataSource(CacheData.token)
            )
        )

        messageViewModel.messages.observe(viewLifecycleOwner, Observer {
            val messages = it ?: return@Observer

            if (recyclerView is RecyclerView) {
                with(view) {
                    recyclerView.layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }

                    MessageContent.foundMessages(messages)

                    val messageRecyclerView =
                        MessageRecyclerViewAdapter(currentActivity,
                            MessageContent.ITEMS as ArrayList<MessageItemView>)

                    recyclerView.adapter = messageRecyclerView
                    recyclerView.scrollToPosition(messageRecyclerView.itemCount - 1)

                    socket.on("ReceiveMessageFromUser", { _: Int, message: String, _: String ->
                        handler.post {
                            messageRecyclerView.receiveMessage(CacheData.contact.image,
                                CacheData.contact.name,
                                message)

                            recyclerView.scrollToPosition(messageRecyclerView.itemCount - 1)

                            socket.invoke("SeenMessagesFromUser",
                                CacheData.userId,
                                CacheData.contact.id)
                        }
                    }, Int::class.java, String::class.java, String::class.java)

                    socket.on("ReceiveMessageFromGroup",
                        { userId: Int, _: Int, message: String, _: String, name: String, image: String ->
                            if (userId != CacheData.userId) {
                                handler.post {
                                    messageRecyclerView.receiveMessage(image,
                                        name,
                                        message)

                                    recyclerView.scrollToPosition(messageRecyclerView.itemCount - 1)

                                    socket.invoke("SeenMessagesFromUser",
                                        CacheData.userId,
                                        CacheData.contact.id)
                                }
                            }
                        },
                        Int::class.java,
                        Int::class.java,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        String::class.java)

                    socket.on("ReceiveSeenMessagesFromUser", {
                        handler.post {
                            handler.post {
                                messageRecyclerView.markAsSeenMessages()
                            }
                        }
                    }, Int::class.java)

                    socket.invoke("SeenMessagesFromUser", CacheData.userId, CacheData.contact.id)

                    sendMessage.setOnClickListener {
                        val message = newMessage.text.toString()

                        if (message.isNotEmpty()) {
                            messageViewModel.sendingMessageResult.observe(viewLifecycleOwner) { it_ ->
                                if (it_ != "false") {
                                    if (CacheData.contact.type == ContactType.USERS) {
                                        socket.invoke("SendMessageToUser",
                                            CacheData.userId,
                                            CacheData.contact.id,
                                            it_,
                                            LocalDateTime.now().toString())
                                    } else {
                                        socket.invoke("SendMessageToGroup",
                                            CacheData.userId,
                                            CacheData.contact.id,
                                            it_,
                                            LocalDateTime.now().toString(),
                                            CacheData.profile.name,
                                            CacheData.profile.image)
                                    }
                                    messageRecyclerView.sendMessage(it_)
                                    recyclerView.scrollToPosition(messageRecyclerView.itemCount - 1)
                                    newMessage.text = null

                                    if (warning.visibility == View.VISIBLE)
                                        warning.visibility = View.GONE
                                } else
                                    Toast.makeText(currentActivity,
                                        "The message could not be sent.",
                                        Toast.LENGTH_SHORT).show()
                            }

                            lifecycleScope.launch {
                                messageViewModel.sendMessage(CacheData.contact,
                                    message)
                            }
                        }
                    }
                }
            }
        })

        lifecycleScope.launch {
            messageViewModel.messages(CacheData.contact, CacheData.userId)
        }

        return view
    }
}
