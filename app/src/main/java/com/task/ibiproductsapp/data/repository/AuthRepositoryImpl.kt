package com.task.ibiproductsapp.data.repository

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.common.ParseErrorMessage
import com.task.ibiproductsapp.data.mapper.toDomain
import com.task.ibiproductsapp.data.remote.ApiService
import com.task.ibiproductsapp.data.remote.dto.request.LoginRequestDto
import com.task.ibiproductsapp.domain.model.AuthToken
import com.task.ibiproductsapp.domain.model.LoginCredentials
import com.task.ibiproductsapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(credentials: LoginCredentials): NetworkResult<AuthToken> {
        return try {
            val response = apiService.login(
                LoginRequestDto(
                    username = credentials.username,
                    password = credentials.password,
                    expiresInMins = credentials.expiresInMins
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return NetworkResult.Error("Empty response from server")
                NetworkResult.Success(body.toDomain())
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                NetworkResult.Error(ParseErrorMessage.parseErrorMessage(errorBody))
            }
        } catch (e: Exception) {
            NetworkResult.Error(ParseErrorMessage.getNetworkErrorMessage(e))
        }
    }
}