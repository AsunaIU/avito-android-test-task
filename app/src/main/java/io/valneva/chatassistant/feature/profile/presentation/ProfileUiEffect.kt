package io.valneva.chatassistant.feature.profile.presentation

import androidx.annotation.StringRes

sealed interface ProfileUiEffect {
    data class ShowSnackbar(@StringRes val messageRes: Int) : ProfileUiEffect
    data object SignedOut : ProfileUiEffect
}
