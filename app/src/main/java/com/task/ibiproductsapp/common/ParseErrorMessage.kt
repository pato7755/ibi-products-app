package com.task.ibiproductsapp.common

import com.google.gson.Gson

data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: String? = null
)

object ParseErrorMessage {

    private val gson = Gson()

    fun parseErrorMessage(responseBody: String): String {
        return try {
            val errorObj = gson.fromJson(responseBody, ApiErrorResponse::class.java)
            errorObj.error ?: errorObj.message ?: errorObj.details ?: "Unknown error"
        } catch (e: Exception) {
            responseBody.take(200)
        }
    }

    fun getNetworkErrorMessage(e: Exception): String {
        return when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timed out"
            is java.io.IOException -> "Network error. Please try again"
            else -> e.message ?: "Something went wrong"
        }
    }
}