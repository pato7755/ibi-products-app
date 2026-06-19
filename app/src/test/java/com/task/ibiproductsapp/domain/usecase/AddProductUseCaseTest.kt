package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddProductUseCaseTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var addProductUseCase: AddProductUseCase

    private val validProduct = Product(
        id = 1,
        title = "Test Product",
        description = "A test product",
        category = "test",
        price = 19.99,
        discountPercentage = 0.0,
        rating = 0.0,
        stock = 10,
        brand = "TestBrand",
        thumbnail = "https://example.com/thumb.png",
        images = emptyList(),
        tags = emptyList()
    )

    @Before
    fun setup() {
        productRepository = mockk()
        addProductUseCase = AddProductUseCase(productRepository)
    }

    @Test
    fun `invoke returns error when title is blank`() = runTest {
        val product = validProduct.copy(title = "")

        val result = addProductUseCase(product)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Product title cannot be empty", (result as NetworkResult.Error).message)
        coVerify(exactly = 0) { productRepository.addProduct(any()) }
    }

    @Test
    fun `invoke returns error when price is zero`() = runTest {
        val product = validProduct.copy(price = 0.0)

        val result = addProductUseCase(product)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Price must be greater than zero", (result as NetworkResult.Error).message)
    }

    @Test
    fun `invoke returns error when price is negative`() = runTest {
        val product = validProduct.copy(price = -5.0)

        val result = addProductUseCase(product)

        assertTrue(result is NetworkResult.Error)
        coVerify(exactly = 0) { productRepository.addProduct(any()) }
    }

    @Test
    fun `invoke delegates to repository when product is valid`() = runTest {
        coEvery { productRepository.addProduct(validProduct) } returns NetworkResult.Success(Unit)

        val result = addProductUseCase(validProduct)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { productRepository.addProduct(validProduct) }
    }
}