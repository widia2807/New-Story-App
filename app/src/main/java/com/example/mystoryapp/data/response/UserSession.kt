package com.example.mystoryapp.data.response

data class UserSession(
    val userId: String,
    val token: String,
    val email: String = "",
    val name: String ,
    val isLoggedIn: Boolean = false,
    val id: String? = null,
)


