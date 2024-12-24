package com.example.mystoryapp.ui.main.main2

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.data.di.DependencyProvider
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.ui.auth.LoginViewModel
import com.example.mystoryapp.ui.auth.RegisterViewModel
import com.example.mystoryapp.ui.main.main1.MainViewModel

class ViewModelFactory private constructor(
    private val userRepo: UserManager,
    private val storyRepo: StoryManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepo, storyRepo) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepo) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context, apiService: ApiService): ViewModelFactory {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val userRepository = DependencyProvider.provideUserRepository(context, apiService)
                        val storyRepository = DependencyProvider.provideStoryRepository(context)
                        instance = ViewModelFactory(userRepository, storyRepository)
                    }
                }
            }
            return instance!!
        }

        fun resetInstance() {
            instance = null
        }
    }
}