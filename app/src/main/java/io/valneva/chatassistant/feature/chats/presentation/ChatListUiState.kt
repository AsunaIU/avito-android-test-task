package io.valneva.chatassistant.feature.chats.presentation

data class ChatListUiState(
    val searchQuery: String = "",
    val appliedQuery: String = "",
    val isCreatingChat: Boolean = false,
)
