package com.jusiai.assistant

import android.app.Application
import com.jusiai.assistant.data.api.ApiClient
import com.jusiai.assistant.data.auth.TokenStore
import com.jusiai.assistant.data.repository.AuthRepository

/**
 * Hand-rolled service locator for the login surface. Holds the small set of
 * singletons the UI layer reads from — TokenStore, ApiClient, AuthRepository.
 * Swap to Hilt if the app grows beyond a couple of screens.
 */
class AssistantApp : Application() {

    lateinit var tokenStore: TokenStore
        private set
    lateinit var apiClient: ApiClient
        private set
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(this)
        apiClient = ApiClient(tokenStore)
        authRepository = AuthRepository(apiClient.authApi, tokenStore)
    }
}
