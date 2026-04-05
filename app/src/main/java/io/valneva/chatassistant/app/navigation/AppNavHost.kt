package io.valneva.chatassistant.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.valneva.chatassistant.R
import io.valneva.chatassistant.feature.auth.presentation.login.LoginScreen
import io.valneva.chatassistant.feature.auth.presentation.register.RegisterScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(route = AppRoute.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AppRoute.Register.route)
                },
                onNavigateToChats = {
                    navController.navigateToMain(clearBackStack = true)
                },
            )
        }

        composable(route = AppRoute.Register.route) {
            RegisterScreen(
                onNavigateBack = navController::navigateUp,
                onNavigateToChats = {
                    navController.navigateToMain(clearBackStack = true)
                },
            )
        }

        composable(route = AppRoute.Main.route) {
            MainScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
            )
        }

        composable(
            route = AppRoute.Chat.route,
            arguments = listOf(
                navArgument(AppRoute.Chat.CHAT_ID_ARG) {
                    type = NavType.StringType
                },
            ),
        ) {
            PlaceholderScreen(title = stringResource(id = R.string.chat_placeholder_title))
        }
    }
}

private fun NavHostController.navigateToMain(clearBackStack: Boolean) {
    navigate(AppRoute.Main.route) {
        launchSingleTop = true
        restoreState = !clearBackStack

        if (clearBackStack) {
            popUpTo(graph.findStartDestination().id) {
                inclusive = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = title)
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(id = R.string.placeholder_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
