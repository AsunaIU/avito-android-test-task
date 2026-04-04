package io.valneva.chatassistant.feature.auth.presentation.login

import androidx.annotation.StringRes

sealed interface LoginUiEffect {
    data class ShowSnackbar(
        @StringRes val messageRes: Int,
        val showRetry: Boolean = false,
    ) : LoginUiEffect

    data object RequestGoogleCredential : LoginUiEffect
    data object NavigateToChats : LoginUiEffect
}
