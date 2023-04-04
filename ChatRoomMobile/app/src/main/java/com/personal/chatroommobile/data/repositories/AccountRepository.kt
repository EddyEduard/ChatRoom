package com.personal.chatroommobile.data.repositories

import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.model.Group
import com.personal.chatroommobile.data.model.User
import com.personal.chatroommobile.data.source.AccountDataSource

class AccountRepository(private val accountDataSource: AccountDataSource) {

    /**
     * Get user profile.
     *
     * @return user profile
     * */
    fun profile(): Result<Pair<User, ArrayList<Group>>> {
        return accountDataSource.profile()
    }

    /**
     * Update profile.
     *
     * @param username
     * @param email
     * @param imagePath
     * */
    fun updateProfile(username: String, email: String, imagePath: String): Result<String> {
        if (imagePath.isNotEmpty())
            accountDataSource.updateProfileImage(imagePath)

        return accountDataSource.updateProfile(username, email)
    }

    /**
     * Delete account.
     * */
    fun deleteAccount(): Result<String> {
        return accountDataSource.deleteAccount()
    }
}