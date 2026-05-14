package com.jusiai.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jusiai.assistant.data.auth.SessionState
import com.jusiai.assistant.feature.aicall.ui.AiCallScreen
import com.jusiai.assistant.ui.login.LoginScreen
import com.jusiai.assistant.ui.theme.AssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssistantTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppEntry()
                }
            }
        }
    }
}

/**
 * Minimal navigation gate: show [LoginScreen] until the user has a valid
 * token, then drop into [AiCallScreen]. Session-expired signals from the
 * network layer bounce the user back to login.
 */
@Composable
private fun AppEntry() {
    val app = LocalContext.current.applicationContext as AssistantApp
    var loggedIn by remember { mutableStateOf(app.authRepository.isLoggedIn()) }

    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            SessionState.expired.collect { expired ->
                if (expired) {
                    SessionState.reset()
                    loggedIn = false
                }
            }
        }
    }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
    } else {
        AiCallScreen(
            onSignOut = {
                app.authRepository.signOut()
                loggedIn = false
            },
        )
    }
}
