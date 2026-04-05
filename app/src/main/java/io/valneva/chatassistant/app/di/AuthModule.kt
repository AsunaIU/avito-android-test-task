package io.valneva.chatassistant.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.valneva.chatassistant.feature.auth.data.AuthRepositoryImpl
import io.valneva.chatassistant.feature.auth.domain.AuthRepository
import javax.inject.Singleton

/*
 * This module is used by Hilt via generated code during compilation to build the dependency graph.
 * Removing this module will break dependency injection at runtime.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository
}
