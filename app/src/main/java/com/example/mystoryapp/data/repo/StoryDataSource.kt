package com.example.mystoryapp.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StoryDataSource(
    private val api: ApiService,
    private val preferences: UserPreference
) : PagingSource<Int, ListStoryItem>() {

    companion object {
        private const val START_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val currentPage = params.key ?: START_PAGE_INDEX
            val authToken = runBlocking { preferences.getSession().first().token }
            val response = api.getStories(
                token = "Bearer $authToken",
                page = currentPage,
                size = params.loadSize
            )
            val storyList = response.listStory

            LoadResult.Page(
                data = storyList?.filterNotNull() ?: emptyList(),
                prevKey = if (currentPage == START_PAGE_INDEX) null else currentPage - 1,
                nextKey = if (storyList.isNullOrEmpty()) null else currentPage + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}