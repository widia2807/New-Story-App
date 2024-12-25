package com.example.mystoryapp.data.userpref


data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false,
    val id: String? = null,
    val name: String? = null,
)