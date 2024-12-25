package com.example.mystoryapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.retrofit.ApiService
import kotlinx.coroutines.launch

class RegisterViewModel(private val apiService: ApiService) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState

    private val _registrationOutcome = MutableLiveData<String?>()
    val registrationOutcome: LiveData<String?> = _registrationOutcome

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun registerUser(name: String, email: String, password: String) {
        _loadingState.value = true
        viewModelScope.launch {
            try {
                val response = apiService.registerUser(name, email, password)
                if (response.error == false) {
                    _registrationOutcome.value = response.message
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }


    fun clearErrors() {
        _error.value = null
    }
}
