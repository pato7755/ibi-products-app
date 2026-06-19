package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveFavoriteUseCasesTest {

    private lateinit var productRepository: ProductRepository

    @Before
    fun setup() {
        productRepository = mockk()
    }

    @Test
    fun `RemoveFavoriteUseCase calls updateFavoriteStatus with false`() = runTest {
        coEvery {
            productRepository.updateFavoriteStatus(1, false)
        } returns NetworkResult.Success(Unit)
        val useCase = RemoveFavoriteUseCase(productRepository)

        val result = useCase(1)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { productRepository.updateFavoriteStatus(1, false) }
    }

    @Test
    fun `RemoveFavoriteUseCase propagates repository error`() = runTest {
        coEvery {
            productRepository.updateFavoriteStatus(1, false)
        } returns NetworkResult.Error("Failed to update favorite")
        val useCase = RemoveFavoriteUseCase(productRepository)

        val result = useCase(1)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Failed to update favorite", (result as NetworkResult.Error).message)
    }
}