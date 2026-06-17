package com.task.ibiproductsapp.domain.model

data class AuthToken(
    val token: String,
    val refreshToken: String,
    val userId: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String
)

data class LoginCredentials(
    val username: String,
    val password: String,
    val expiresInMins: Int = 60
)
