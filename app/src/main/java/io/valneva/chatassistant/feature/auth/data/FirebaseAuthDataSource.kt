package io.valneva.chatassistant.feature.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun observeCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        trySend(firebaseAuth.currentUser)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun signIn(
        email: String,
        password: String,
    ): FirebaseUser = firebaseAuth.signInWithEmailAndPassword(email, password).await().user
        ?: error("Firebase returned null user for sign in")

    suspend fun signUp(
        email: String,
        password: String,
    ): FirebaseUser = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
        ?: error("Firebase returned null user for sign up")

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(credential).await().user
            ?: error("Firebase returned null user for Google sign in")
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
