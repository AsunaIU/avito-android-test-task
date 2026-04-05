package io.valneva.chatassistant.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.valneva.chatassistant.core.data.local.AppDatabase
import io.valneva.chatassistant.core.data.local.dao.ChatDao
import io.valneva.chatassistant.core.data.local.dao.MessageDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chat_assistant.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
}
