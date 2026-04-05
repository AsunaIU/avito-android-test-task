package io.valneva.chatassistant.feature.chats.presentation

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.core.testing.MainDispatcherRule
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.auth.domain.AuthUser
import io.valneva.chatassistant.feature.chats.data.ChatsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authInteractor = mockk<AuthInteractor>()
    private val repository = mockk<ChatsRepository>()

    @Test
    fun `onCreateChatClick emits open chat effect`() = runTest {
        val user = AuthUser(
            uid = "user-1",
            email = "user@example.com",
            displayName = "User",
            photoUrl = null,
            phoneNumber = null,
        )

        every { authInteractor.getCurrentUser() } returns user
        every { authInteractor.observeCurrentUser() } returns MutableStateFlow(user)
        every { repository.getPagedChats(any(), any()) } returns flowOf(PagingData.empty<ChatEntity>())
        coEvery { repository.createChat("user-1") } returns "chat-1"

        val viewModel = ChatListViewModel(
            authInteractor = authInteractor,
            repository = repository,
        )

        viewModel.effects.test {
            viewModel.onCreateChatClick()

            assertThat(awaitItem()).isEqualTo(ChatListUiEffect.OpenChat(chatId = "chat-1"))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.isCreatingChat).isFalse()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.createChat("user-1") }
    }

    @Test
    fun `onSearchCleared resets both search values`() = runTest {
        every { authInteractor.getCurrentUser() } returns null
        every { authInteractor.observeCurrentUser() } returns MutableStateFlow(null)

        val viewModel = ChatListViewModel(
            authInteractor = authInteractor,
            repository = repository,
        )

        viewModel.onSearchQueryChanged("кот")
        viewModel.onSearchClick()
        viewModel.onSearchCleared()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
        assertThat(viewModel.uiState.value.appliedQuery).isEmpty()
    }
}
