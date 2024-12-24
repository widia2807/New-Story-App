package com.example.mystoryapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mystoryapp.databinding.ActivityRegisterBinding
import com.example.mystoryapp.ui.main.main2.ViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this, com.example.mystoryapp.data.retrofit.ApiConfig().getApiService("token"))
    }

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        observeViewModel()
        handleUserActions()
        animateUIComponents()
    }

    private fun initializeView() {
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

    private fun observeViewModel() {
        viewModel.loadingState.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.registerButton.isEnabled = !isLoading
        }

        viewModel.registrationOutcome.observe(this) { outcome ->
            outcome?.let {
                Log.d("RegisterActivity", "Registration success: $outcome")
                showPopup(
                    title = "Success!",
                    message = "Your account has been created successfully.",
                    isError = false
                )
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Log.e("RegisterActivity", "Registration error: $error")
                showPopup(
                    title = "Registration Failed",
                    message = error,
                    isError = true
                )
            }
        }
    }

    private fun handleUserActions() {
        binding.registerButton.setOnClickListener {
            val fullName = binding.edRegisterName.text.toString()
            val emailAddress = binding.edRegisterEmail.text.toString()
            val userPassword = binding.edRegisterPassword.text.toString()

            Log.d("RegisterActivity", "Register button clicked. Data: Name=$fullName, Email=$emailAddress")

            if (fullName.isBlank() || emailAddress.isBlank() || userPassword.isBlank()) {
                showPopup(
                    title = "Incomplete Data",
                    message = "Please fill in all required fields.",
                    isError = true
                )
                Log.w("RegisterActivity", "Required fields are missing.")
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                showPopup(
                    title = "Invalid Email",
                    message = "Please enter a valid email address.",
                    isError = true
                )
                Log.w("RegisterActivity", "Invalid email format: $emailAddress")
            } else if (userPassword.length < 6) {
                showPopup(
                    title = "Weak Password",
                    message = "Password must be at least 6 characters long.",
                    isError = true
                )
                Log.w("RegisterActivity", "Password is too short.")
            } else {
                viewModel.registerUser(fullName, emailAddress, userPassword)
            }
        }
    }

    private fun showPopup(title: String, message: String, isError: Boolean) {
        AlertDialog.Builder(this@RegisterActivity).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (!isError) {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            create()
            show()
        }
    }

    private fun animateUIComponents() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -25f, 25f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleAnimation = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(200)
        val nameLabelAnimation = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(200)
        val nameFieldAnimation = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val emailLabelAnimation = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(200)
        val emailFieldAnimation = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val passwordLabelAnimation = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(200)
        val passwordFieldAnimation = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(200)
        val registerButtonAnimation = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(
                titleAnimation,
                nameLabelAnimation,
                nameFieldAnimation,
                emailLabelAnimation,
                emailFieldAnimation,
                passwordLabelAnimation,
                passwordFieldAnimation,
                registerButtonAnimation
            )
            startDelay = 100
        }.start()
    }
}
