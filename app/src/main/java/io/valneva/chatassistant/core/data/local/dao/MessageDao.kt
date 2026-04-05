package io.valneva.chatassistant.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.valneva.chatassistant.core.data.local.entity.MessageEntity
import io.valneva.chatassistant.core.data.local.model.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query(
        """
        SELECT * FROM messages
        WHERE chatId = :chatId AND userId = :userId
        ORDER BY createdAt ASC
        """,
    )
    fun observeMessages(
        chatId: String,
        userId: String,
    ): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query(
        """
        SELECT * FROM messages
        WHERE id = :messageId AND userId = :userId
        LIMIT 1
        """,
    )
    suspend fun getMessageById(
        messageId: String,
        userId: String,
    ): MessageEntity?

    @Query(
        """
        UPDATE messages
        SET text = :text,
            status = :status,
            errorMessage = :errorMessage,
            tokenUsage = :tokenUsage
        WHERE id = :messageId AND userId = :userId
        """,
    )
    suspend fun updateMessage(
        messageId: String,
        userId: String,
        text: String,
        status: MessageStatus,
        errorMessage: String?,
        tokenUsage: Int?,
    )

    @Query("SELECT COALESCE(SUM(tokenUsage), 0) FROM messages WHERE userId = :userId")
    fun observeTotalTokenUsage(userId: String): Flow<Int>

    @Query("DELETE FROM messages WHERE userId = :userId")
    suspend fun deleteMessagesByUser(userId: String)
}
