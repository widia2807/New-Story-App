package com.example.mystoryapp.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.response.StoryResponse
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class StoryManager private constructor(
    private val api: ApiService,
    private val preferences: UserPreference
) {

    suspend fun fetchAllStories(): StoryResponse {
        return try {
            val authToken = preferences.getSession().firstOrNull()?.token
                ?: throw IllegalStateException("Authentication token is missing")
            api.getStories(token = "Bearer $authToken")
        } catch (exception: Exception) {
            throw exception
        }
    }

    suspend fun fetchStoryDetails(storyId: String): DetailStoryResponse {
        return try {
            val authToken = preferences.getSession().firstOrNull()?.token
                ?: throw IllegalStateException("Authentication token is missing")
            api.getStoryDetail(token = "Bearer $authToken", storyId = storyId)
        } catch (exception: Exception) {
            throw exception
        }
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