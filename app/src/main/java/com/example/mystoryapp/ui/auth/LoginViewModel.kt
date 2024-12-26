package com.example.mystoryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystoryapp.data.repo.UserManager
import com.example.mystoryapp.data.response.LoginResponse
import com.example.mystoryapp.data.response.UserSession
import com.example.mystoryapp.data.retrofit.ApiService
import com.example.mystoryapp.data.userpref.UserModel
import com.example.mystoryapp.data.userpref.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val userManager: UserManager,
    private val preferences: UserPreference
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<LoginResponse>>(NetworkResult.Loading)
    val loginState: StateFlow<NetworkResult<LoginResponse>> = _loginState

    fun login(email: String, password: String) = flow {
        Timber.d("Starting login process for email: $email")
        emit(NetworkResult.Loading)
        try {
            val response = userManager.loginUser(email, password)
            Timber.d("Login response received: error=${response.error}, message=${response.message}")

            if (response.error == false) {
                Timber.d("Login successful: userId=${response.loginResult?.userId}, name=${response.loginResult?.name}")
                emit(NetworkResult.Success(response))
            } else {
                Timber.e("Login failed: ${response.message}")
                emit(NetworkResult.Error(response.message ?: "Unknown error occurred"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error: ${e.message}")
            emit(NetworkResult.Error(e.message ?: "Network error occurred"))
        }
    }

    fun saveUserSession(session: UserSession) {
        viewModelScope.launch {
            try {
                Timber.d("Saving user session: userId=${session.userId}, email=${session.email}, isLoggedIn=${session.isLoggedIn}")
                preferences.saveSession(
                    UserModel(
                        email = session.email,
                        token = session.token,
                        isLogin = session.isLoggedIn,
                        id = session.userId,
                        name = session.name
                    )
                )
                Timber.d("User session saved successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error saving user session")
            }
        }
    }

    fun getUserSession(): Flow<UserSession> {
        Timber.d("Getting user session")
        return preferences.getSession().also { flow ->
            viewModelScope.launch {
                flow.collect { session ->
                    Timber.d("Current session state: userId=${session.userId}, isLoggedIn=${session.isLoggedIn}, tokenEmpty=${session.token.isEmpty()}")
                }
            }
        }
    }

    fun verifySession() {
        viewModelScope.launch {
            Timber.d("Verifying session")
            preferences.getSession().collect { session ->
                Timber.d("Session verification: isLoggedIn=${session.isLoggedIn}, hasToken=${session.token.isNotEmpty()}, userId=${session.userId}")
            }
        }
    }
}
class LoginViewModelFactory(
    private val userManager: UserManager,
    private val preferences: UserPreference
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userManager, preferences) as T
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


