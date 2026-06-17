package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.AuthToken
import com.task.ibiproductsapp.domain.model.LoginCredentials
import com.task.ibiproductsapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credentials: LoginCredentials): NetworkResult<AuthToken> {
        return authRepository.login(credentials)
    }
}