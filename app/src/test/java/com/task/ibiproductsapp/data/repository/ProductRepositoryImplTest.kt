package com.task.ibiproductsapp.data.repository

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.data.local.dao.FavoriteDao
import com.task.ibiproductsapp.data.local.dao.ProductDao
import com.task.ibiproductsapp.data.local.entity.FavoriteEntity
import com.task.ibiproductsapp.data.local.entity.ProductEntity
import com.task.ibiproductsapp.data.remote.ApiService
import com.task.ibiproductsapp.data.remote.dto.request.ProductDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProductRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var productDao: ProductDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var repository: ProductRepositoryImpl

    private val sampleDto = ProductDto(
        id = 1,
        title = "Test Product",
        description = "desc",
        category = "beauty",
        price = 9.99,
        discountPercentage = 10.0,
        rating = 4.5,
        stock = 50,
        brand = "TestBrand",
        thumbnail = "https://example.com/thumb.png",
        images = listOf("https://example.com/1.png"),
        tags = listOf("tag1")
    )

    private val sampleEntity = ProductEntity(
        id = 1,
        title = "Test Product",
        description = "desc",
        category = "beauty",
        price = 9.99,
        discountPercentage = 10.0,
        rating = 4.5,
        stock = 50,
        brand = "TestBrand",
        thumbnail = "https://example.com/thumb.png",
        images = "https://example.com/1.png",
        tags = "tag1",
        isLocallyModified = false
    )

    @Before
    fun setup() {
        apiService = mockk()
        productDao = mockk()
        favoriteDao = mockk()
        repository = ProductRepositoryImpl(apiService, productDao, favoriteDao)
    }

    @Test
    fun `getProductById returns success and caches to Room on successful network response`() =
        runTest {
            coEvery { apiService.getProductById(1) } returns Response.success(sampleDto)
            coEvery { productDao.upsertProduct(any()) } returns Unit
            coEvery { productDao.getProductById(1) } returns sampleEntity

            val result = repository.getProductById(1)

            assertTrue(result is NetworkResult.Success)
            assertEquals("Test Product", (result as NetworkResult.Success).data.title)
            coVerify(exactly = 1) { productDao.upsertProduct(any()) }
        }

    @Test
    fun `getProductById falls back to cache when network call fails`() = runTest {
        coEvery { apiService.getProductById(1) } throws java.io.IOException("No network")
        coEvery { productDao.getProductById(1) } returns sampleEntity

        val result = repository.getProductById(1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.id)
    }

    @Test
    fun `getProductById returns error when network fails and no cache exists`() = runTest {
        coEvery { apiService.getProductById(1) } throws java.io.IOException("No network")
        coEvery { productDao.getProductById(1) } returns null

        val result = repository.getProductById(1)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getFavorites maps favorite ids to products via productDao`() = runTest {
        every { favoriteDao.getFavoriteIds() } returns flowOf(listOf(1))
        coEvery { productDao.getProductById(1) } returns sampleEntity

        val result = repository.getFavorites()

        result.collect { products ->
            assertEquals(1, products.size)
            assertEquals(1, products.first().id)
        }
    }

    @Test
    fun `updateFavoriteStatus true adds favorite to favoriteDao`() = runTest {
        coEvery { favoriteDao.addFavorite(FavoriteEntity(1)) } returns Unit

        val result = repository.updateFavoriteStatus(1, true)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { favoriteDao.addFavorite(FavoriteEntity(1)) }
    }

    @Test
    fun `updateFavoriteStatus false removes favorite from favoriteDao`() = runTest {
        coEvery { favoriteDao.removeFavorite(1) } returns Unit

        val result = repository.updateFavoriteStatus(1, false)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { favoriteDao.removeFavorite(1) }
        coVerify(exactly = 0) { favoriteDao.addFavorite(any()) }
    }

    @Test
    fun `deleteProduct calls productDao deleteProduct`() = runTest {
        coEvery { productDao.deleteProduct(1) } returns Unit

        val result = repository.deleteProduct(1)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { productDao.deleteProduct(1) }
    }
}