package com.jusiai.assistant.feature.aicall.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val name: String,
    val access_level: String = "public",
)

@JsonClass(generateAdapter = true)
data class LiveKitInfoDto(
    val url: String,
    val room: String?,
    val token: String,
)

@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    val id: String,
    val slug: String? = null,
    val livekit: LiveKitInfoDto? = null,
)

@JsonClass(generateAdapter = true)
data class AiProviderDto(
    val value: String,
    val label: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiVoiceDto(
    val value: String,
    val label: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiProviderDefault(
    val voice: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiPromptDto(
    val label: String,
    val content: String? = null,
    val category: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiAgentConfigResponse(
    val providers: List<AiProviderDto> = emptyList(),
    val voices: Map<String, List<AiVoiceDto>> = emptyMap(),
    val defaults: Map<String, AiProviderDefault> = emptyMap(),
    val prompts: List<AiPromptDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class AgentRequestConfig(
    val voice: String? = null,
    val prompt_label: String? = null,
    val prompt_content: String? = null,
)

@JsonClass(generateAdapter = true)
data class StartAgentRequest(
    val token: String,
    val provider: String? = null,
    val config: AgentRequestConfig? = null,
)

@JsonClass(generateAdapter = true)
data class StopAgentRequest(
    val token: String,
)
