package io.valneva.chatassistant.feature.chats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.chats.data.ChatsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val repository: ChatsRepository,
) : ViewModel() {

    private val currentUserId: String? = authInteractor.getCurrentUser()?.uid

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ChatsUiEffect>()
    val effects = _effects.asSharedFlow()

    val chats: Flow<PagingData<ChatEntity>> = uiState
        .combine(flowOf(currentUserId)) { state, userId ->
            userId to state.appliedQuery.trim()
        }
        .distinctUntilChanged()
        .flatMapLatest { (userId, query) ->
            if (userId.isNullOrBlank()) {
                flowOf(PagingData.empty())
            } else {
                repository.getPagedChats(userId = userId, query = query)
            }
        }
        .cachedIn(viewModelScope)

    val hasAuthenticatedUser: StateFlow<Boolean> = flowOf(currentUserId != null)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = currentUserId != null,
        )

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            appliedQuery = if (query.isBlank()) "" else _uiState.value.appliedQuery,
        )
    }

    fun onSearchClick() {
        val trimmedQuery = _uiState.value.searchQuery.trim()
        if (trimmedQuery.isBlank()) return

        _uiState.value = _uiState.value.copy(appliedQuery = trimmedQuery)
    }

    fun onSearchCleared() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            appliedQuery = "",
        )
    }

    fun onCreateChatClick() {
        val userId = currentUserId ?: return
        if (_uiState.value.isCreatingChat) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingChat = true)
            val chatId = repository.createChat(userId = userId)
            _uiState.value = _uiState.value.copy(isCreatingChat = false)
            _effects.emit(ChatsUiEffect.OpenChat(chatId = chatId))
        }
    }
}
