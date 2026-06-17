package com.task.ibiproductsapp.data.remote

import com.task.ibiproductsapp.util.DataStoreHelper
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val dataStoreHelper: DataStoreHelper
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = dataStoreHelper.getAuthToken()
        val request = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
