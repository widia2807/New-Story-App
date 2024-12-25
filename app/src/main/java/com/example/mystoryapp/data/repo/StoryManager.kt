package com.example.mystoryapp.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.StoryResponse
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import com.example.mystoryapp.ui.auth.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException


class StoryManager private constructor(
    private val api: ApiService,
    private val preferences: UserPreference
) {

    fun getStories(token: String): Flow<NetworkResult<List<ListStoryItem?>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = api.getStories("Bearer $token")
            val stories = response.listStory ?: emptyList()
            emit(NetworkResult.Success(stories))
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: "An HTTP error occurred"))
        } catch (e: IOException) {
            emit(NetworkResult.Error("Network error occurred"))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "An unknown error occurred"))
        }
    }


    suspend fun fetchAllStories(): StoryResponse {
        val authToken = preferences.getSession().firstOrNull()?.token
            ?: throw IllegalStateException("Authentication token is missing")
        return api.getStories(token = "Bearer $authToken")
    }

    suspend fun fetchStoryDetails(storyId: String): DetailStoryResponse {
        val authToken = preferences.getSession().firstOrNull()?.token
            ?: throw IllegalStateException("Authentication token is missing")
        return api.getStoryDetail(token = "Bearer $authToken", id = storyId)
    }

    fun getPaginatedStories(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryDataSource(api, preferences) }
        ).flow
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryManager? = null

        fun createInstance(api: ApiService, preferences: UserPreference): StoryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryManager(api, preferences).also { INSTANCE = it }
            }
        }
    }
}
