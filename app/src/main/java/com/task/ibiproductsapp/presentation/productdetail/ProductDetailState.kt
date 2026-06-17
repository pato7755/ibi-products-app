package com.task.ibiproductsapp.presentation.productdetail

import com.task.ibiproductsapp.domain.model.Product

data class ProductDetailState(
    val product: Product? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)