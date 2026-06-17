package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = productRepository.getFavorites()
}