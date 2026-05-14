package com.jusiai.assistant.data.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Catches HTTP 401 on authenticated requests, clears stored credentials, and
 * broadcasts a session-expired signal via [SessionState]. Only authenticated
 * requests trigger the signal: presence of the Authorization header is the
 * marker (set upstream by [AuthInterceptor]).
 */
class SessionExpiredInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && request.header("Authorization") != null) {
            tokenStore.clear()
            SessionState.markExpired()
        }
        return response
    }
}
