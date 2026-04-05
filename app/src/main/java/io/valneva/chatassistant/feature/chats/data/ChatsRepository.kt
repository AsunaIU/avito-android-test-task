package io.valneva.chatassistant.feature.chats.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.valneva.chatassistant.core.data.local.dao.ChatDao
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val chatDao: ChatDao,
) {

    fun getPagedChats(
        userId: String,
        query: String,
    ): Flow<PagingData<ChatEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
        ) {
            chatDao.getPagedChats(userId = userId, query = query)
        }.flow
    }

    suspend fun createChat(userId: String): String {
        val now = System.currentTimeMillis()
        val chatId = UUID.randomUUID().toString()

        chatDao.insertChat(
            ChatEntity(
                id = chatId,
                userId = userId,
                title = DEFAULT_CHAT_TITLE,
                createdAt = now,
                updatedAt = now,
            ),
        )

        return chatId
    }

    private companion object {
        const val DEFAULT_CHAT_TITLE = "Новый чат"
    }
}
