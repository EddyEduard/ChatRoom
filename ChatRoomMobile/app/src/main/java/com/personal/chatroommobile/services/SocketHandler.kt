package com.personal.chatroommobile.services

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import java.net.URISyntaxException

object SocketHandler {

    private lateinit var socketHandler: HubConnection

    @Synchronized
    fun setSocket(uri: String) {
        try {
            socketHandler = HubConnectionBuilder.create(uri).build();
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): HubConnection {
        return socketHandler
    }

    @Synchronized
    fun establishConnection() {
        socketHandler.start().blockingAwait()
    }

    @Synchronized
    fun closeConnection() {
        socketHandler.stop()
    }
}