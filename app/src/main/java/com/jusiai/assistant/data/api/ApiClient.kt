package com.jusiai.assistant.data.api

import com.jusiai.assistant.BuildConfig
import com.jusiai.assistant.data.auth.AuthInterceptor
import com.jusiai.assistant.data.auth.SessionExpiredInterceptor
import com.jusiai.assistant.data.auth.TokenStore
import com.jusiai.assistant.feature.aicall.data.AiAgentApi
import com.jusiai.assistant.feature.aicall.data.RoomApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Application-scoped Retrofit + OkHttp setup. Built once in [com.jusiai.assistant.AssistantApp]
 * and shared by all repositories — never re-instantiate per ViewModel.
 */
class ApiClient(tokenStore: TokenStore) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(tokenStore))
        .addInterceptor(SessionExpiredInterceptor(tokenStore))
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    }
                )
            }
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(normalizedBaseUrl(BuildConfig.JUSIAI_BASE_URL))
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val roomApi: RoomApi = retrofit.create(RoomApi::class.java)
    val aiAgentApi: AiAgentApi = retrofit.create(AiAgentApi::class.java)

    private fun normalizedBaseUrl(raw: String): String =
        if (raw.endsWith("/")) raw else "$raw/"
}
