package com.task.ibiproductsapp.data.remote

import com.task.ibiproductsapp.data.remote.dto.request.LoginRequestDto
import com.task.ibiproductsapp.data.remote.dto.request.ProductDto
import com.task.ibiproductsapp.data.remote.dto.response.LoginResponseDto
import com.task.ibiproductsapp.data.remote.dto.response.ProductsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String = "id",
        @Query("order") order: String = "asc",
        @Query("select") select: String = "id,title,description,category,price,discountPercentage,rating,stock,brand,thumbnail,images,tags"
    ): Response<ProductsResponseDto>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String = "id",
        @Query("order") order: String = "asc",
    ): Response<ProductsResponseDto>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductDto>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("sortBy") sortBy: String = "id",
        @Query("order") order: String = "asc",
    ): Response<ProductsResponseDto>
}