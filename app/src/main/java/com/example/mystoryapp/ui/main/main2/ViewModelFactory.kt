package com.example.mystoryapp.ui.main.main2

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import com.example.mystoryapp.ui.auth.LoginViewModel
import com.example.mystoryapp.ui.auth.RegisterViewModel


class ViewModelFactory private constructor(
    private val userManager: UserManager,
    private val preferences: UserPreference
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userManager, preferences) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context, apiService: ApiService): ViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    UserManager.createInstance(
                        UserPreference.getInstance(context.dataStore),
                        apiService
                    ),
                    UserPreference.getInstance(context.dataStore)
                ).also { instance = it }
            }
        }

        fun resetInstance() {
            instance = null
        }
    }
}