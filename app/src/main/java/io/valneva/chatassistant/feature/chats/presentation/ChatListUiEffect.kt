package io.valneva.chatassistant.feature.chats.presentation

sealed interface ChatListUiEffect {
    data class OpenChat(val chatId: String) : ChatListUiEffect
}
