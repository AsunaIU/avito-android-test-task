package io.valneva.chatassistant.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import io.valneva.chatassistant.app.navigation.AppRoot
import io.valneva.chatassistant.designsystem.theme.ChatAssistantTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ChatAssistantTheme {
                AppRoot()
            }
        }
    }
}