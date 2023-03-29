package com.personal.chatroommobile.data.source

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.LoggedInUser
import org.json.JSONObject
import java.io.IOException
import com.github.kittinunf.result.Result as ResultFuel

class AuthDataSource {

    init {
        FuelManager.instance.basePath = "http://chatroomfree.somee.com/api"
    }

    /**
     * Login user.
     *
     * @param email
     * @param password
     * @return User data
     * */
    fun login(email: String, password: String): Result<LoggedInUser> {
        try {
            val credentials = JSONObject()
            credentials.put("email", email)
            credentials.put("password", password)

            val result = Fuel.post("/auth/login")
                .header(Headers.CONTENT_TYPE, "application/json")
                .body(credentials.toString())
                .response().third

            return when (result) {
                is ResultFuel.Success -> {
                    val body = JSONObject(result.value.toString(Charsets.UTF_8))
                    val user = LoggedInUser(
                        body.get("user_id").toString(),
                        body.get("name").toString(),
                        body.get("token").toString())
                    Result.Success(user)
                }

                is ResultFuel.Failure -> {
                    val body = JSONObject(result.error.errorData.toString(Charsets.UTF_8))
                    Result.Warning(body.get("message").toString())
                }

                else -> {
                    Result.Error(Exception("Login failed."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Login failed.", e))
        }
    }

    /**
     * Registration user.
     *
     * @param username
     * @param email
     * @param password
     * @return User data
     * */
    fun register(username: String, email: String, password: String): Result<String> {
        try {
            val credentials = JSONObject()
            credentials.put("name", username)
            credentials.put("email", email)
            credentials.put("password", password)

            val result = Fuel.post("/auth/register")
                .header(Headers.CONTENT_TYPE, "application/json")
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
                    Result.Error(Exception("Registration failed."))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Registration failed.", e))
        }
    }
}