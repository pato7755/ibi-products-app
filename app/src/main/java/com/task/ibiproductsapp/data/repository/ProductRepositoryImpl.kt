package com.task.ibiproductsapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.task.ibiproductsapp.common.Constants
import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.common.ParseErrorMessage
import com.task.ibiproductsapp.data.local.dao.FavoriteDao
import com.task.ibiproductsapp.data.local.dao.ProductDao
import com.task.ibiproductsapp.data.local.entity.FavoriteEntity
import com.task.ibiproductsapp.data.mapper.toDomain
import com.task.ibiproductsapp.data.mapper.toEntity
import com.task.ibiproductsapp.data.paging.ProductPagingSource
import com.task.ibiproductsapp.data.remote.ApiService
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao
) : ProductRepository {

    override fun getPagedProducts(
        searchQuery: String,
        category: String?,
        sortOption: String
    ): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ProductPagingSource(
                    apiService = apiService,
                    productDao = productDao,
                    searchQuery = searchQuery,
                    category = category,
                    sortOption = sortOption
                )
            }
        ).flow
    }

    override suspend fun getProductById(id: Int): NetworkResult<Product> {
        return try {
            // If locally modified, serve from Room — don't overwrite with API data
            val cached = productDao.getProductById(id)
            if (cached != null && cached.isLocallyModified) {
                return NetworkResult.Success(cached.toDomain())
            }

            val response = apiService.getProductById(id)
            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return NetworkResult.Error("Empty response from server")
                productDao.upsertProduct(dto.toEntity())

                val entity = productDao.getProductById(id)
                    ?: return NetworkResult.Error("Product not found after save")
                NetworkResult.Success(entity.toDomain())
            } else {
                val cached = productDao.getProductById(id)
                if (cached != null) {
                    NetworkResult.Success(cached.toDomain())
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    NetworkResult.Error(ParseErrorMessage.parseErrorMessage(errorBody))
                }
            }
        } catch (e: Exception) {
            val cached = productDao.getProductById(id)
            if (cached != null) {
                NetworkResult.Success(cached.toDomain())
            } else {
                NetworkResult.Error(ParseErrorMessage.getNetworkErrorMessage(e))
            }
        }
    }

    override fun getFavorites(): Flow<List<Product>> {
        return favoriteDao.getFavoriteIds().map { ids ->
            ids.mapNotNull { id ->
                productDao.getProductById(id)?.toDomain()
            }
        }
    }

    override suspend fun updateFavoriteStatus(
        productId: Int,
        isFavorite: Boolean
    ): NetworkResult<Unit> {
        return try {
            if (isFavorite) favoriteDao.addFavorite(FavoriteEntity(productId))
            else favoriteDao.removeFavorite(productId)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update favorite")
        }
    }

    override suspend fun addProduct(product: Product): NetworkResult<Unit> {
        return try {
            productDao.upsertProduct(product.toEntity())
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add product")
        }
    }

    override suspend fun editProduct(product: Product): NetworkResult<Unit> {
        return try {
            productDao.upsertProduct(product.toEntity())
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to edit product")
        }
    }

    override suspend fun deleteProduct(id: Int): NetworkResult<Unit> {
        return try {
            productDao.deleteProduct(id)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to delete product")
        }
    }

    override suspend fun resetProduct(id: Int): NetworkResult<Unit> {
        return try {
            val response = apiService.getProductById(id)
            if (response.isSuccessful) {
                val dto = response.body()
                    ?: return NetworkResult.Error("Empty response from server")
                productDao.upsertProduct(dto.toEntity())
                NetworkResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                NetworkResult.Error(ParseErrorMessage.parseErrorMessage(errorBody))
            }
        } catch (e: Exception) {
            NetworkResult.Error(ParseErrorMessage.getNetworkErrorMessage(e))
        }
    }

    override suspend fun refreshProducts(): NetworkResult<Unit> {
        return try {
            var skip = 0
            var total: Int
            do {
                val response = apiService.getProducts(
                    limit = Constants.PAGE_SIZE,
                    skip = skip
                )
                if (response.isSuccessful) {
                    val body = response.body()
                        ?: return NetworkResult.Error("Empty response from server")
                    total = body.total
                    productDao.upsertProducts(body.products.map { it.toEntity() })
                    skip += Constants.PAGE_SIZE
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    return NetworkResult.Error(ParseErrorMessage.parseErrorMessage(errorBody))
                }
            } while (skip < total)

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(ParseErrorMessage.getNetworkErrorMessage(e))
        }
    }

    override fun getCategories(): Flow<List<String>> = productDao.getCategories()
}