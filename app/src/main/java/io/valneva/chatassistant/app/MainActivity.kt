package io.valneva.chatassistant.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.valneva.chatassistant.app.navigation.AppRoot
import io.valneva.chatassistant.app.startup.AppLaunchViewModel
import io.valneva.chatassistant.designsystem.theme.ChatAssistantTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appLaunchViewModel: AppLaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !appLaunchViewModel.uiState.value.isReady
        }

        enableEdgeToEdge()

        setContent {
            val uiState = appLaunchViewModel.uiState.collectAsStateWithLifecycle().value

            ChatAssistantTheme {
                AppRoot(startDestination = uiState.startDestination)
            }
        }
    }
}
