package com.example.mystoryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.response.UserSession
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val repository: UserRepository,
    private val preferences: UserPreference
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<LoginResponse>>(NetworkResult.Loading)
    val loginState: StateFlow<NetworkResult<LoginResponse>> = _loginState

    fun login(email: String, password: String): Flow<NetworkResult<LoginResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = repository.login(email, password)
            if (response.error == false) {
                emit(NetworkResult.Success(response))
            } else {
                emit(NetworkResult.Error(response.message ?: "Unknown error occurred"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            emit(NetworkResult.Error(e.message ?: "Network error occurred"))
        }
    }.catch { e ->
        Timber.e(e, "Login flow error")
        emit(NetworkResult.Error("An unexpected error occurred"))
    }

    fun saveUserSession(session: UserSession) {
        viewModelScope.launch {
            try {
                preferences.saveUserSession(session)
            } catch (e: Exception) {
                Timber.e(e, "Error saving user session")
            }
        }
    }

    fun getUserSession(): Flow<UserSession> = preferences.getUserSession()

    fun logout() {
        viewModelScope.launch {
            try {
                preferences.clearUserSession()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing user session")
            }
        }
    }
}

class LoginViewModelFactory(
    private val repository: UserRepository,
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

// Network Result sealed class untuk handling berbagai state
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

// Data class untuk response login
data class LoginResponse(
    val error: Boolean? = null,
    val message: String? = null,
    val token: String? = null
)

// User Repository interface
interface UserRepository {
    suspend fun login(email: String, password: String): LoginResponse
}

// User Repository implementation
class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(LoginRequest(email, password))
    }
}

// Data class untuk request login
data class LoginRequest(
    val email: String,
    val password: String
)