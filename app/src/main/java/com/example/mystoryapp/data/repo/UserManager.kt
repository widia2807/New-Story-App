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
    private val apiService: ApiService,
    private val preference: UserPreference



) {
    private var authenticatedApi: ApiService? = null
    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            apiService.register(name, email, password)
        } catch (exception: HttpException) {
            val errorJson = exception.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            throw Exception("Registration failed: ${errorResponse.message}")
        }
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return try {
            val response = apiService.login(email, password)
            response.loginResult?.token?.let { token ->
                ApiConfig.getAuthenticatedApiService(token)
            }
            response
        } catch (exception: HttpException) {
            val errorJson = exception.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            throw Exception("Login failed: ${errorResponse.message}")
        }
    }

    suspend fun storeUserSession(user: UserModel) {
        preference.saveSession(user)
    }

    fun retrieveUserSession(): Flow<UserModel> {
        // Mapping dari Flow<UserSession> ke Flow<UserModel> jika UserPreference mengembalikan UserSession
        return preference.getSession().map { userSession ->
            UserModel(
                id = userSession.id,
                name = userSession.name,
                email = userSession.email,
                token = userSession.token
            )
        }
    }

    suspend fun clearSession() {
        preference.logout()
    }

    suspend fun fetchStories(
        authToken: String,
        page: Int? = null,
        size: Int? = null,
        locationFilter: Int? = null
    ): StoryResponse {
        val validPage = page ?: 1
        val validSize = size ?: 10
        val validLocationFilter = locationFilter ?: 0
        return apiService.getStories(authToken, validPage, validSize, validLocationFilter)
    }

    suspend fun fetchStoryDetails(authToken: String, storyId: String): DetailStoryResponse {
        return apiService.getStoryDetail(authToken, storyId)
    }

    suspend fun uploadStory(
        authToken: String,
        image: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody? = null,
        longitude: RequestBody? = null
    ): AddResponse {
        return apiService.addStory(authToken, image, description, latitude, longitude)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null

        fun createInstance(apiService: ApiService, preference: UserPreference): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager(apiService, preference).also { INSTANCE = it }
            }
        }
    }
}
