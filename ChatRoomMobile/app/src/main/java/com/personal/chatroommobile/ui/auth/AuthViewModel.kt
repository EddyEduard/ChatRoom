package com.personal.chatroommobile.ui.auth

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.repositories.AuthRepository
import com.personal.chatroommobile.data.Result
import kotlinx.coroutines.*

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _registerFormState = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    /**
     * Login user.
     *
     * @param email
     * @param password
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun login(email: String, password: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = authRepository.login(email, password)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _loginResult.value =
                                LoginResult(success = LoggedInUserView(result.data.id,
                                    result.data.name,
                                    result.data.token))
                        } else {
                            if (result is Result.Warning)
                                _loginResult.value = LoginResult(warning = result.warning)
                            else
                                _loginResult.value = LoginResult(error = R.string.login_failed)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Registration user.
     *
     * @param username
     * @param email
     * @param password
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            GlobalScope.launch {
                suspend {
                    val result = authRepository.register(username, email, password)

                    withContext(Dispatchers.Main) {
                        if (result is Result.Success) {
                            _registerResult.value = RegisterResult(success = result.data)
                        } else {
                            if (result is Result.Warning)
                                _registerResult.value = RegisterResult(warning = result.warning)
                            else
                                _registerResult.value =
                                    RegisterResult(error = R.string.registration_failed)
                        }
                    }
                }.invoke()
            }
        }
    }

    /**
     * Validate login data when is changed.
     *
     * @param email
     * @param password
     * */
    fun loginDataChanged(email: String, password: String) {
        if (!isUserNameValid(email)) {
            _loginFormState.value = LoginFormState(emailError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    /**
     * Validate registration data when is changed.
     *
     * @param username
     * @param email
     * @param password
     * @param confirmPassword
     * */
    fun registerDataChanged(username: String, email: String, password: String, confirmPassword: String) {
        if (!isUserNameValid(username)) {
            _registerFormState.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
                _registerFormState.value = RegisterFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _registerFormState.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else if (!isPasswordValid(confirmPassword)) {
            _registerFormState.value =
                RegisterFormState(confirmPasswordError = R.string.invalid_password)
        } else if (!isMatchPasswords(password, confirmPassword)) {
            _registerFormState.value =
                RegisterFormState(confirmPasswordError = R.string.invalid_sign_up_confirm_password)
        } else {
            _registerFormState.value = RegisterFormState(isDataValid = true)
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

    /**
     * A placeholder password validation check.
     *
     * @param password
     * @return true if password is valid or false otherwise
     * */
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * A placeholder passwords match check.
     *
     * @param password_one
     * @param password_two
     * @return true if passwords are match or false otherwise
     * */
    private fun isMatchPasswords(password_one: String, password_two: String): Boolean {
        return password_one.matches(Regex(password_two))
    }
}