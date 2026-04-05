package io.valneva.chatassistant.feature.profile.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import io.valneva.chatassistant.core.data.local.dao.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val messageDao: MessageDao,
) {

    fun observeTotalTokens(userId: String): Flow<Int> = messageDao.observeTotalTokenUsage(userId)

    suspend fun updateDisplayName(name: String): Result<String> = runCatching {
        val trimmedName = name.trim()
        val user = firebaseAuth.currentUser ?: error("User is not authenticated")

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build(),
        ).await()

        trimmedName
    }

    suspend fun updatePhoto(
        userId: String,
        localUri: Uri,
    ): Result<String> = runCatching {
        val user = firebaseAuth.currentUser ?: error("User is not authenticated")
        val avatarReference = firebaseStorage.reference
            .child("users/$userId/avatar.jpg")

        avatarReference.putFile(localUri).await()
        val remoteUri = avatarReference.downloadUrl.await()

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setPhotoUri(remoteUri)
                .build(),
        ).await()

        remoteUri.toString()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
