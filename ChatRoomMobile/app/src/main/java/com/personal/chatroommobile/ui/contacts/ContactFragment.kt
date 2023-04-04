package com.personal.chatroommobile.ui.contacts

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import android.widget.SearchView
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.CacheData
import com.personal.chatroommobile.ui.contacts.placeholder.ContactContent
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import com.personal.chatroommobile.data.source.RelationshipDataSource
import com.personal.chatroommobile.services.SocketHandler
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ContactFragment(private val loadAllUsers: Boolean = false) : Fragment() {

    private var columnCount = 1
    private lateinit var currentActivity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            currentActivity = it
        }
    }

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact_list, container, false)
        val contactsLoading = view.findViewById<ProgressBar>(R.id.loading_contacts)
        val contactsSearch = view.findViewById<SearchView>(R.id.contacts_search)
        val recyclerView = view.findViewById<RecyclerView>(R.id.contacts_list)

        val handler = Handler(Looper.getMainLooper())
        val socket = SocketHandler.getSocket()

        val contactViewModel = ContactViewModel(
            RelationshipRepository(
                RelationshipDataSource(CacheData.token)
            )
        )

        contactViewModel.contacts.observe(viewLifecycleOwner, Observer {
            val contacts = it ?: return@Observer

            if (recyclerView is RecyclerView) {
                with(view) {
                    recyclerView.layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }

                    ContactContent.foundContacts(contacts)

                    val contactRecyclerView =
                        ContactRecyclerViewAdapter(currentActivity,
                            ContactContent.ITEMS)

                    recyclerView.adapter = contactRecyclerView
                    contactsLoading.visibility = View.GONE
                    contactsSearch.visibility = View.VISIBLE

                    socket.on("ReceiveMessageFromUser", { userId: Int, message: String, _: String ->
                        handler.post {
                            contactRecyclerView.newMessageFromUserOrGroup(userId, message)
                        }
                    }, Int::class.java, String::class.java, String::class.java)

                    socket.on("ReceiveMessageFromGroup",
                        { _: Int, groupId: Int, message: String, _: String, _: String, _: String ->
                            println(groupId)
                            handler.post {
                                contactRecyclerView.newMessageFromUserOrGroup(groupId, message)
                            }
                        },
                        Int::class.java,
                        Int::class.java,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        String::class.java)

                    socket.on("OnlineUsers", { it_ ->
                        handler.post {
                            contactRecyclerView.checkForOnlineUsers(it_)
                        }
                    }, IntArray::class.java)

                    socket.on("OnlineGroups", { it_ ->
                        handler.post {
                            contactRecyclerView.checkForOnlineGroups(it_)
                        }
                    }, IntArray::class.java)

                    if (!loadAllUsers) {
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                socket.invoke("ProvideConnectedUsersAndGroups")
                            }
                        }, 1000)
                    }

                    contactsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(text: String?): Boolean {
                            contactRecyclerView.filter.filter(text)
                            return false
                        }
                    })
                }
            }
        })

        lifecycleScope.launch {
            if (loadAllUsers)
                contactViewModel.users(ContactContent.ITEMS as ArrayList<ContactItemView>,
                    CacheData.userId)
            else
                contactViewModel.contacts(CacheData.userId)
        }

        return view
    }
}