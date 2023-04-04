package com.personal.chatroommobile.ui.chat

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.repositories.MessageRepository
import com.personal.chatroommobile.ui.contacts.ContactItemView
import com.personal.chatroommobile.ui.contacts.ContactType
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

open class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }
}

class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    private val _messagesResult = MutableLiveData<ArrayList<MessageItemView>>()
    var messages: LiveData<ArrayList<MessageItemView>> = _messagesResult

    private val _sendingMessageResult = SingleLiveEvent<String>()
    var sendingMessageResult: SingleLiveEvent<String> = _sendingMessageResult

    /**
     * Get messages.
     *
     * @param contact
     * */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    fun messages(contact: ContactItemView, userId: Int) {

        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    if (contact.type == ContactType.USERS) {
                        val result = messageRepository.messagesUser(contact.id)

                        withContext(Dispatchers.Main) {
                            if (result is Result.Success) {
                                val messageList: ArrayList<MessageItemView> = ArrayList()
                                var position = 0

                                result.data.forEach {
                                    messageList.add(MessageItemView(
                                        if (it.idUserTo != contact.id) contact.image else "",
                                        if (it.idUserTo != contact.id) contact.name else "",
                                        it.content,
                                        it.status,
                                        it.dateTime,
                                        if (it.idUserFrom == userId) MessageType.SENT else MessageType.RECEIVE,
                                        position
                                    ))

                                    position++
                                }

                                _messagesResult.value = messageList
                            } else if (result is Result.Warning)
                                _messagesResult.value = ArrayList()
                        }
                    } else if (contact.type == ContactType.GROUP) {
                        val result = messageRepository.messagesGroup(contact.id)

                        withContext(Dispatchers.Main) {
                            if (result is Result.Success) {
                                val messageList: ArrayList<MessageItemView> = ArrayList()
                                var position = 0

                                result.data.forEach {
                                    messageList.add(MessageItemView(
                                        if (it.user != null) it.user.image else "",
                                        if (it.user != null) it.user.name else "",
                                        it.content,
                                        it.status,
                                        it.dateTime,
                                        if (it.idUser == userId) MessageType.SENT else MessageType.RECEIVE,
                                        position
                                    ))

                                    position++
                                }

                                _messagesResult.value = messageList
                            }
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Send a message
     *
     * @param contact
     * @param message
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun sendMessage(contact: ContactItemView, message: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    if (contact.type == ContactType.USERS) {
                        val result =
                            messageRepository.sendMessageToUser(contact.id, message, contact.exist)

                        withContext(Dispatchers.Main) {
                            if (result is Result.Success) {
                                if (!contact.exist)
                                    contact.exist = true

                                _sendingMessageResult.value = message
                            } else {
                                if (result is Result.Warning)
                                    _sendingMessageResult.value = "false"
                                else
                                    _sendingMessageResult.value = "false"
                            }
                        }
                    } else if (contact.type == ContactType.GROUP) {
                        val result = messageRepository.sendMessageToGroup(contact.id, message)

                        withContext(Dispatchers.Main) {
                            if (result is Result.Success) {
                                _sendingMessageResult.value = message
                            } else {
                                if (result is Result.Warning)
                                    _sendingMessageResult.value = "false"
                                else
                                    _sendingMessageResult.value = "false"
                            }
                        }
                    }
                }.invoke()
            }
        }
    }
}