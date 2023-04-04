package com.personal.chatroommobile.ui.account

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.repositories.AccountRepository
import kotlinx.coroutines.*
import com.personal.chatroommobile.data.Result
import com.personal.chatroommobile.data.repositories.RelationshipRepository
import kotlin.collections.ArrayList

class AccountViewModel(
    private val accountRepository: AccountRepository,
    private val relationshipRepository: RelationshipRepository,
) : ViewModel() {

    private val _profileResult = MutableLiveData<ProfileView>()
    var profile: LiveData<ProfileView> = _profileResult

    private val _accountFormState = MutableLiveData<AccountFormState>()
    val accountFormState: LiveData<AccountFormState> = _accountFormState

    private val _accountChangeResult = MutableLiveData<AccountChangeResult>()
    val accountChangeResult: LiveData<AccountChangeResult> = _accountChangeResult

    private val _createGroupResult = MutableLiveData<GroupItemView>()
    val createGroupResult: LiveData<GroupItemView> = _createGroupResult

    /**
     * Get user profile.
     *
     * @return user profile
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun profile() {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = accountRepository.profile()

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            val groups: ArrayList<GroupItemView> = ArrayList()
                            var position = 0

                            result.data.second.forEach {
                                groups.add(GroupItemView(it.id, it.name, it.image, position))
                                position++
                            }

                            _profileResult.value =
                                ProfileView(result.data.first.image,
                                    result.data.first.name,
                                    result.data.first.email,
                                    groups)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Update profile.
     *
     * @param username
     * @param email
     * @param imagePath
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun updateProfile(username: String, email: String, imagePath: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = accountRepository.updateProfile(username, email, imagePath)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _accountChangeResult.value = AccountChangeResult(success = result.data,
                                isForChangeDataAccount = true)
                        } else {
                            if (result is Result.Warning)
                                _accountChangeResult.value =
                                    AccountChangeResult(warning = result.warning,
                                        isForChangeDataAccount = true)
                            else
                                _accountChangeResult.value =
                                    AccountChangeResult(error = R.string.update_profile_failed,
                                        isForChangeDataAccount = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Create group.
     *
     * @param name
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun createGroup(name: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = relationshipRepository.createGroup(name)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _createGroupResult.value =
                                GroupItemView(result.data.id, result.data.name, result.data.image, -1)
                        } else {
                            if (result is Result.Warning)
                                _accountChangeResult.value = AccountChangeResult(isForCreateGroup = true, warning = result.warning)
                            else if (result is Result.Error)
                                _accountChangeResult.value = AccountChangeResult(isForCreateGroup = true, error = R.string.create_group_failed)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Delete account.
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun deleteAccount() {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = accountRepository.deleteAccount()

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _accountChangeResult.value = AccountChangeResult(success = result.data,
                                isForDeleteAccount = true)
                        } else {
                            if (result is Result.Warning)
                                _accountChangeResult.value =
                                    AccountChangeResult(warning = result.warning,
                                        isForDeleteAccount = true)
                            else
                                _accountChangeResult.value =
                                    AccountChangeResult(error = R.string.delete_account_failed,
                                        isForDeleteAccount = true)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Validate account data when is changed.
     *
     * @param username
     * @param email
     * */
    fun accountDataChanged(username: String, email: String) {
        if (!isUserNameValid(username)) {
            _accountFormState.value = AccountFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _accountFormState.value = AccountFormState(emailError = R.string.invalid_email)
        } else {
            _accountFormState.value = AccountFormState(isDataValid = true)
        }
    }

    /**
     * A placeholder username validation check.
     *
     * @param username
     * @return true if username is valid or false otherwise
     * */
    private fun isUserNameValid(username: String): Boolean {
        return username.replace(" ", "").length >= 6
    }

    /**
     * A placeholder email validation check.
     *
     * @param email
     * @return true if email is valid or false otherwise
     * */
    private fun isEmailValid(email: String): Boolean {
        if (!TextUtils.isEmpty(email)) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
        return false
    }
}