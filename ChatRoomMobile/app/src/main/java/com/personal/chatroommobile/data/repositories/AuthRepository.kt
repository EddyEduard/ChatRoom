package com.personal.chatroommobile.data.repositories

import com.personal.chatroommobile.data.model.LoggedInUser
import com.personal.chatroommobile.data.source.AuthDataSource
import com.personal.chatroommobile.data.Result

class AuthRepository(private val dataSource: AuthDataSource) {

    /**
     * Login user.
     *
     * @param email
     * @param password
     * @return User data
     * */
    fun login(email: String, password: String): Result<LoggedInUser> {
        return dataSource.login(email, password)
    }

    /**
     * Registration user.
     *
     * @param username
     * @param email
     * @param password
     * */
    fun register(username: String, email: String, password: String): Result<String> {
        return dataSource.register(username, email, password)
    }
}