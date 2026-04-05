package io.valneva.chatassistant.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@Composable
fun AppRoot(
    startDestination: String?,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        startDestination?.let { route ->
            AppNavHost(
                navController = navController,
                startDestination = route,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
            )
        }
    }
}
