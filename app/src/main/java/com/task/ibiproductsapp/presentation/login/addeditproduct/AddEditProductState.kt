package com.task.ibiproductsapp.presentation.login.addeditproduct

data class AddEditProductState(
    val productId: Int? = null, // null = add mode, non-null = edit mode
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val brand: String = "",
    val stock: String = "",
    val discountPercentage: String = "",
    val rating: String = "",
    val thumbnail: String = "",
    val images: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val isLocallyModified: Boolean = false
)