package com.personal.chatroommobile.ui.contacts

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ContactViewModel(private val relationshipRepository: RelationshipRepository) : ViewModel() {

    private val _contactsResult = MutableLiveData<ArrayList<ContactItemView>>()
    var contacts: LiveData<ArrayList<ContactItemView>> = _contactsResult

    /**
     * Get users.
     *
     * @param contacts
     * @param userId
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    fun users(contacts: ArrayList<ContactItemView>, userId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.users()

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            val contactList: ArrayList<ContactItemView> = ArrayList()
                            var position = 0

                            result.data.forEach {
                                if (it.id != userId) {
                                    if (contacts.find { it_ -> it_.id == it.id } == null) {
                                        contactList.add(ContactItemView(
                                            it.id,
                                            it.name,
                                            it.image,
                                            MessageItemView(-1, it.email, -1, ""),
                                            ContactType.USERS,
                                            false,
                                            ContactStatus.OFFLINE,
                                            position
                                        ))

                                        position++
                                    }
                                }
                            }

                            _contactsResult.value = contactList
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Get contacts.
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    fun contacts(userId: Int) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.contacts()

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            val contactList: ArrayList<ContactItemView> = ArrayList()
                            var position = 0

                            result.data.first.forEach {
                                val dateString =
                                    (if (it.lastMessage != null) it.lastMessage.dateTime else Date().toString()).replace(
                                        "T",
                                        " ").replace("Z", "").split(".")[0]
                                val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val localDateTime = LocalDateTime.parse(dateString, pattern)
                                val time =
                                    SimpleDateFormat("HH:mm").format(Date.from(localDateTime.atZone(
                                        ZoneId.systemDefault()).toInstant()))

                                contactList.add(ContactItemView(
                                    it.id,
                                    it.name,
                                    it.image,
                                    MessageItemView(
                                        if (it.lastMessage != null && it.lastMessage.content != "null") it.lastMessage.idUserFrom else -1,
                                        if (it.lastMessage != null && it.lastMessage.content != "null") it.lastMessage.content else "No message yet!",
                                        if (it.lastMessage != null && it.lastMessage.content != "null") it.lastMessage.status else -1,
                                        if (it.lastMessage != null && it.lastMessage.content != "null") time else "",
                                    ),
                                    ContactType.USERS,
                                    true,
                                    ContactStatus.OFFLINE,
                                    position
                                ))

                                position++
                            }

                            result.data.second.forEach {
                                val dateString =
                                    (if (it.lastMessage != null) it.lastMessage.dateTime else Date().toString()).replace(
                                        "T",
                                        " ").replace("Z", "")
                                        .split(".")[0]
                                val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val localDateTime = LocalDateTime.parse(dateString, pattern)
                                val time =
                                    SimpleDateFormat("HH:mm").format(Date.from(localDateTime.atZone(
                                        ZoneId.systemDefault()).toInstant()))

                                contactList.add(ContactItemView(
                                    it.id,
                                    it.name,
                                    it.image,
                                    MessageItemView(
                                        if (it.lastMessage != null && it.lastMessage.content != "null") it.lastMessage.idUser else -1,
                                        if (it.lastMessage != null && it.lastMessage.content != "null") it.lastMessage.content else "No message yet!",
                                        if (it.lastMessage != null && it.lastMessage.content != "null") if (it.lastMessage.seenMembers.indexOf(
                                                userId.toString()) == -1
                                        ) 0 else 1 else -1,
                                        if (it.lastMessage != null && it.lastMessage.content != "null") time else "",
                                    ),
                                    ContactType.GROUP,
                                    true,
                                    ContactStatus.OFFLINE,
                                    position
                                ))

                                position++
                            }

                            _contactsResult.value = contactList
                        }
                    }
                }.invoke()
            }
        }
    }
}