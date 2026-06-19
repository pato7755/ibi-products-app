package com.task.ibiproductsapp.domain.usecase

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddFavoriteUseCasesTest {

    private lateinit var productRepository: ProductRepository

    @Before
    fun setup() {
        productRepository = mockk()
    }

    @Test
    fun `AddFavoriteUseCase calls updateFavoriteStatus with true`() = runTest {
        coEvery {
            productRepository.updateFavoriteStatus(1, true)
        } returns NetworkResult.Success(Unit)
        val useCase = AddFavoriteUseCase(productRepository)

        val result = useCase(1)

        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { productRepository.updateFavoriteStatus(1, true) }
    }
}