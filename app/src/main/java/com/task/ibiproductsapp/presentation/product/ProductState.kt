package com.task.ibiproductsapp.presentation.product

import com.task.ibiproductsapp.domain.model.Product

data class ProductState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortOption: String = "default",
    val categories: List<String> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val recentlyDeletedProduct: Product? = null
)