package com.jusiai.assistant.feature.aicall.data

import com.jusiai.assistant.feature.aicall.model.AgentRequestConfig
import com.jusiai.assistant.feature.aicall.model.AiAgentConfigResponse
import com.jusiai.assistant.feature.aicall.model.StartAgentRequest
import com.jusiai.assistant.feature.aicall.model.StopAgentRequest

class AiAgentRepository(private val api: AiAgentApi) {

    suspend fun fetchConfig(): AiAgentConfigResponse = api.fetchConfig()

    suspend fun startAgent(
        roomId: String,
        livekitToken: String,
        provider: String,
        voice: String?,
        promptLabel: String?,
    ) {
        api.startAgent(
            roomId,
            StartAgentRequest(
                token = livekitToken,
                provider = provider,
                config = AgentRequestConfig(
                    voice = voice,
                    prompt_label = promptLabel,
                    prompt_content = null,
                ),
            ),
        )
    }

    suspend fun stopAgent(roomId: String, livekitToken: String): Result<Unit> = runCatching {
        api.stopAgent(roomId, StopAgentRequest(token = livekitToken))
    }
}
