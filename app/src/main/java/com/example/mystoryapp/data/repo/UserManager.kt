package com.example.mystoryapp.data.repo

import android.net.http.HttpException
import android.util.Log
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ErrorResponse
import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.data.response.RegisterResponse
import com.example.mystoryapp.data.response.StoryResponse
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserModel
import com.example.mystoryapp.data.userpref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserManager(
    private val userPreferences: UserPreference,
    private val api: ApiService
) {

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            val registerResult = api.register(name, email, password)
            Log.i("UserManager", "Registration successful: $registerResult")
            registerResult
        } catch (exception: HttpException) {
            val errorJson = exception.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            Log.e("UserManager", "Registration failed: ${errorResponse.message}")
            throw exception
        }
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return try {
            api.login(email, password)
        } catch (exception: HttpException) {
            val errorJson = exception.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            Log.e("UserManager", "Login failed: ${errorResponse.message}")
            throw exception
        }
    }

    suspend fun storeUserSession(user: UserModel) {
        userPreferences.saveSession(user)
    }

    fun retrieveUserSession(): Flow<UserModel> {
        return userPreferences.getSession()
    }

    suspend fun clearSession() {
        userPreferences.logout()
    }

    suspend fun fetchStories(
        authToken: String,
        page: Int? = null,
        size: Int? = null,
        locationFilter: Int? = 0
    ): StoryResponse {
        return api.getStories(authToken, page, size)
    }

    suspend fun fetchStoryDetails(authToken: String, storyId: String): DetailStoryResponse {
        return api.getStoryDetail(authToken, storyId)
    }

    suspend fun uploadStory(
        authToken: String,
        image: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody? = null,
        longitude: RequestBody? = null
    ): RegisterResponse {
        return api.addStory(authToken, image, description)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null

        fun createInstance(
            userPreferences: UserPreference,
            api: ApiService
        ): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(userPreferences, api).also { INSTANCE = it }
            }
        }
    }
}