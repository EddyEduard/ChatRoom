package com.personal.chatroommobile.data.source

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.MessageGroup
import com.personal.chatroommobile.data.model.MessageUser
import com.personal.chatroommobile.data.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.github.kittinunf.result.Result as ResultFuel

class MessageDataSource(private var token: String) {

    init {
        FuelManager.instance.basePath = "https://chatroomfree.somee.com/api"
    }

    /**
     * Get messages from a relationship between two users.
     *
     * @param userId
     * @return messages
     * */
    fun messagesUser(userId: Int): Result<ArrayList<MessageUser>> {
        try {
            val result = Fuel.get("/message/${userId}")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONArray(result.value.toString(Charsets.UTF_8))
                    val messages: ArrayList<MessageUser> = ArrayList()

                    for (i in 0 until body.length()) {
                        val message = JSONObject(body.get(i).toString())

                        messages.add(MessageUser(
                            message.getInt("id"),
                            message.getInt("id_user_from"),
                            message.getInt("id_user_to"),
                            message.get("content").toString(),
                            message.getInt("status"),
                            message.get("date_time").toString()
                        ))
                    }

                    Result.Success(messages)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting messages."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting messages.", e))
        }
    }

    /**
     * Get messages from a group.
     *
     * @param groupId
     * @return messages
     * */
    fun messagesGroup(groupId: Int): Result<ArrayList<MessageGroup>> {
        try {
            val result = Fuel.get("/message/group/${groupId}")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONArray(result.value.toString(Charsets.UTF_8))
                    val messages: ArrayList<MessageGroup> = ArrayList()

                    for (i in 0 until body.length()) {
                        val message = JSONObject(body.get(i).toString())
                        val user = message.getJSONObject("user")

                        messages.add(MessageGroup(
                            message.getInt("id"),
                            message.getInt("id_user"),
                            message.getInt("id_group"),
                            message.get("content").toString(),
                            message.getInt("status"),
                            message.getString("seen_members"),
                            message.get("date_time").toString(),
                            User(
                                user.getInt("id"),
                                user.getString("name"),
                                user.getString("email"),
                                user.getString("image"),
                                null
                            )
                        ))
                    }

                    Result.Success(messages)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting messages."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting messages.", e))
        }
    }

    /**
     * Send message to a user.
     *
     * @param userId
     * @param message
     * */
    fun sendMessageToUser(userId: Int, message: String): Result<Boolean> {
        try {
            val credentials = JSONObject()
            credentials.put("user_id", userId)
            credentials.put("message", message)

            val result = Fuel.post("/message")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    Result.Success(true)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The message could not be sent."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The message could not be sent.", e))
        }
    }

    /**
     * Mark as seen all messages from a conversation between two users.
     *
     * @param userId
     * */
    fun markAsSeenMessagesFromUsers(userId: Int): Result<Boolean> {
        try {
            val result = Fuel.put("/message/${userId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    Result.Success(true)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The messages could not be mark as seen."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The messages could not be mark as seen.", e))
        }
    }

    /**
     * Send message to a group.
     *
     * @param groupId
     * @param message
     * */
    fun sendMessageToGroup(groupId: Int, message: String): Result<Boolean> {
        try {
            val credentials = JSONObject()
            credentials.put("group_id", groupId)
            credentials.put("message", message)

            val result = Fuel.post("/message/group")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    Result.Success(true)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The message could not be sent."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The message could not be sent.", e))
        }
    }

    /**
     * Mark as seen all messages from a group for specific member.
     *
     * @param groupId
     * */
    fun markAsSeenMessagesFromGroup(groupId: Int): Result<Boolean> {
        try {
            val result = Fuel.put("/message/group/${groupId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    Result.Success(true)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The messages could not be mark as seen."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The messages could not be mark as seen.", e))
        }
    }
}