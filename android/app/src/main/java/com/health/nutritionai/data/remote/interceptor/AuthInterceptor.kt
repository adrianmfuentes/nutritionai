package com.health.nutritionai.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val request = originalRequest.newBuilder()

        // Only add Authorization header if we have a token
        // and the request is not to auth endpoints (login/register/refresh)
        val isAuthEndpoint = originalRequest.url.encodedPath.contains("/auth/")

        if (token != null && !isAuthEndpoint) {
            request.addHeader("Authorization", "Bearer $token")
            Log.d(TAG, "Added auth header to: ${originalRequest.url.encodedPath}")
        } else if (token == null && !isAuthEndpoint) {
            Log.d(TAG, "No token available for: ${originalRequest.url.encodedPath}")
        }

        // 401s on non-auth endpoints are handled by TokenAuthenticator, which
        // transparently refreshes and retries before a response ever reaches here.
        return chain.proceed(request.build())
    }
}
