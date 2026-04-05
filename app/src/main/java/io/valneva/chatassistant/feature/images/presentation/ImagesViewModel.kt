package io.valneva.chatassistant.feature.images.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.feature.images.data.ImagesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val repository: ImagesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImagesUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ImagesUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onPromptChanged(value: String) {
        _uiState.value = _uiState.value.copy(prompt = value)
    }

    fun onGenerateClick() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        generate(prompt = prompt)
    }

    fun onRetryClick() {
        val prompt = _uiState.value.lastPrompt.ifBlank { _uiState.value.prompt }.trim()
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        generate(prompt = prompt)
    }

    private fun generate(prompt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                lastPrompt = prompt,
            )

            repository.generateImage(prompt = prompt).fold(
                onSuccess = { image ->
                    _uiState.value = _uiState.value.copy(
                        prompt = prompt,
                        isGenerating = false,
                        generatedImageBytes = image.bytes,
                        generatedImageFileId = image.fileId,
                        responseMessage = image.message,
                        lastPrompt = prompt,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isGenerating = false, lastPrompt = prompt)
                    _effects.emit(
                        ImagesUiEffect.ShowSnackbar(
                            message = error.message ?: "Не удалось создать изображение",
                        ),
                    )
                },
            )
        }
    }
}
