package com.example.mystoryapp.data.userpref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import com.example.mystoryapp.data.response.UserSession
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreference(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id ?: ""
            preferences[EMAIL_KEY] = user.email
            preferences[NAME_KEY] = user.name ?: ""
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = user.isLogin
        }
    }

    fun getSession(): Flow<UserSession> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            UserSession(
                userId = preferences[USER_ID_KEY] ?: "",
                token = preferences[TOKEN_KEY] ?: "",
                email = preferences[EMAIL_KEY] ?: "",
                name = preferences[NAME_KEY] ?: "",
                isLoggedIn = preferences[IS_LOGIN_KEY] ?: false
            )
        }

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("userId")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NAME_KEY = stringPreferencesKey("name")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLoggedIn")
    }
}