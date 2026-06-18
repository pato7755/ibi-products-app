package com.task.ibiproductsapp.presentation.product

data class SearchParams(
    val searchQuery: String = "",
    val category: String? = null,
    val sortOption: String = "default"
)