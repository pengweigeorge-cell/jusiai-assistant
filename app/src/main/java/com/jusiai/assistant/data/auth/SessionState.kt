package com.jusiai.assistant.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global flag for "the backend told us our Bearer token is no longer valid".
 * Set by [SessionExpiredInterceptor] the moment any authed request comes back
 * 401; observed by the UI layer to prompt the user to sign in again.
 */
object SessionState {
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    fun markExpired() {
        _expired.value = true
    }

    fun reset() {
        _expired.value = false
    }
}
