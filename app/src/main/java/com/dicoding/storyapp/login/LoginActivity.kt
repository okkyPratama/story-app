package com.dicoding.storyapp.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.MainActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.customview.CustomEmailText
import com.dicoding.storyapp.customview.CustomPasswordText
import com.dicoding.storyapp.data.User
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.remote.ApiConfig
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var user: User

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupCustomView()
        setupAction()
        playAnimation()

    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        val apiService = ApiConfig.getApiService()

        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), apiService, applicationContext)
        )[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this) { user ->
            this.user = user
        }

        loginViewModel.loggedIn.observe(this, { loggedIn ->
            showLoading(false)
            if (loggedIn) {
                AlertDialog.Builder(this).apply {
                    setTitle("Sukses")
                    setMessage("Sukses!! Login telah berhasil")
                    setPositiveButton("Lanjut") { _, _ ->
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    create()
                    show()
                }
            }
        })

        loginViewModel.errorMessage.observe(this, { errorMessage ->
            showLoading(false)
            if ( errorMessage!!.isNotEmpty()) {
                when (errorMessage) {
                    "Email tidak sesuai" -> {
                        binding.emailEditTextLayout.error = errorMessage
                    }
                    "Password tidak sesuai" -> {
                        binding.passwordEditTextLayout.error = errorMessage
                    }
                    else -> {
                        showErrorDialog(errorMessage)
                    }

                }
            }
        })

    }

    private fun setupCustomView() {
        val customEmailText = findViewById<CustomEmailText>(R.id.ed_login_email)
        val textInputLayout = findViewById<TextInputLayout>(R.id.emailEditTextLayout)
        customEmailText.setTextInputLayout(textInputLayout)

        val customPasswordText = findViewById<CustomPasswordText>(R.id.ed_login_password)
        val passwordTextInputLayout = findViewById<TextInputLayout>(R.id.passwordEditTextLayout)
        customPasswordText.setTextInputLayout(passwordTextInputLayout)
    }



    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            val isEmailValid = binding.edLoginEmail.isValid()
            val isPasswordValid = binding.edLoginPassword.isValid()

            when {
                email.isEmpty() -> {
                    binding.emailEditTextLayout.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.passwordEditTextLayout.error = "Masukkan password"
                }
                !isEmailValid -> {
                    binding.emailEditTextLayout.error = "Email tidak valid"
                }
                !isPasswordValid -> {
                    binding.passwordEditTextLayout.error = "Password tidak sesuai"
                }
                else -> {
                    showLoading(true)
                    loginViewModel.login(email, password)
                }
            }
        }
    }


    private fun showErrorDialog(error: String) {
        val errorDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK", null)
            .create()

        errorDialog.show()
    }

    private fun playAnimation(){
        ObjectAnimator.ofFloat(binding.profileImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(500)
        val emailLabel = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(500)
        val emailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordLabel = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(500)
        val passwordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val loginButton = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(500)

         AnimatorSet().apply {
            playSequentially(title,message,emailLabel,emailInput,passwordLabel,passwordInput, loginButton)
            start()
        }


    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


}