package io.valneva.chatassistant.feature.images.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.valneva.chatassistant.core.data.remote.gigachat.GigaChatGeneratedImage
import io.valneva.chatassistant.core.testing.MainDispatcherRule
import io.valneva.chatassistant.feature.images.data.ImagesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImagesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ImagesRepository>()

    @Test
    fun `onGenerateClick updates image state on success`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3)

        coEvery { repository.generateImage("розовый кот") } returns Result.success(
            GigaChatGeneratedImage(
                fileId = "file-1",
                bytes = imageBytes,
                message = "Готово",
                totalTokens = 10,
            ),
        )

        val viewModel = ImagesViewModel(repository = repository)

        viewModel.onPromptChanged("розовый кот")
        viewModel.onGenerateClick()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.generatedImageFileId).isEqualTo("file-1")
        assertThat(viewModel.uiState.value.generatedImageBytes?.toList()).isEqualTo(imageBytes.toList())
        assertThat(viewModel.uiState.value.responseMessage).isEqualTo("Готово")
        assertThat(viewModel.uiState.value.isGenerating).isFalse()
    }

    @Test
    fun `onGenerateClick emits snackbar on error`() = runTest {
        coEvery { repository.generateImage("кот") } returns Result.failure(IllegalStateException("Ошибка генерации"))

        val viewModel = ImagesViewModel(repository = repository)

        viewModel.effects.test {
            viewModel.onPromptChanged("кот")
            viewModel.onGenerateClick()
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(ImagesUiEffect.ShowSnackbar("Ошибка генерации"))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
