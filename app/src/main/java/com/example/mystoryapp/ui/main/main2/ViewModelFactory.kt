package com.example.mystoryapp.ui.main.main2

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.data.di.DependencyProvider
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import com.example.mystoryapp.ui.auth.LoginViewModel
import com.example.mystoryapp.ui.auth.RegisterViewModel
import com.example.mystoryapp.ui.main.main1.MainViewModel


class ViewModelFactory private constructor(
    private val userManager: UserManager,
    private val storyManager: com.example.mystoryapp.data.repo.StoryManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userManager, UserPreference.getInstance(context.dataStore)) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userManager) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userManager, storyManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        private lateinit var context: Context

        fun getInstance(context: Context, apiService: ApiService): ViewModelFactory {
            this.context = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    DependencyProvider.createUserRepository(context, apiService),
                    DependencyProvider.createStoryRepository(context)
                ).also { instance = it }
            }
        }

        fun resetInstance() {
            instance = null
        }
    }
}