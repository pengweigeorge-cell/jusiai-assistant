package com.jusiai.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jusiai.assistant.data.auth.SessionState
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
 * token, then drop into the placeholder "signed-in" screen. Session-expired
 * signals from the network layer bounce the user back to login.
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
        SignedInPlaceholder(
            phone = app.tokenStore.phone,
            onSignOut = {
                app.authRepository.signOut()
                loggedIn = false
            },
        )
    }
}

@Composable
private fun SignedInPlaceholder(
    phone: String?,
    onSignOut: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.logged_in_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.logged_in_signed_in_as,
                    phone?.let { "+86$it" } ?: "",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Text(
                    text = stringResource(R.string.logged_in_sign_out),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
