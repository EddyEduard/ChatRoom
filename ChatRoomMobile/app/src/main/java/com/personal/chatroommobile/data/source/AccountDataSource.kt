package com.personal.chatroommobile.data.source

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.Group
import com.personal.chatroommobile.data.model.User
import org.json.JSONObject
import java.io.File
import java.io.IOException
import com.github.kittinunf.result.Result as ResultFuel

class AccountDataSource(private val token: String) {

    init {
        FuelManager.instance.basePath = "https://chatroomfree.somee.com/api"
    }

    /**
     * Get user profile.
     *
     * @return user profile
     * */
    fun profile(): Result<Pair<User, ArrayList<Group>>> {
        try {
            val result = Fuel.get("/account")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val userObject = body.getJSONObject("user")
                    val groupList = body.getJSONArray("groups")
                    val groups: ArrayList<Group> = ArrayList()

                    val user = User(
                        userObject.getInt("id"),
                        userObject.getString("name"),
                        userObject.getString("email"),
                        userObject.getString("image"),
                        null
                    )

                    for (i in 0 until groupList.length()) {
                        val group = JSONObject(groupList.get(i).toString())

                        groups.add(Group(
                            group.getInt("id"),
                            group.getInt("idAdminUser"),
                            group.getString("name"),
                            group.getString("image"),
                            null
                        ))
                    }

                    Result.Success(Pair(user, groups))
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error getting profile."))
                }
            }
        } catch (e: Throwable) {
            println(e.message)
            return Result.Error(IOException("Error getting profile.", e))
        }
    }

    /**
     * Update profile.
     *
     * @param username
     * @param email
     * */
    fun updateProfile(username: String, email: String): Result<String> {
        try {
            val data = JSONObject()
            data.put("name", username)
            data.put("email", email)

            val result = Fuel.put("/account")
                .header(Headers.AUTHORIZATION, "Bearer $token")
                .header(Headers.CONTENT_TYPE, "application/json")
                .body(data.toString())
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
                    Result.Error(Exception("Error updating profile."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error updating profile.", e))
        }
    }

    /**
     * Update profile image.
     *
     * @param imagePath
     * */
    fun updateProfileImage(imagePath: String): Result<Boolean> {
        try {
            val image = FileDataPart(File(imagePath), "image")
            val result =
                Fuel.upload("/account/profile-image", Method.PUT)
                    .add(image)
                    .header(Headers.AUTHORIZATION, "Bearer $token")
                    .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    Result.Success(true)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    println(body)
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Error uploading image."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error uploading image.", e))
        }
    }

    /**
     * Delete account.
     * */
    fun deleteAccount(): Result<String> {
        try {
            val result = Fuel.delete("/account")
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
                    return Result.Error(Exception("Error deleting account."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error deleting account.", e))
        }
    }
}