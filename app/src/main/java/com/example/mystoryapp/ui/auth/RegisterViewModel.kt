package com.example.mystoryapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.retrofit.ApiService
import kotlinx.coroutines.launch

class RegisterViewModel(private val apiService: ApiService) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerResult = MutableLiveData<String?>()
    val registerResult: LiveData<String?> = _registerResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.registerUser(name, email, password)
                if (response.isSuccessful && response.body() != null) {
                    _registerResult.value = response.body()?.message
                } else {
                    _error.value = response.errorBody()?.string() ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrors() {
        _error.value = null
    }
}
