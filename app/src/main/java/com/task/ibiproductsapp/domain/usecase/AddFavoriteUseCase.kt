package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.repository.ProductRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Int): NetworkResult<Unit> =
        productRepository.updateFavoriteStatus(productId, isFavorite = true)
}