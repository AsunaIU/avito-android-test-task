package io.valneva.chatassistant.feature.auth.domain

enum class AuthError {
    INVALID_EMAIL,
    INVALID_CREDENTIALS,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    TOO_MANY_REQUESTS,
    NO_NETWORK,
    GOOGLE_FAILED,
    UNKNOWN,
}
