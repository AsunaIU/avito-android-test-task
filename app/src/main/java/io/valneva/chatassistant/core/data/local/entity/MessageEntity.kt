package io.valneva.chatassistant.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.valneva.chatassistant.core.data.local.model.MessageRole
import io.valneva.chatassistant.core.data.local.model.MessageStatus

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["chatId", "createdAt"]),
        Index(value = ["userId"]),
    ],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val userId: String,
    val role: MessageRole,
    val text: String,
    val status: MessageStatus,
    val createdAt: Long,
    val errorMessage: String? = null,
    val tokenUsage: Int? = null,
)
