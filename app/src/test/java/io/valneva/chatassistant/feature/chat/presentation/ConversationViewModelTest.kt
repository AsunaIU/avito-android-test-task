package io.valneva.chatassistant.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.valneva.chatassistant.app.navigation.AppRoute
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.core.testing.MainDispatcherRule
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.auth.domain.AuthUser
import io.valneva.chatassistant.feature.chat.data.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authInteractor = mockk<AuthInteractor>()
    private val repository = mockk<ChatRepository>()

    @Test
    fun `onSendClick with blank input does nothing`() = runTest {
        val user = testUser()

        every { authInteractor.getCurrentUser() } returns user
        every { authInteractor.observeCurrentUser() } returns MutableStateFlow(user)
        every { repository.observeMessages(any(), any()) } returns flowOf(emptyList<MessageEntity>())
        every { repository.observeChat(any(), any()) } returns flowOf(testChat())

        val viewModel = ConversationViewModel(
            savedStateHandle = SavedStateHandle(mapOf(AppRoute.Chat.CHAT_ID_ARG to "chat-1")),
            authInteractor = authInteractor,
            repository = repository,
        )

        viewModel.onInputChanged("   ")
        viewModel.onSendClick()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.sendUserMessage(any(), any(), any()) }
    }

    @Test
    fun `onSendClick sends user message and requests assistant reply`() = runTest {
        val user = testUser()

        every { authInteractor.getCurrentUser() } returns user
        every { authInteractor.observeCurrentUser() } returns MutableStateFlow(user)
        every { repository.observeMessages(any(), any()) } returns flowOf(emptyList<MessageEntity>())
        every { repository.observeChat(any(), any()) } returns flowOf(testChat())
        coEvery {
            repository.sendUserMessage(
                chatId = "chat-1",
                userId = "user-1",
                text = "Привет",
            )
        } returns ChatRepository.PendingAssistantReply(assistantMessageId = "assistant-1")
        coEvery {
            repository.generateAssistantReply(
                chatId = "chat-1",
                userId = "user-1",
                assistantMessageId = "assistant-1",
            )
        } returns Unit

        val viewModel = ConversationViewModel(
            savedStateHandle = SavedStateHandle(mapOf(AppRoute.Chat.CHAT_ID_ARG to "chat-1")),
            authInteractor = authInteractor,
            repository = repository,
        )

        viewModel.onInputChanged("Привет")
        viewModel.onSendClick()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.input).isEmpty()
        assertThat(viewModel.uiState.value.isSending).isFalse()

        coVerify(exactly = 1) {
            repository.sendUserMessage(
                chatId = "chat-1",
                userId = "user-1",
                text = "Привет",
            )
        }
        coVerify(exactly = 1) {
            repository.generateAssistantReply(
                chatId = "chat-1",
                userId = "user-1",
                assistantMessageId = "assistant-1",
            )
        }
    }

    private fun testUser() = AuthUser(
        uid = "user-1",
        email = "user@example.com",
        displayName = "User",
        photoUrl = null,
        phoneNumber = null,
    )

    private fun testChat() = ChatEntity(
        id = "chat-1",
        userId = "user-1",
        title = "Чат",
        createdAt = 1L,
        updatedAt = 1L,
    )
}
