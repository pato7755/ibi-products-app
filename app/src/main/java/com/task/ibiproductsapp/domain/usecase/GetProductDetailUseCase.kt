package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductDetailUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: Int): NetworkResult<Product> =
        productRepository.getProductById(id)
}