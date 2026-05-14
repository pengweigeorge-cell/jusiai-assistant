package com.jusiai.assistant.feature.aicall.data

import com.jusiai.assistant.data.auth.AuthInterceptor
import com.jusiai.assistant.feature.aicall.model.AiAgentConfigResponse
import com.jusiai.assistant.feature.aicall.model.StartAgentRequest
import com.jusiai.assistant.feature.aicall.model.StopAgentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AiAgentApi {
    @Headers("${AuthInterceptor.NO_AUTH}: 1")
    @GET("api/v1.0/rooms/ai-agent-config/")
    suspend fun fetchConfig(): AiAgentConfigResponse

    @POST("api/v1.0/rooms/{roomId}/start-ai-agent/")
    suspend fun startAgent(
        @Path("roomId") roomId: String,
        @Body body: StartAgentRequest,
    )

    @POST("api/v1.0/rooms/{roomId}/stop-ai-agent/")
    suspend fun stopAgent(
        @Path("roomId") roomId: String,
        @Body body: StopAgentRequest,
    )
}
