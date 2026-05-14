package com.jusiai.assistant

import android.app.Application
import com.jusiai.assistant.data.api.ApiClient
import com.jusiai.assistant.data.auth.TokenStore
import com.jusiai.assistant.data.repository.AuthRepository
import com.jusiai.assistant.feature.aicall.data.AiAgentRepository
import com.jusiai.assistant.feature.aicall.data.AiCallPreferences
import com.jusiai.assistant.feature.aicall.data.AiRoomRepository

/**
 * Hand-rolled service locator. Holds the small set of singletons the UI layer
 * reads from — TokenStore, ApiClient, AuthRepository, plus the AI-call feature
 * repositories. Swap to Hilt if the app grows beyond a handful of screens.
 */
class AssistantApp : Application() {

    lateinit var tokenStore: TokenStore
        private set
    lateinit var apiClient: ApiClient
        private set
    lateinit var authRepository: AuthRepository
        private set

    lateinit var aiRoomRepository: AiRoomRepository
        private set
    lateinit var aiAgentRepository: AiAgentRepository
        private set
    lateinit var aiCallPreferences: AiCallPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(this)
        apiClient = ApiClient(tokenStore)
        authRepository = AuthRepository(apiClient.authApi, tokenStore)

        aiRoomRepository = AiRoomRepository(apiClient.roomApi)
        aiAgentRepository = AiAgentRepository(apiClient.aiAgentApi)
        aiCallPreferences = AiCallPreferences(this)
    }
}
