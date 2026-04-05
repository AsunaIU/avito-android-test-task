package io.valneva.chatassistant.feature.images.presentation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.valneva.chatassistant.R
import io.valneva.chatassistant.designsystem.component.ErrorSnackbar
import io.valneva.chatassistant.designsystem.component.LoadingButton

@Composable
fun ImagesScreen(
    modifier: Modifier = Modifier,
    viewModel: ImagesViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bitmap = remember(uiState.generatedImageBytes) {
        uiState.generatedImageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ImagesUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                ErrorSnackbar(snackbarData = snackbarData)
            }
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.images_screen_title),
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
            )

            OutlinedTextField(
                value = uiState.prompt,
                onValueChange = viewModel::onPromptChanged,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.images_prompt_label))
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.images_prompt_placeholder))
                },
                minLines = 3,
                maxLines = 6,
                enabled = !uiState.isGenerating,
                shape = androidx.compose.material3.MaterialTheme.shapes.large,
            )

            LoadingButton(
                text = stringResource(id = R.string.images_generate),
                onClick = viewModel::onGenerateClick,
                enabled = uiState.prompt.isNotBlank(),
                isLoading = uiState.isGenerating,
            )

            if (bitmap != null) {
                Surface(
                    shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.images_result),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (uiState.responseMessage.isNotBlank()) {
                            Text(
                                text = uiState.responseMessage,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else if (!uiState.isGenerating) {
                Surface(
                    shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.images_empty_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (uiState.lastPrompt.isNotBlank()) {
                LoadingButton(
                    text = stringResource(id = R.string.images_retry),
                    onClick = viewModel::onRetryClick,
                    isLoading = false,
                    enabled = !uiState.isGenerating,
                )
            }
        }
    }
}
