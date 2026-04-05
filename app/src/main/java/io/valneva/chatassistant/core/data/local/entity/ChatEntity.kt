package io.valneva.chatassistant.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["userId", "updatedAt"]),
        Index(value = ["userId", "title"]),
    ],
)
data class ChatEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessagePreview: String? = null,
    val totalTokens: Int = 0,
)
