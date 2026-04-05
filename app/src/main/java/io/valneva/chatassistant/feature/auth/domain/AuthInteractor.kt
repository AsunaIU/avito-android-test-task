package io.valneva.chatassistant.feature.auth.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthInteractor @Inject constructor(
    private val repository: AuthRepository,
) {
    fun getCurrentUser(): AuthUser? = repository.getCurrentUser()

    fun observeCurrentUser(): Flow<AuthUser?> = repository.observeCurrentUser()

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthUser> = repository.signIn(email = email, password = password)

    suspend fun signUp(
        email: String,
        password: String,
    ): Result<AuthUser> = repository.signUp(email = email, password = password)

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        repository.signInWithGoogle(idToken = idToken)

    fun signOut() {
        repository.signOut()
    }
}
