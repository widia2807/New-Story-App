package com.example.mystoryapp.data.di

import android.content.Context
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.data.userpref.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object DependencyProvider {

    fun createUserRepository(context: Context, apiService: ApiService): UserManager {
        val preferences = UserPreference.getInstance(context.dataStore)
        return UserManager.getInstance(preferences, apiService)
    }

    fun createStoryRepository(context: Context): StoryManager {
        val userPreferences = UserPreference.getInstance(context.dataStore)
        val userSession = runBlocking { userPreferences.getSession().first() }
        val apiClient = com.example.mystoryapp.data.retrofit.ApiConfig().getApiService(userSession.token)
        return StoryManager.getInstance(apiClient, userPreferences)
    }
}