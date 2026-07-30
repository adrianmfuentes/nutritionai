package com.health.nutritionai.data.remote.interceptor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.health.nutritionai.BuildConfig
import com.health.nutritionai.util.Constants
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

private data class RefreshRequestBody(val refreshToken: String)
private data class RefreshResponseBody(val token: String?, val refreshToken: String?)

/**
 * On a 401, swaps the expired short-lived access token for a new one using the
 * long-lived refresh token, synchronously (Authenticator runs off the main thread
 * already, so a blocking call here is the standard OkHttp pattern). If refresh
 * fails, the session is cleared and [onSessionExpired] fires so the UI can
 * route back to login.
 */
class TokenAuthenticator(
    private val context: Context,
    private val onSessionExpired: () -> Unit
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRIES = 1
    }

    private val gson = Gson()
    private val refreshUrl = BuildConfig.API_BASE_URL + "/auth/refresh"
    private val rawClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) > MAX_RETRIES) return null

        val prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val refreshToken = prefs.getString(Constants.KEY_REFRESH_TOKEN, null) ?: run {
            onSessionExpired()
            return null
        }

        synchronized(this) {
            // Another request may have already refreshed while we waited for the lock
            val currentAccessToken = prefs.getString(Constants.KEY_AUTH_TOKEN, null)
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val newTokens = performRefresh(refreshToken)
            if (newTokens == null) {
                prefs.edit().remove(Constants.KEY_AUTH_TOKEN).remove(Constants.KEY_REFRESH_TOKEN).apply()
                onSessionExpired()
                return null
            }

            prefs.edit()
                .putString(Constants.KEY_AUTH_TOKEN, newTokens.first)
                .putString(Constants.KEY_REFRESH_TOKEN, newTokens.second)
                .apply()

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.first}")
                .build()
        }
    }

    private fun performRefresh(refreshToken: String): Pair<String, String>? {
        return try {
            val body = gson.toJson(RefreshRequestBody(refreshToken))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(refreshUrl).post(body).build()

            rawClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "Refresh failed with code ${resp.code}")
                    return null
                }
                val parsed = gson.fromJson(resp.body.string(), RefreshResponseBody::class.java)
                if (parsed?.token != null && parsed.refreshToken != null) {
                    parsed.token to parsed.refreshToken
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
