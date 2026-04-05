package io.valneva.chatassistant.feature.chats.presentation

data class ChatsUiState(
    val searchQuery: String = "",
    val appliedQuery: String = "",
    val isCreatingChat: Boolean = false,
)
