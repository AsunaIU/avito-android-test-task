package io.valneva.chatassistant.feature.profile.presentation

data class ProfileUiState(
    val displayName: String = "",
    val displayNameInput: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val totalTokens: Int = 0,
    val isSavingName: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isSigningOut: Boolean = false,
)
