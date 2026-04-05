package io.valneva.chatassistant.feature.auth.presentation.common

import androidx.annotation.StringRes
import io.valneva.chatassistant.R
import io.valneva.chatassistant.feature.auth.domain.AuthError

object AuthFormValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @StringRes
    fun validateEmail(email: String): Int? {
        val normalized = email.trim()
        return if (normalized.matches(emailRegex)) null else R.string.auth_error_invalid_email
    }

    @StringRes
    fun validatePassword(password: String): Int? {
        return when {
            password.isBlank() -> R.string.auth_error_empty_password
            password.length < MIN_PASSWORD_LENGTH -> R.string.auth_error_short_password
            else -> null
        }
    }

    @StringRes
    fun validateConfirmPassword(
        password: String,
        confirmPassword: String,
    ): Int? {
        val passwordError = validatePassword(confirmPassword)
        if (passwordError != null) return passwordError

        return if (password == confirmPassword) null else R.string.auth_error_passwords_mismatch
    }

    private const val MIN_PASSWORD_LENGTH = 6
}

@StringRes
fun AuthError.toMessageRes(): Int = when (this) {
    AuthError.INVALID_EMAIL -> R.string.auth_error_invalid_email
    AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthError.EMAIL_IN_USE -> R.string.auth_error_email_in_use
    AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
    AuthError.NO_NETWORK -> R.string.auth_error_no_network
    AuthError.GOOGLE_FAILED -> R.string.auth_error_google_failed
    AuthError.UNKNOWN -> R.string.auth_error_generic
}
