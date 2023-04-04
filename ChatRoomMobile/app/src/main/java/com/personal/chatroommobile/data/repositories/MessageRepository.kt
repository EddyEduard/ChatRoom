package com.personal.chatroommobile.data.repositories

import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.MessageGroup
import com.personal.chatroommobile.data.model.MessageUser
import com.personal.chatroommobile.data.source.MessageDataSource
import com.personal.chatroommobile.data.source.RelationshipDataSource

class MessageRepository(
    private val messageDataSource: MessageDataSource,
    private val relationshipDataSource: RelationshipDataSource,
) {

    /**
     * Get messages from a relationship between two users.
     *
     * @param userId
     * @return messages
     * */
    fun messagesUser(userId: Int): Result<ArrayList<MessageUser>> {
        val messages = messageDataSource.messagesUser(userId)

        if (messages is Result.Success)
            messageDataSource.markAsSeenMessagesFromUsers(userId)

        return messages
    }

    /**
     * Get messages from a group.
     *
     * @param groupId
     * @return messages
     * */
    fun messagesGroup(groupId: Int): Result<ArrayList<MessageGroup>> {
        val messages = messageDataSource.messagesGroup(groupId)

        if (messages is Result.Success)
            messageDataSource.markAsSeenMessagesFromGroup(groupId)

        return messages
    }

    /**
     * Send message to a user.
     *
     * @param userId
     * @param message
     * @param existRelationship
     * @return messages
     * */
    fun sendMessageToUser(
        userId: Int,
        message: String,
        existRelationship: Boolean = true,
    ): Result<Boolean> {
        if (!existRelationship)
            relationshipDataSource.createContact(userId)

        return messageDataSource.sendMessageToUser(userId, message)
    }

    /**
     * Send message to a group.
     *
     * @param groupId
     * @param message
     * @return messages
     * */
    fun sendMessageToGroup(groupId: Int, message: String): Result<Boolean> {
        return messageDataSource.sendMessageToGroup(groupId, message)
    }
}