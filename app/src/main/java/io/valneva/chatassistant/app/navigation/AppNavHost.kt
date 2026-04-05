package io.valneva.chatassistant.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.valneva.chatassistant.feature.auth.presentation.login.LoginScreen
import io.valneva.chatassistant.feature.auth.presentation.register.RegisterScreen
import io.valneva.chatassistant.feature.chat.presentation.ChatScreen

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
                onOpenChat = { chatId ->
                    navController.navigate(AppRoute.Chat.createRoute(chatId))
                },
                onSignedOut = {
                    navController.navigateToLogin(clearBackStack = true)
                },
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
            ChatScreen(onNavigateBack = navController::navigateUp)
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

private fun NavHostController.navigateToLogin(clearBackStack: Boolean) {
    navigate(AppRoute.Login.route) {
        launchSingleTop = true

        if (clearBackStack) {
            popUpTo(AppRoute.Main.route) {
                inclusive = true
            }
        }
    }
}
