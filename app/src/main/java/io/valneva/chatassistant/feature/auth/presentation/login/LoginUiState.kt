package io.valneva.chatassistant.feature.auth.presentation.login

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailErrorRes: Int? = null,
    @StringRes val passwordErrorRes: Int? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
)
