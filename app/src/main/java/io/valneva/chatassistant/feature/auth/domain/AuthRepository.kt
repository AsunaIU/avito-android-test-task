package io.valneva.chatassistant.feature.auth.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): AuthUser?

    fun observeCurrentUser(): Flow<AuthUser?>

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signUp(
        email: String,
        password: String,
    ): Result<AuthUser>

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    fun signOut()
}
