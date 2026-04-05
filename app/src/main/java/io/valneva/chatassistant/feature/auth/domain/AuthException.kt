package io.valneva.chatassistant.feature.auth.domain

class AuthException(
    val error: AuthError,
) : Exception(error.name)

fun Throwable.toAuthError(): AuthError = (this as? AuthException)?.error ?: AuthError.UNKNOWN