package com.example.mystoryapp.ui.main.main1

import androidx.lifecycle.LiveData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.mystoryapp.data.repo.StoryManager
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.DetailStoryResponse
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.data.userpref.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val userManager: UserManager,
    private val storyManager: StoryManager
) : ViewModel() {

    fun getUserSession(): LiveData<UserModel> {
        return userManager.getSession().asLiveData()
    }

    fun logoutUser() {
        viewModelScope.launch { userManager.logout() }
    }

    suspend fun fetchStoryById(storyId: String): DetailStoryResponse {
        return storyManager.getStoryById(storyId)
    }

    fun getStoryPagingData(): Flow<PagingData<ListStoryItem>> {
        return storyManager.getStoryPager()
    }
}