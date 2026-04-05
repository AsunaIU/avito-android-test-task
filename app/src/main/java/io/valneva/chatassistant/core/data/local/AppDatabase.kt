package io.valneva.chatassistant.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.valneva.chatassistant.core.data.local.dao.ChatDao
import io.valneva.chatassistant.core.data.local.dao.MessageDao
import io.valneva.chatassistant.core.data.local.entity.ChatEntity
import io.valneva.chatassistant.core.data.local.entity.MessageEntity

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
