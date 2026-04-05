package io.valneva.chatassistant.feature.chat.presentation

data class ConversationUiState(
    val title: String = "",
    val input: String = "",
    val isSending: Boolean = false,
    val isMissingChat: Boolean = false,
)
