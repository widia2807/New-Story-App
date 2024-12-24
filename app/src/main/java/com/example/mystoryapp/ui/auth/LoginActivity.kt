package com.example.mystoryapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.R
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.databinding.ActivityLoginBinding
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import kotlinx.coroutines.launch
import java.lang.ref.Cleaner.create

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferences: UserPreference

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            UserManager(ApiService.create()),
            UserPreference(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowDecorations()
        initializeViews()
        checkUserSession()
        setupAnimations()
    }

    private fun setupWindowDecorations() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide()
    }

    private fun initializeViews() {
        with(binding) {
            loginButton.setOnClickListener { handleLoginAttempt() }
            registerLink.setOnClickListener { navigateToRegister() }

            // Custom password validation
            passwordEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if ((s?.length ?: 0) < 8) {
                        passwordEditTextLayout.error = "Password must be at least 8 characters"
                        passwordEditTextLayout.setErrorIconDrawable(R.drawable.ic_password_error)
                    } else {
                        passwordEditTextLayout.error = null
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun handleLoginAttempt() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (validateInputs(email, password)) {
            binding.loginButton.isEnabled = false
            binding.loadingIndicator.visibility = View.VISIBLE

            lifecycleScope.launch {
                viewModel.login(email, password).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            val session = UserSession(
                                email = email,
                                token = result.data.token,
                                isLoggedIn = true
                            )
                            viewModel.saveUserSession(session)
                            navigateToHome()
                        }
                        is NetworkResult.Error -> {
                            handleLoginError(result.message, email)
                        }
                        is NetworkResult.Loading -> {
                            // Loading state handled by initial visibility changes
                        }
                    }
                    binding.loginButton.isEnabled = true
                    binding.loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditTextLayout.error = "Please enter a valid email"
            binding.emailEditTextLayout.setErrorIconDrawable(R.drawable.ic_email_error)
            isValid = false
        } else {
            binding.emailEditTextLayout.error = null
        }

        if (password.length < 8) {
            binding.passwordEditTextLayout.error = "Password must be at least 8 characters"
            binding.passwordEditTextLayout.setErrorIconDrawable(R.drawable.ic_password_error)
            isValid = false
        } else {
            binding.passwordEditTextLayout.error = null
        }

        return isValid
    }

    private fun handleLoginError(message: String, email: String) {
        when {
            message.contains("not found") -> {
                showDialog(
                    "Account Not Found",
                    "No account found with email $email. Would you like to register?",
                    positiveAction = { navigateToRegister() }
                )
            }
            message.contains("invalid credentials") -> {
                showDialog(
                    "Login Failed",
                    "Invalid email or password. Please try again."
                )
            }
            else -> {
                showDialog(
                    "Error",
                    "An unexpected error occurred. Please try again later."
                )
            }
        }
    }

    private fun checkUserSession() {
        lifecycleScope.launch {
            viewModel.getUserSession().collect { session ->
                if (session.isLoggedIn && session.token.isNotEmpty()) {
                    navigateToHome()
                }
            }
        }
    }

    private fun setupAnimations() {
        // Logo animation
        ObjectAnimator.ofFloat(binding.logoImage, View.TRANSLATION_Y, -50f, 50f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        // Sequential fade-in animations
        val fadeAnimations = listOf(
            binding.titleText,
            binding.subtitleText,
            binding.emailEditTextLayout,
            binding.passwordEditTextLayout,
            binding.loginButton,
            binding.registerLink
        ).map { view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                duration = 300
            }
        }

        AnimatorSet().apply {
            playSequentially(fadeAnimations)
            startDelay = 500
        }.start()
    }

    private fun showDialog(
        title: String,
        message: String,
        positiveAction: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                positiveAction?.invoke()
                dialog.dismiss()
            }
            if (positiveAction != null) {
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            create()
            show()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}