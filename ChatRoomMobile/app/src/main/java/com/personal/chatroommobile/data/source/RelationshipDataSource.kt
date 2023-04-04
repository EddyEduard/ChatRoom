package com.personal.chatroommobile.data.source

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.github.kittinunf.result.Result as ResultFuel

class RelationshipDataSource(private val token: String) {

    init {
        FuelManager.instance.basePath = "https://chatroomfree.somee.com/api"
    }

    /**
     * Get users.
     *
     * @return users
     * */
    fun users(): Result<ArrayList<User>> {
        try {
            val result = Fuel.get("/relationship/users")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONArray(result.value.toString(Charsets.UTF_8))
                    val users: ArrayList<User> = ArrayList()

                    for (i in 0 until body.length()) {
                        val user = JSONObject(body.get(i).toString())

                        users.add(User(
                            user.getInt("id"),
                            user.get("name").toString(),
                            user.get("email").toString(),
                            user.get("image").toString(),
                            null
                        ))
                    }

                    Result.Success(users)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting users."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting users.", e))
        }
    }

    /**
     * Get contacts.
     *
     * @return contacts
     * */
    fun contacts(): Result<Pair<ArrayList<User>, ArrayList<Group>>> {
        try {
            val result = Fuel.get("/relationship/contacts")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val userList = body.getJSONArray("users")
                    val groupList = body.getJSONArray("groups")
                    val users: ArrayList<User> = ArrayList()
                    val groups: ArrayList<Group> = ArrayList()

                    for (i in 0 until userList.length()) {
                        val user = JSONObject(userList.get(i).toString())
                        val message = user.getJSONObject("last_message")

                        users.add(User(
                            user.getInt("id"),
                            user.get("name").toString(),
                            user.get("email").toString(),
                            user.get("image").toString(),
                            MessageUser(
                                message.getInt("id"),
                                message.getInt("id_user_from"),
                                message.getInt("id_user_to"),
                                message.get("content").toString(),
                                message.getInt("status"),
                                message.get("date_time").toString()
                            )
                        ))
                    }

                    for (i in 0 until groupList.length()) {
                        val group = JSONObject(groupList.get(i).toString())
                        val message = group.getJSONObject("last_message")

                        groups.add(Group(
                            group.getInt("id"),
                            group.getInt("idAdminUser"),
                            group.get("name").toString(),
                            group.get("image").toString(),
                            MessageGroup(
                                message.getInt("id"),
                                message.getInt("id_user"),
                                message.getInt("id_group"),
                                message.get("content").toString(),
                                message.getInt("status"),
                                message.get("seen_members").toString(),
                                message.get("date_time").toString(),
                                null
                            )
                        ))
                    }

                    Result.Success(Pair(users, groups))
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting contacts."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting contacts.", e))
        }
    }

    /**
     * Create contact.
     *
     * @param userId
     * */
    fun createContact(userId: Int): Result<Boolean> {
        try {
            val credentials = JSONObject()
            credentials.put("user_id", userId)

            val result = Fuel.post("/relationship/contact")
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
                    Result.Error(Exception("The relationship could not be create."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The relationship could not be create.", e))
        }
    }

    /**
     * Get members.
     *
     * @param groupId
     * @return members
     * */
    fun members(groupId: Int): Result<ArrayList<User>> {
        try {
            val result = Fuel.get("/relationship/group/${groupId}")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONArray(result.value.toString(Charsets.UTF_8))
                    val members: ArrayList<User> = ArrayList()

                    for (i in 0 until body.length()) {
                        val member = JSONObject(body.get(i).toString())

                        members.add(User(
                            member.getInt("id"),
                            member.get("name").toString(),
                            member.get("email").toString(),
                            member.get("image").toString(),
                            null
                        ))
                    }

                    Result.Success(members)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting members."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting members.", e))
        }
    }

    /**
     * Create group.
     *
     * @param name
     * @return group
     * */
    fun createGroup(name: String): Result<Group> {
        try {
            val credentials = JSONObject()
            credentials.put("name", name)

            val result = Fuel.post("/relationship/group")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val group = Group(
                        body.getInt("id"),
                        body.getInt("idAdminUser"),
                        body.getString("name"),
                        body.getString("image"),
                        null
                    )

                    Result.Success(group)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The group could not be create."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The group could not be create.", e))
        }
    }

    /**
     * Update profile group.
     *
     * @param groupId
     * @param name
     * */
    fun updateProfileGroup(groupId: Int, name: String): Result<String> {
        try {
            val credentials = JSONObject()
            credentials.put("name", name)

            val result = Fuel.put("/relationship/group/${groupId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val message = body.get("message").toString()
                    Result.Success(message)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The profile group could not be updating."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The profile group could not be updating.", e))
        }
    }

    /**
     * Update profile group image.
     *
     * @param groupId
     * @param imagePath
     * */
    fun updateProfileGroupImage(groupId: Int, imagePath: String): Result<Boolean> {
        try {
            val file = FileDataPart.from(imagePath)
            val result =
                Fuel.upload("/relationship/group/${groupId}",
                    Method.PUT)
                    .add(file)
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
                    Result.Error(Exception("The profile group image could not be updating."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The profile group image could not be updating.", e))
        }
    }

    /**
     * Add a new member into a group.
     *
     * @param groupId
     * @param userId
     * */
    fun addMemberToGroup(groupId: Int, userId: Int): Result<String> {
        try {
            val credentials = JSONObject()
            credentials.put("user_id", userId)

            val result = Fuel.post("/relationship/group/${groupId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val message = body.get("message").toString()
                    Result.Success(message)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("The member could not be added to the group."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("The member could not be added to the group.", e))
        }
    }

    /**
     * Remove a member from group.
     *
     * @param groupId
     * @param userId
     * */
    fun removeMemberFromGroup(groupId: Int, userId: Int): Result<String> {
        try {
            val result = Fuel.delete("/relationship/group/${groupId}/${userId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val message = body.get("message").toString()
                    Result.Success(message)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Could not delete the member from the group."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Could not delete the member from the group.", e))
        }
    }

    /**
     * Delete a group.
     *
     * @param groupId
     * */
    fun deleteGroup(groupId: Int): Result<String> {
        try {
            val result = Fuel.delete("/relationship/group/${groupId}")
                .header(Headers.CONTENT_TYPE, "application/json")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val message = body.get("message").toString()
                    Result.Success(message)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Could not delete the group."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Could not delete the group.", e))
        }
    }
}