package io.valneva.chatassistant.app.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Main : AppRoute("main")
    data object Chats : AppRoute("chats")
    data object Profile : AppRoute("profile")
    data object Images : AppRoute("images")

    data object Chat : AppRoute("chat/{chatId}") {
        const val CHAT_ID_ARG = "chatId"

        fun createRoute(chatId: String): String = "chat/$chatId"
    }
}
