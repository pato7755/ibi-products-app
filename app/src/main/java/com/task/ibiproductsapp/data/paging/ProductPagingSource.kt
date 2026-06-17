package com.task.ibiproductsapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.task.ibiproductsapp.common.Constants
import com.task.ibiproductsapp.data.local.dao.ProductDao
import com.task.ibiproductsapp.data.mapper.toDomain
import com.task.ibiproductsapp.data.mapper.toEntity
import com.task.ibiproductsapp.data.remote.ApiService
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.model.SortOption

class ProductPagingSource(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val searchQuery: String,
    private val category: String?,
    private val sortOption: String
) : PagingSource<Int, Product>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val page = params.key ?: 0
        val skip = page * Constants.PAGE_SIZE

        val (sortBy, order) = SortOption.entries
            .find { it.key == sortOption }
            ?.toApiParams() ?: ("id" to "asc")

        return try {
            val response = when {
                searchQuery.isNotBlank() -> apiService.searchProducts(
                    query = searchQuery,
                    limit = params.loadSize,
                    skip = skip,
                    sortBy = sortBy,
                    order = order
                )
                category != null -> apiService.getProductsByCategory(
                    category = category,
                    limit = params.loadSize,
                    skip = skip,
                    sortBy = sortBy,
                    order = order
                )
                else -> apiService.getProducts(
                    limit = params.loadSize,
                    skip = skip,
                    sortBy = sortBy,
                    order = order
                )
            }

            if (response.isSuccessful) {
                val body = response.body()!!
                val dtos = body.products

                // Cache network results to Room
                productDao.upsertProducts(dtos.map { it.toEntity() })

                // Merge local-only products on first page only to avoid duplicates
                val localOnly = if (page == 0) {
                    val networkIds = dtos.map { it.id }.toSet()
                    productDao.getLocalProducts(
                        searchQuery = searchQuery,
                        category = category ?: ""
                    ).filter { it.id !in networkIds }
                } else {
                    emptyList()
                }

                val merged = localOnly.map { it.toDomain() } +
                        dtos.map { it.toEntity().toDomain() }

                LoadResult.Page(
                    data = merged,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (dtos.isEmpty() || skip + dtos.size >= body.total) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to load products"))
            }
        } catch (e: Exception) {
            // Offline fallback — serve from Room cache
            try {
                val cached = productDao.getAllProducts(
                    searchQuery = searchQuery,
                    category = category ?: "",
                    skip = skip,
                    limit = params.loadSize
                )
                LoadResult.Page(
                    data = cached.map { it.toDomain() },
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (cached.isEmpty()) null else page + 1
                )
            } catch (cacheException: Exception) {
                LoadResult.Error(e)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}