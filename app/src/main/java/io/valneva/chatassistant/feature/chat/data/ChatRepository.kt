package io.valneva.chatassistant.feature.chat.data

import androidx.room.withTransaction
import io.valneva.chatassistant.core.data.local.AppDatabase
import io.valneva.chatassistant.core.data.local.dao.ChatDao
import io.valneva.chatassistant.core.data.local.dao.MessageDao
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.core.data.local.model.MessageRole
import io.valneva.chatassistant.core.data.local.model.MessageStatus
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatMessageDto
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatRemoteDataSource
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val database: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val gigaChatRemoteDataSource: GigaChatRemoteDataSource,
) {

    data class PendingAssistantReply(
        val assistantMessageId: String,
    )

    fun observeChat(
        chatId: String,
        userId: String,
    ): Flow<ChatEntity?> = chatDao.observeChatById(chatId = chatId, userId = userId)

    fun observeMessages(
        chatId: String,
        userId: String,
    ): Flow<List<MessageEntity>> = messageDao.observeMessages(chatId = chatId, userId = userId)

    suspend fun sendUserMessage(
        chatId: String,
        userId: String,
        text: String,
    ): PendingAssistantReply? {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return null

        var pendingAssistantReply: PendingAssistantReply? = null

        database.withTransaction {
            val chat = chatDao.getChatById(chatId = chatId, userId = userId) ?: return@withTransaction
            val now = System.currentTimeMillis()
            val userMessageId = UUID.randomUUID().toString()
            val assistantMessageId = UUID.randomUUID().toString()
            val assistantCreatedAt = now + 1

            messageDao.insertMessage(
                MessageEntity(
                    id = userMessageId,
                    chatId = chatId,
                    userId = userId,
                    role = MessageRole.USER,
                    text = trimmedText,
                    status = MessageStatus.SENT,
                    createdAt = now,
                ),
            )

            messageDao.insertMessage(
                MessageEntity(
                    id = assistantMessageId,
                    chatId = chatId,
                    userId = userId,
                    role = MessageRole.ASSISTANT,
                    replyToMessageId = userMessageId,
                    text = "",
                    status = MessageStatus.GENERATING,
                    createdAt = assistantCreatedAt,
                ),
            )

            chatDao.updateChatSummary(
                chatId = chatId,
                userId = userId,
                title = resolveChatTitle(chat = chat, firstMessage = trimmedText),
                updatedAt = assistantCreatedAt,
                lastMessagePreview = trimmedText.take(MAX_PREVIEW_LENGTH),
                totalTokens = chat.totalTokens,
            )

            pendingAssistantReply = PendingAssistantReply(assistantMessageId = assistantMessageId)
        }

        return pendingAssistantReply
    }

    suspend fun retryAssistantReply(
        messageId: String,
        userId: String,
    ): PendingAssistantReply? {
        var pendingAssistantReply: PendingAssistantReply? = null

        database.withTransaction {
            val assistantMessage = messageDao.getMessageById(messageId = messageId, userId = userId)
                ?.takeIf { message ->
                    message.role == MessageRole.ASSISTANT && !message.replyToMessageId.isNullOrBlank()
                }
                ?: return@withTransaction

            val chat = chatDao.getChatById(chatId = assistantMessage.chatId, userId = userId) ?: return@withTransaction

            messageDao.updateMessage(
                messageId = assistantMessage.id,
                userId = userId,
                text = assistantMessage.text,
                status = MessageStatus.GENERATING,
                errorMessage = null,
                tokenUsage = assistantMessage.tokenUsage,
            )

            chatDao.updateChatSummary(
                chatId = chat.id,
                userId = userId,
                title = chat.title,
                updatedAt = System.currentTimeMillis(),
                lastMessagePreview = chat.lastMessagePreview,
                totalTokens = chat.totalTokens,
            )

            pendingAssistantReply = PendingAssistantReply(assistantMessageId = assistantMessage.id)
        }

        return pendingAssistantReply
    }

    suspend fun markAssistantReplyFailed(
        messageId: String,
        userId: String,
        errorMessage: String,
    ) {
        val assistantMessage = messageDao.getMessageById(messageId = messageId, userId = userId)
            ?.takeIf { message -> message.role == MessageRole.ASSISTANT }
            ?: return

        messageDao.updateMessage(
            messageId = assistantMessage.id,
            userId = userId,
            text = assistantMessage.text,
            status = MessageStatus.ERROR,
            errorMessage = errorMessage,
            tokenUsage = assistantMessage.tokenUsage,
        )
    }

    suspend fun generateAssistantReply(
        chatId: String,
        userId: String,
        assistantMessageId: String,
    ) {
        val chat = chatDao.getChatById(chatId = chatId, userId = userId) ?: return
        val assistantMessage = messageDao.getMessageById(messageId = assistantMessageId, userId = userId)
            ?.takeIf { message -> message.role == MessageRole.ASSISTANT }
            ?: return

        try {
            val reply = gigaChatRemoteDataSource.generateReply(
                messages = buildConversationHistory(chatId = chatId, userId = userId),
            )

            val now = System.currentTimeMillis()
            val replyPreview = reply.content.take(MAX_PREVIEW_LENGTH)

            database.withTransaction {
                messageDao.updateMessage(
                    messageId = assistantMessage.id,
                    userId = userId,
                    text = reply.content,
                    status = MessageStatus.SENT,
                    errorMessage = null,
                    tokenUsage = reply.totalTokens,
                )

                chatDao.updateChatSummary(
                    chatId = chat.id,
                    userId = userId,
                    title = chat.title,
                    updatedAt = now,
                    lastMessagePreview = replyPreview,
                    totalTokens = chat.totalTokens + (reply.totalTokens ?: 0),
                )
            }
        } catch (error: Exception) {
            markAssistantReplyFailed(
                messageId = assistantMessage.id,
                userId = userId,
                errorMessage = error.message ?: DEFAULT_ASSISTANT_ERROR,
            )
        }
    }

    private suspend fun buildConversationHistory(
        chatId: String,
        userId: String,
    ): List<GigaChatMessageDto> {
        return messageDao.getMessages(chatId = chatId, userId = userId)
            .mapNotNull { message ->
                when (message.role) {
                    MessageRole.USER -> message.text.takeIf { text -> text.isNotBlank() }?.let { text ->
                        GigaChatMessageDto(role = ROLE_USER, content = text)
                    }

                    MessageRole.ASSISTANT -> {
                        if (message.status == MessageStatus.SENT && message.text.isNotBlank()) {
                            GigaChatMessageDto(role = ROLE_ASSISTANT, content = message.text)
                        } else {
                            null
                        }
                    }
                }
            }
    }

    private fun resolveChatTitle(
        chat: ChatEntity,
        firstMessage: String,
    ): String {
        if (chat.title != DEFAULT_CHAT_TITLE) return chat.title

        val candidate = firstMessage
            .trim()
            .replace("\n", " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .take(MAX_TITLE_WORDS)
            .joinToString(separator = " ")

        return candidate
            .take(MAX_TITLE_LENGTH)
            .ifBlank { DEFAULT_CHAT_TITLE }
    }

    private companion object {
        const val DEFAULT_CHAT_TITLE = "Новый чат"
        const val DEFAULT_ASSISTANT_ERROR = "Не удалось получить ответ"
        const val MAX_PREVIEW_LENGTH = 120
        const val MAX_TITLE_LENGTH = 40
        const val MAX_TITLE_WORDS = 6
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
    }
}
