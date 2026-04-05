package io.valneva.chatassistant.feature.auth.presentation.register

import androidx.annotation.StringRes

sealed interface RegisterUiEffect {
    data class ShowSnackbar(
        @StringRes val messageRes: Int,
        val showRetry: Boolean = false,
    ) : RegisterUiEffect

    data object NavigateToChats : RegisterUiEffect
}
