package com.task.ibiproductsapp.presentation.login.favorite

import com.task.ibiproductsapp.domain.model.Product

data class FavoritesState(
    val favorites: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val pendingRemovalIds: Set<Int> = emptySet()
)