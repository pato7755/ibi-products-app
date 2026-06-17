package com.task.ibiproductsapp.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import com.task.ibiproductsapp.data.remote.dto.request.ProductDto

data class ProductsResponseDto(
    @SerializedName("products") val products: List<ProductDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("skip") val skip: Int,
    @SerializedName("limit") val limit: Int
)