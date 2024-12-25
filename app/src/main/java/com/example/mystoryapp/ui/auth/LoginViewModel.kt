package com.example.mystoryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.data.response.UserSession
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val repository: UserRepository,
    private val preferences: UserPreference
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<LoginResponse>>(NetworkResult.Loading)
    val loginState: StateFlow<NetworkResult<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = NetworkResult.Loading
            try {
                val response = repository.login(email, password)
                if (response.error == false) {
                    _loginState.value = NetworkResult.Success(response)
                } else {
                    _loginState.value = NetworkResult.Error(response.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                Timber.e(e, "Login error")
                _loginState.value = NetworkResult.Error(e.message ?: "Network error occurred")
            }
        }
    }

    fun saveUserSession(session: UserSession) {
        viewModelScope.launch {
            try {
                preferences.saveSession(session)
            } catch (e: Exception) {
                Timber.e(e, "Error saving user session")
            }
        }
    }

    fun getUserSession(): Flow<UserSession> = preferences.getSession()

    fun logout() {
        viewModelScope.launch {
            try {
                preferences.logout()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing user session")
            }
        }
    }
}

class LoginViewModelFactory(
    private val repository: UserManager,
    private val preferences: UserPreference
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

interface UserRepository {
    suspend fun login(email: String, password: String): LoginResponse
}

class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }
}


