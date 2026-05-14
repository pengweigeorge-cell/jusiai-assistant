package com.jusiai.assistant.util

import android.content.Context
import com.jusiai.assistant.R
import com.jusiai.assistant.data.api.dto.ApiErrorBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException

/**
 * Scope hint lets callers override a backend message for a specific endpoint.
 */
enum class ErrorScope {
    GENERIC,
    AUTH_SEND_OTP,
    AUTH_VERIFY_OTP,
}

private val errorBodyAdapter by lazy {
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiErrorBody::class.java)
}

/**
 * Translate a thrown exception from a Retrofit call into a user-facing
 * Chinese message.
 *
 * Precedence:
 * 1. `IOException` → 网络异常。
 * 2. Global HTTP overrides for 401 / 429.
 * 3. Backend-provided `error` / `detail` (mobile auth endpoints already
 *    localise these into Chinese).
 * 4. Family fallbacks (5xx → 服务异常、其他 → 出错了).
 */
fun Throwable.toUserMessage(
    context: Context,
    @Suppress("UNUSED_PARAMETER") scope: ErrorScope = ErrorScope.GENERIC,
): String = when (this) {
    is IOException -> context.getString(R.string.error_network)
    is HttpException -> translateHttp(this, context)
    else -> context.getString(R.string.error_unknown)
}

private fun translateHttp(
    e: HttpException,
    context: Context,
): String {
    val code = e.code()

    if (code == 401) return context.getString(R.string.error_auth_expired)
    if (code == 429) return context.getString(R.string.error_too_many_requests)

    val bodyMessage = parseBody(e)?.let { body ->
        body.error?.takeIf { it.isNotBlank() }
            ?: body.detail?.takeIf { it.isNotBlank() }
    }
    if (bodyMessage != null) return bodyMessage

    if (code in 500..599) return context.getString(R.string.error_server)
    return context.getString(R.string.error_unknown)
}

private fun parseBody(e: HttpException): ApiErrorBody? {
    val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    if (raw.isNullOrBlank()) return null
    return runCatching { errorBodyAdapter.fromJson(raw) }.getOrNull()
}
