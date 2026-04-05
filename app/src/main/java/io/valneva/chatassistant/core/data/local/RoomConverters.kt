package io.valneva.chatassistant.core.data.local

import androidx.room.TypeConverter
import io.valneva.chatassistant.core.data.local.model.MessageRole
import io.valneva.chatassistant.core.data.local.model.MessageStatus

class RoomConverters {

    @TypeConverter
    fun fromMessageRole(value: MessageRole): String = value.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = MessageRole.valueOf(value)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}
