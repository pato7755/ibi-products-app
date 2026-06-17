package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.repository.ProductRepository
import javax.inject.Inject

class ResetProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: Int): NetworkResult<Unit> =
        productRepository.resetProduct(id)
}