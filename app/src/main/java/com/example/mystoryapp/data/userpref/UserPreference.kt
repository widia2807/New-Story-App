package com.example.mystoryapp.data.userpref

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.ads.mediationtestsuite.utils.DataStore
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.prefs.Preferences

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        try {
            dataStore.edit { preferences ->
                preferences[EMAIL_KEY] = user.email
                preferences[TOKEN_KEY] = user.token
                preferences[IS_LOGIN_KEY] = true
            }
            Log.d(TAG, "User session saved: $user")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving session: ${e.message}", e)
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading session: ${exception.message}", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val email = preferences[EMAIL_KEY] ?: ""
                val token = preferences[TOKEN_KEY] ?: ""
                val isLogin = preferences[IS_LOGIN_KEY] ?: false
                UserModel(email, token, isLogin).also {
                    Log.d(TAG, "User session retrieved: $it")
                }
            }
    }

    suspend fun logout() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d(TAG, "User session cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "UserPreference"

        @Volatile
        private var INSTANCE: UserPreference? = null

        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}