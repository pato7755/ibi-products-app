package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): NetworkResult<Unit> {
        if (product.title.isBlank()) return NetworkResult.Error("Product title cannot be empty")
        if (product.price <= 0) return NetworkResult.Error("Price must be greater than zero")
        return productRepository.addProduct(product)
    }
}