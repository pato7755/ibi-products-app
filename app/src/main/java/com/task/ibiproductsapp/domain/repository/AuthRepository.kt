package com.task.ibiproductsapp.domain.repository

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.AuthToken
import com.task.ibiproductsapp.domain.model.LoginCredentials

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): NetworkResult<AuthToken>
}