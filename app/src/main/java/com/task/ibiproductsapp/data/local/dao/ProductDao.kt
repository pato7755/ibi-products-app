package com.task.ibiproductsapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.task.ibiproductsapp.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query(
        """
    SELECT * FROM products
    WHERE (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%')
    AND (:category = '' OR category = :category)
    ORDER BY
        CASE WHEN :sortOption = 'price_asc' THEN price END ASC,
        CASE WHEN :sortOption = 'price_desc' THEN price END DESC,
        CASE WHEN :sortOption = 'rating' THEN rating END DESC,
        CASE WHEN :sortOption = 'name' THEN title END ASC,
        id ASC
        """
    )
    fun getPagedProducts(
        searchQuery: String,
        category: String?,
        sortOption: String
    ): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Upsert
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("DELETE FROM products WHERE isLocallyModified = 0")
    suspend fun clearNonModifiedProducts()

    @Query("""
    SELECT * FROM products
    WHERE (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%')
    AND (:category = '' OR category = :category)
    LIMIT :limit OFFSET :skip
    """)
    suspend fun getAllProducts(
        searchQuery: String,
        category: String,
        skip: Int,
        limit: Int
    ): List<ProductEntity>

    @Query("""
    SELECT * FROM products
    WHERE isLocallyModified = 1
    AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%')
    AND (:category = '' OR category = :category)
    """)
    suspend fun getLocalProducts(
        searchQuery: String,
        category: String
    ): List<ProductEntity>
}