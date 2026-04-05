package io.valneva.chatassistant.feature.images.presentation

sealed interface ImagesUiEffect {
    data class ShowSnackbar(val message: String) : ImagesUiEffect
}
