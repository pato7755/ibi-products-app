package com.task.ibiproductsapp.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("expiresInMins") val expiresInMins: Int = 30
)