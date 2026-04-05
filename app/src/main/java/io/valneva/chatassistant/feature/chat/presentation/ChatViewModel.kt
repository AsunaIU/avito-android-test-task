package io.valneva.chatassistant.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.app.navigation.AppRoute
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.chat.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authInteractor: AuthInteractor,
    private val repository: ChatRepository,
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle[AppRoute.Chat.CHAT_ID_ARG])
    private val currentUserId: String? = authInteractor.getCurrentUser()?.uid

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = currentUserId?.let { userId ->
        repository.observeMessages(chatId = chatId, userId = userId)
    }?.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    ) ?: MutableStateFlow(emptyList())

    init {
        observeChat()
    }

    private fun observeChat() {
        val userId = currentUserId
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isMissingChat = true)
            return
        }

        viewModelScope.launch {
            repository.observeChat(chatId = chatId, userId = userId).collect { chat ->
                _uiState.value = _uiState.value.copy(
                    title = chat?.title.orEmpty(),
                    isMissingChat = chat == null,
                )
            }
        }
    }

    fun onInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun onSendClick() {
        val userId = currentUserId ?: return
        val text = _uiState.value.input.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            repository.sendUserMessage(
                chatId = chatId,
                userId = userId,
                text = text,
            )
            _uiState.value = _uiState.value.copy(
                input = "",
                isSending = false,
            )
        }
    }
}
