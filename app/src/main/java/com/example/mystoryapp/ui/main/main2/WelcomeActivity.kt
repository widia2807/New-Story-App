package com.example.mystoryapp.ui.main.main2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import com.example.mystoryapp.databinding.ActivityWelcomeBinding
import com.example.mystoryapp.ui.auth.LoginActivity
import com.example.mystoryapp.ui.auth.LoginViewModel
import com.example.mystoryapp.ui.auth.LoginViewModelFactory
import com.example.mystoryapp.ui.auth.RegisterActivity
import com.example.mystoryapp.ui.main.main1.MainActivity
import kotlinx.coroutines.launch
import timber.log.Timber

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreference = UserPreference.getInstance(dataStore)
        val userManager = UserManager(
            apiService = ApiConfig.getApiService(),
            preference = userPreference
        )
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(userManager, userPreference)
        )[LoginViewModel::class.java]

        checkExistingSession()
        setupView()
        setupAction()
        playAnimation()
    }



    private fun checkExistingSession() {
        Timber.d("Checking existing session in WelcomeActivity")
        lifecycleScope.launch {
            viewModel.getUserSession().collect { session ->
                Timber.d("Session in Welcome: isLoggedIn=${session.isLoggedIn}, hasToken=${session.token.isNotEmpty()}")
                if (session.isLoggedIn && session.token.isNotEmpty()) {
                    Timber.d("Valid session found, navigating to MainActivity")
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
    private fun playAnimation(){
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val desc = ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 1f).setDuration(100)

        val together = AnimatorSet().apply {
            playTogether(login, signup)
        }
        AnimatorSet().apply {
            playSequentially(title, desc, together)
            start()
        }
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


    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}