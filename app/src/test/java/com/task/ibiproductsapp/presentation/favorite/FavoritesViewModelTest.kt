package com.task.ibiproductsapp.presentation.favorite

import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.usecase.GetFavoritesUseCase
import com.task.ibiproductsapp.domain.usecase.RemoveFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getFavoritesUseCase: GetFavoritesUseCase
    private lateinit var removeFavoriteUseCase: RemoveFavoriteUseCase
    private lateinit var viewModel: FavoritesViewModel

    private val favoritesFlow = MutableStateFlow(
        listOf(productOf(1), productOf(2), productOf(3))
    )

    private fun productOf(id: Int) = Product(
        id = id,
        title = "Product $id",
        description = "desc",
        category = "cat",
        price = 9.99,
        discountPercentage = 0.0,
        rating = 4.0,
        stock = 10,
        brand = null,
        thumbnail = "",
        images = emptyList(),
        tags = emptyList()
    )

    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)

        getFavoritesUseCase = mockk()
        removeFavoriteUseCase = mockk()

        every { getFavoritesUseCase() } returns favoritesFlow
        coEvery { removeFavoriteUseCase(any()) } returns NetworkResult.Success(Unit)

        viewModel = FavoritesViewModel(
            getFavoritesUseCase = getFavoritesUseCase,
            removeFavoriteUseCase = removeFavoriteUseCase,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `removeFavorite adds product to pendingRemovalIds immediately`() = runTest(testDispatcher) {
        viewModel.removeFavorite(1)

        val state = viewModel.favoritesState.value
        assertTrue(state.pendingRemovalIds.contains(1))
    }

    @Test
    fun `removeFavorite actually deletes from repository after delay`() = runTest(testDispatcher) {
        viewModel.removeFavorite(1)
        advanceTimeBy(4001)

        coVerify(exactly = 1) { removeFavoriteUseCase(1) }
    }

    @Test
    fun `undoRemove cancels pending deletion and removes id from pendingRemovalIds`() =
        runTest(testDispatcher) {
            viewModel.removeFavorite(1)
            assertTrue(viewModel.favoritesState.value.pendingRemovalIds.contains(1))

            viewModel.undoRemove(1)

            assertFalse(viewModel.favoritesState.value.pendingRemovalIds.contains(1))
            advanceTimeBy(4001)
            coVerify(exactly = 0) { removeFavoriteUseCase(1) }
        }

    // This is the core regression test for the concurrent-removal bug:
    // removing multiple items in quick succession must give each its own
    // independent timer — a later removal must not cancel an earlier one's job.
    @Test
    fun `concurrent removals each get their own independent job and all complete`() =
        runTest(testDispatcher) {
            viewModel.removeFavorite(1)
            advanceTimeBy(1000)
            viewModel.removeFavorite(2)
            advanceTimeBy(1000)
            viewModel.removeFavorite(3)

            // All three should be pending at this point
            val midState = viewModel.favoritesState.value
            assertTrue(midState.pendingRemovalIds.contains(1))
            assertTrue(midState.pendingRemovalIds.contains(2))
            assertTrue(midState.pendingRemovalIds.contains(3))

            // Let every timer fully elapse
            advanceTimeBy(5000)

            // All three deletions must have actually executed — none silently dropped
            coVerify(exactly = 1) { removeFavoriteUseCase(1) }
            coVerify(exactly = 1) { removeFavoriteUseCase(2) }
            coVerify(exactly = 1) { removeFavoriteUseCase(3) }
        }

    @Test
    fun `undoing one item does not cancel the removal of a different item`() =
        runTest(testDispatcher) {
            viewModel.removeFavorite(1)
            viewModel.removeFavorite(2)

            viewModel.undoRemove(1)
            advanceTimeBy(4001)

            coVerify(exactly = 0) { removeFavoriteUseCase(1) }
            coVerify(exactly = 1) { removeFavoriteUseCase(2) }
        }

    @Test
    fun `pendingRemovalIds only clears once Room flow confirms deletion`() =
        runTest(testDispatcher) {
            viewModel.removeFavorite(1)
            advanceTimeBy(4001)

            // Simulate Room's Flow emitting the updated list without product 1
            favoritesFlow.value = listOf(productOf(2), productOf(3))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.favoritesState.value
            assertFalse(state.pendingRemovalIds.contains(1))
            assertEquals(2, state.favorites.size)
        }
}