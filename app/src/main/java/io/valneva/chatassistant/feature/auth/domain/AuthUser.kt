package io.valneva.chatassistant.feature.auth.domain

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String?,
)
