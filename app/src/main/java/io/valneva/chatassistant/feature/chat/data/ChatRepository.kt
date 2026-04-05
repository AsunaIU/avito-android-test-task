package io.valneva.chatassistant.feature.chat.data

import androidx.room.withTransaction
import io.valneva.chatassistant.core.data.local.AppDatabase
import io.valneva.chatassistant.core.data.local.dao.ChatDao
import io.valneva.chatassistant.core.data.local.dao.MessageDao
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.core.data.local.model.MessageRole
import io.valneva.chatassistant.core.data.local.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val database: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
) {

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
    ) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return

        database.withTransaction {
            val chat = chatDao.getChatById(chatId = chatId, userId = userId) ?: return@withTransaction
            val now = System.currentTimeMillis()

            messageDao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    userId = userId,
                    role = MessageRole.USER,
                    text = trimmedText,
                    status = MessageStatus.SENT,
                    createdAt = now,
                ),
            )

            chatDao.updateChatSummary(
                chatId = chatId,
                userId = userId,
                title = resolveChatTitle(chat = chat, firstMessage = trimmedText),
                updatedAt = now,
                lastMessagePreview = trimmedText.take(MAX_PREVIEW_LENGTH),
                totalTokens = chat.totalTokens,
            )
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
        const val MAX_PREVIEW_LENGTH = 120
        const val MAX_TITLE_LENGTH = 40
        const val MAX_TITLE_WORDS = 6
    }
}
