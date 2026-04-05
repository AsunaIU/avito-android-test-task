package io.valneva.chatassistant.feature.auth.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import io.valneva.chatassistant.feature.auth.domain.AuthError
import io.valneva.chatassistant.feature.auth.domain.AuthException
import io.valneva.chatassistant.feature.auth.domain.AuthRepository
import io.valneva.chatassistant.feature.auth.domain.AuthUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
) : AuthRepository {

    override fun getCurrentUser(): AuthUser? = firebaseAuthDataSource.getCurrentUser()?.toDomain()

    override fun observeCurrentUser(): Flow<AuthUser?> = firebaseAuthDataSource.observeCurrentUser()
        .map { user -> user?.toDomain() }
        .distinctUntilChanged()

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthUser> = executeAuthAction(AuthAction.SignIn) {
        firebaseAuthDataSource.signIn(email = email, password = password)
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<AuthUser> = executeAuthAction(AuthAction.SignUp) {
        firebaseAuthDataSource.signUp(email = email, password = password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        executeAuthAction(AuthAction.GoogleSignIn) {
            firebaseAuthDataSource.signInWithGoogle(idToken = idToken)
        }

    override fun signOut() {
        firebaseAuthDataSource.signOut()
    }

    private suspend fun executeAuthAction(
        action: AuthAction,
        block: suspend () -> FirebaseUser,
    ): Result<AuthUser> {
        return try {
            Result.success(block().toDomain())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Result.failure(AuthException(error = throwable.toAuthError(action = action)))
        }
    }

    private fun Throwable.toAuthError(action: AuthAction): AuthError = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
        is FirebaseNetworkException -> AuthError.NO_NETWORK
        is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> {
            if (action == AuthAction.GoogleSignIn) AuthError.GOOGLE_FAILED
            else AuthError.INVALID_CREDENTIALS
        }

        else -> AuthError.UNKNOWN
    }

    private fun FirebaseUser.toDomain(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        phoneNumber = phoneNumber,
    )

    private enum class AuthAction {
        SignIn,
        SignUp,
        GoogleSignIn,
    }
}
