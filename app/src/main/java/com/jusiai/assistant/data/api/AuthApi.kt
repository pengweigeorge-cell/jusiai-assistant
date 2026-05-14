package com.jusiai.assistant.data.api

import com.jusiai.assistant.data.api.dto.SendOtpRequest
import com.jusiai.assistant.data.api.dto.SendOtpResponse
import com.jusiai.assistant.data.api.dto.VerifyOtpRequest
import com.jusiai.assistant.data.api.dto.VerifyOtpResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Mobile authentication endpoints. These do NOT require an Authorization
 * header — the AuthInterceptor only attaches a Bearer token when one is
 * already stored, so anonymous calls go through unmodified.
 */
interface AuthApi {

    @POST("api/mobile/auth/send-otp/")
    suspend fun sendOtp(@Body body: SendOtpRequest): SendOtpResponse

    @POST("api/mobile/auth/verify-otp/")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): VerifyOtpResponse
}
