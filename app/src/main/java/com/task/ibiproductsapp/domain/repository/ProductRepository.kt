package com.task.ibiproductsapp.domain.repository

import androidx.paging.PagingData
import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getPagedProducts(
        searchQuery: String,
        category: String?,
        sortOption: String
    ): Flow<PagingData<Product>>

    suspend fun getProductById(id: Int): NetworkResult<Product>

    suspend fun addProduct(product: Product): NetworkResult<Unit>
    suspend fun editProduct(product: Product): NetworkResult<Unit>
    suspend fun deleteProduct(id: Int): NetworkResult<Unit>
    suspend fun resetProduct(id: Int): NetworkResult<Unit>

    suspend fun refreshProducts(): NetworkResult<Unit>
    fun getCategories(): Flow<List<String>>

    // Favorites
    fun getFavorites(): Flow<List<Product>>
    suspend fun updateFavoriteStatus(productId: Int, isFavorite: Boolean): NetworkResult<Unit>
}