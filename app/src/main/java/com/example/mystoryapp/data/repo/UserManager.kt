package com.example.mystoryapp.data.repo

import android.util.Log
import com.example.mystoryapp.data.response.*
import com.example.mystoryapp.data.retrofit.ApiConfig
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserModel
import com.example.mystoryapp.data.userpref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UserManager(
    private val userPreferences: UserPreference,
    private val api: ApiService
) {
    private var authenticatedApi: ApiService? = null
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
            val response = api.login(email, password)
            // Store the authenticated API service for future use
            response.loginResult?.token?.let { token ->
                authenticatedApi = ApiConfig.getAuthenticatedApiService(token)
            }
            response
        } catch (exception: HttpException) {
            val errorJson = exception.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            throw exception
        }
    }

    suspend fun storeUserSession(user: UserModel) {
        userPreferences.saveSession(user)
    }

    fun retrieveUserSession(): Flow<UserModel> {
        // Mapping dari Flow<UserSession> ke Flow<UserModel> jika UserPreference mengembalikan UserSession
        return userPreferences.getSession().map { userSession ->
            UserModel(
                id = userSession.id,
                name = userSession.name,
                email = userSession.email,
                token = userSession.token
            )
        }
    }

    suspend fun clearSession() {
        userPreferences.logout()
    }

    suspend fun fetchStories(
        authToken: String,
        page: Int? = null,
        size: Int? = null,
        locationFilter: Int? = null // Disesuaikan agar null diterima
    ): StoryResponse {
        // Pastikan parameter yang diberikan tidak null jika API tidak menerima null
        val validPage = page ?: 1
        val validSize = size ?: 10
        val validLocationFilter = locationFilter ?: 0
        return api.getStories(authToken, validPage, validSize, validLocationFilter)
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
    ): AddResponse {
        return api.addStory(authToken, image, description, latitude, longitude)
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
