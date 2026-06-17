package com.task.ibiproductsapp.common

object Routes {
    const val LOGIN = "login"
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val ADD_PRODUCT = "product/add"
    const val EDIT_PRODUCT = "product/edit/{productId}"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"

    fun productDetail(id: Int) = "product/$id"
    fun editProduct(id: Int) = "product/edit/$id"
}