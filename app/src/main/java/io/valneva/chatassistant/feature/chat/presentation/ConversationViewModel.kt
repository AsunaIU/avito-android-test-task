package io.valneva.chatassistant.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.app.navigation.AppRoute
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.chat.data.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authInteractor: AuthInteractor,
    private val repository: ChatRepository,
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle[AppRoute.Chat.CHAT_ID_ARG])
    private val currentUserId: StateFlow<String?> = authInteractor.observeCurrentUser()
        .map { user -> user?.uid }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = authInteractor.getCurrentUser()?.uid,
        )

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeMessages(chatId = chatId, userId = userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        observeChat()
    }

    private fun observeChat() {
        viewModelScope.launch {
            currentUserId.flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(null)
                } else {
                    repository.observeChat(chatId = chatId, userId = userId)
                }
            }.collect { chat ->
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
        val userId = currentUserId.value ?: return
        val text = _uiState.value.input.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            val pendingAssistantReply = repository.sendUserMessage(
                chatId = chatId,
                userId = userId,
                text = text,
            )
            _uiState.value = _uiState.value.copy(
                input = "",
                isSending = false,
            )

            pendingAssistantReply?.let { pendingReply ->
                simulateAssistantFailure(
                    messageId = pendingReply.assistantMessageId,
                    userId = userId,
                )
            }
        }
    }

    fun onRetryAssistantMessage(messageId: String) {
        val userId = currentUserId.value ?: return

        viewModelScope.launch {
            val pendingAssistantReply = repository.retryAssistantReply(
                messageId = messageId,
                userId = userId,
            ) ?: return@launch

            simulateAssistantFailure(
                messageId = pendingAssistantReply.assistantMessageId,
                userId = userId,
            )
        }
    }

    private fun simulateAssistantFailure(
        messageId: String,
        userId: String,
    ) {
        viewModelScope.launch {
            delay(900)
            repository.markAssistantReplyFailed(
                messageId = messageId,
                userId = userId,
                errorMessage = ASSISTANT_ERROR_MESSAGE,
            )
        }
    }

    private companion object {
        const val ASSISTANT_ERROR_MESSAGE = "Не удалось получить ответ"
    }
}
