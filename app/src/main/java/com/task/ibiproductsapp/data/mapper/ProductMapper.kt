package com.task.ibiproductsapp.data.mapper

import com.task.ibiproductsapp.data.local.entity.ProductEntity
import com.task.ibiproductsapp.data.remote.dto.request.ProductDto
import com.task.ibiproductsapp.data.remote.dto.response.LoginResponseDto
import com.task.ibiproductsapp.domain.model.AuthToken
import com.task.ibiproductsapp.domain.model.Product

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    brand = brand,
    thumbnail = thumbnail,
    images = images.joinToString(","),
    tags = tags.joinToString(","),
    isLocallyModified = false
)

// ProductEntity → Domain Product
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    brand = brand,
    thumbnail = thumbnail,
    images = images.split(",").filter { it.isNotBlank() },
    tags = tags.split(",").filter { it.isNotBlank() },
    isFavorite = false,
    isLocallyModified = isLocallyModified
)

// Domain Product → ProductEntity (for local CRUD operations)
fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    brand = brand,
    thumbnail = thumbnail,
    images = images.joinToString(","),
    tags = tags.joinToString(","),
    isLocallyModified = true
)

// LoginResponseDto → Domain AuthToken
fun LoginResponseDto.toDomain(): AuthToken = AuthToken(
    token = accessToken,
    refreshToken = refreshToken,
    userId = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    image = image
)