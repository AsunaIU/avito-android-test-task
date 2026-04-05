package io.valneva.chatassistant.feature.chats.presentation

sealed interface ChatsUiEffect {
    data class OpenChat(val chatId: String) : ChatsUiEffect
}
