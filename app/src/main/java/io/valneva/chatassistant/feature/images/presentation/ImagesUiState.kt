package io.valneva.chatassistant.feature.images.presentation

data class ImagesUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val generatedImageBytes: ByteArray? = null,
    val generatedImageFileId: String? = null,
    val responseMessage: String = "",
    val lastPrompt: String = "",
)
