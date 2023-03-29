package com.personal.chatroommobile.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.personal.chatroommobile.MainActivity
import com.personal.chatroommobile.R
import com.personal.chatroommobile.data.repositories.AuthRepository
import com.personal.chatroommobile.data.source.AuthDataSource
import com.personal.chatroommobile.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: ActivityAuthBinding
    private lateinit var rememberedCredentials: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Switch between sign in and sign up.

        val signInForm: View = findViewById(R.id.sign_in_form)
        val signUpForm: View = findViewById(R.id.sign_up_form)
        val signInSwitch: TextView = findViewById(R.id.sign_in_switch)
        val signUpSwitch: TextView = findViewById(R.id.sign_up_switch)

        signInSwitch.setOnClickListener {
            signInForm.visibility = View.GONE
            signUpForm.visibility = View.VISIBLE
        }

        signUpSwitch.setOnClickListener {
            signUpForm.visibility = View.GONE
            signInForm.visibility = View.VISIBLE
        }

        // Check for user remembered credentials.

        rememberedCredentials = getSharedPreferences("CREDENTIALS", Context.MODE_PRIVATE)

        val rememberedEmail = rememberedCredentials.getString("remembered_email", "")
        val rememberedPassword = rememberedCredentials.getString("remembered_password", "")

        if (rememberedEmail != "" && rememberedPassword != "") {
            binding.signInForm.signInEmail.setText(rememberedEmail)
            binding.signInForm.signInPassword.setText(rememberedPassword)
            binding.signInForm.signIn.isEnabled = true
        }
        // Auth user.

        authViewModel = AuthViewModel(
            AuthRepository(
                AuthDataSource()
            )
        )

        // Sign in user.

        val signInEmail = binding.signInForm.signInEmail
        val signInPassword = binding.signInForm.signInPassword
        val signInRemember = binding.signInForm.rememberMe
        val signInLoading = binding.signInForm.signInLoading
        val signIn = binding.signInForm.signIn

        authViewModel.loginFormState.observe(this@AuthActivity, Observer {
            val loginState = it ?: return@Observer

            if (loginState.emailError != null) {
                signInEmail.error = getString(loginState.emailError)
            }

            if (loginState.passwordError != null) {
                signInPassword.error = getString(loginState.passwordError)
            }

            signIn.isEnabled = loginState.isDataValid
        })

        authViewModel.loginResult.observe(this@AuthActivity, Observer {
            val loginResult = it ?: return@Observer

            signInLoading.visibility = View.GONE
            signIn.visibility = View.VISIBLE

            if (loginResult.error != null) {
                Toast.makeText(applicationContext, loginResult.error, Toast.LENGTH_SHORT).show()
            }

            if (loginResult.warning != null) {
                Toast.makeText(applicationContext, loginResult.warning, Toast.LENGTH_SHORT).show()
            }

            if (loginResult.success != null) {
                val editRememberedCredentials = rememberedCredentials.edit()

                if (signInRemember.isChecked) {
                    editRememberedCredentials.putString(
                        "remembered_email",
                        signInEmail.text.toString()
                    ).apply()
                    editRememberedCredentials.putString(
                        "remembered_password",
                        signInPassword.text.toString()
                    ).apply()
                    editRememberedCredentials.putString(
                        "token",
                        loginResult.success.token
                    ).apply()
                    editRememberedCredentials.apply()
                } else {
                    editRememberedCredentials.remove("remembered_email").apply()
                    editRememberedCredentials.remove("remembered_password").apply()
                    editRememberedCredentials.remove("token").apply()
                    editRememberedCredentials.apply()
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("id", loginResult.success.id)
                intent.putExtra("name", loginResult.success.name)
                intent.putExtra("token", loginResult.success.token)
                startActivity(intent)
            }

            setResult(Activity.RESULT_OK)
        })

        signInEmail.afterTextChanged {
            authViewModel.loginDataChanged(
                signInEmail.text.toString(),
                signInPassword.text.toString()
            )
        }

        signInPassword.apply {
            afterTextChanged {
                authViewModel.loginDataChanged(
                    signInEmail.text.toString(),
                    signInPassword.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        authViewModel.login(
                            signInEmail.text.toString(),
                            signInPassword.text.toString()
                        )
                }
                false
            }

            signIn.setOnClickListener {
                signIn.visibility = View.GONE
                signInLoading.visibility = View.VISIBLE

                lifecycleScope.launch {
                    authViewModel.login(signInEmail.text.toString(),
                        signInPassword.text.toString())
                }
            }
        }

        // Sign up user.

        val signUpUsername = binding.signUpForm.signUpUsername
        val signUpEmail = binding.signUpForm.signUpEmail
        val signUpPassword = binding.signUpForm.signUpPassword
        val signUpConfirmPassword = binding.signUpForm.signUpConfirmPassword
        val signUpRemember = binding.signUpForm.rememberMe
        val signUpLoading = binding.signUpForm.signUpLoading
        val signUp = binding.signUpForm.signUp

        authViewModel.registerFormState.observe(this@AuthActivity, Observer {
            val registerState = it ?: return@Observer

            if (registerState.usernameError != null) {
                signUpUsername.error = getString(registerState.usernameError)
            }

            if (registerState.emailError != null) {
                signUpEmail.error = getString(registerState.emailError)
            }

            if (registerState.passwordError != null) {
                signUpPassword.error = getString(registerState.passwordError)
            }

            if (registerState.confirmPasswordError != null) {
                signUpConfirmPassword.error = getString(registerState.confirmPasswordError)
            }

            signUp.isEnabled = registerState.isDataValid
        })

        authViewModel.registerResult.observe(this@AuthActivity, Observer {
            val registerResult = it ?: return@Observer

            signUpLoading.visibility = View.GONE
            signUp.visibility = View.VISIBLE

            if (registerResult.error != null) {
                Toast.makeText(applicationContext, registerResult.error, Toast.LENGTH_SHORT).show()
            }

            if (registerResult.warning != null) {
                Toast.makeText(applicationContext, registerResult.warning, Toast.LENGTH_SHORT)
                    .show()
            }

            if (registerResult.success != null) {
                val editRememberedCredentials = rememberedCredentials.edit()

                if (signUpRemember.isChecked) {
                    editRememberedCredentials.putString(
                        "remembered_email",
                        signUpEmail.text.toString()
                    ).apply()
                    editRememberedCredentials.putString(
                        "remembered_password",
                        signUpPassword.text.toString()
                    ).apply()
                    editRememberedCredentials.apply()

                    binding.signInForm.signInEmail.setText(signUpEmail.text.toString())
                    binding.signInForm.signInPassword.setText(signUpPassword.text.toString())
                } else {
                    editRememberedCredentials.remove("remembered_email").apply()
                    editRememberedCredentials.remove("remembered_password").apply()
                    editRememberedCredentials.apply()

                    binding.signInForm.signInEmail.setText("")
                    binding.signInForm.signInPassword.setText("")
                }

                Toast.makeText(applicationContext,
                    R.string.registration_success,
                    Toast.LENGTH_SHORT).show()

                signUpForm.visibility = View.GONE
                signInForm.visibility = View.VISIBLE
            }

            setResult(Activity.RESULT_OK)
        })

        signUpUsername.afterTextChanged {
            authViewModel.registerDataChanged(
                signUpUsername.text.toString(),
                signUpEmail.text.toString(),
                signUpPassword.text.toString(),
                signUpConfirmPassword.text.toString()
            )
        }

        signUpPassword.apply {
            afterTextChanged {
                authViewModel.registerDataChanged(
                    signUpUsername.text.toString(),
                    signUpEmail.text.toString(),
                    signUpPassword.text.toString(),
                    signUpConfirmPassword.text.toString()
                )
            }
        }

        signUpConfirmPassword.apply {
            afterTextChanged {
                authViewModel.registerDataChanged(
                    signUpUsername.text.toString(),
                    signUpEmail.text.toString(),
                    signUpPassword.text.toString(),
                    signUpConfirmPassword.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        authViewModel.register(
                            signUpUsername.text.toString(),
                            signUpEmail.text.toString(),
                            signUpPassword.text.toString()
                        )
                }
                false
            }

            signUp.setOnClickListener {
                signUp.visibility = View.GONE
                signUpLoading.visibility = View.VISIBLE

                lifecycleScope.launch {
                    authViewModel.register(signUpUsername.text.toString(),
                        signUpEmail.text.toString(),
                        signUpPassword.text.toString())
                }
            }
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}