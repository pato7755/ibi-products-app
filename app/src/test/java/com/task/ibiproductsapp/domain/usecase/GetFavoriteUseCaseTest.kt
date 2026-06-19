package com.task.ibiproductsapp.domain.usecase

import app.cash.turbine.test
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetFavoriteUseCasesTest {

    private lateinit var productRepository: ProductRepository

    private val sampleProduct = Product(
        id = 1,
        title = "Sample",
        description = "desc",
        category = "cat",
        price = 9.99,
        discountPercentage = 0.0,
        rating = 4.5,
        stock = 5,
        brand = null,
        thumbnail = "",
        images = emptyList(),
        tags = emptyList()
    )

    @Before
    fun setup() {
        productRepository = mockk()
    }

    @Test
    fun `GetFavoritesUseCase emits favorites from repository`() = runTest {
        every { productRepository.getFavorites() } returns flowOf(listOf(sampleProduct))
        val useCase = GetFavoritesUseCase(productRepository)

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(sampleProduct, result.first())
            awaitComplete()
        }
    }
}