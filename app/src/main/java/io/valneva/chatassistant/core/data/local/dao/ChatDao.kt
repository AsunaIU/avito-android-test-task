package io.valneva.chatassistant.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query(
        """
        SELECT * FROM chats
        WHERE userId = :userId
          AND (:query = '' OR title LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY updatedAt DESC
        """,
    )
    fun getPagedChats(
        userId: String,
        query: String,
    ): PagingSource<Int, ChatEntity>

    @Query(
        """
        SELECT * FROM chats
        WHERE id = :chatId AND userId = :userId
        LIMIT 1
        """,
    )
    fun observeChatById(
        chatId: String,
        userId: String,
    ): Flow<ChatEntity?>

    @Query(
        """
        SELECT * FROM chats
        WHERE id = :chatId AND userId = :userId
        LIMIT 1
        """,
    )
    suspend fun getChatById(
        chatId: String,
        userId: String,
    ): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query(
        """
        UPDATE chats
        SET title = :title,
            updatedAt = :updatedAt,
            lastMessagePreview = :lastMessagePreview,
            totalTokens = :totalTokens
        WHERE id = :chatId AND userId = :userId
        """,
    )
    suspend fun updateChatSummary(
        chatId: String,
        userId: String,
        title: String,
        updatedAt: Long,
        lastMessagePreview: String?,
        totalTokens: Int,
    )

    @Query("DELETE FROM chats WHERE userId = :userId")
    suspend fun deleteChatsByUser(userId: String)
}
