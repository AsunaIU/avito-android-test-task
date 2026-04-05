package io.valneva.chatassistant.feature.profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.valneva.chatassistant.R
import io.valneva.chatassistant.designsystem.component.ErrorSnackbar
import io.valneva.chatassistant.designsystem.component.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = viewModel::onPhotoPicked,
    )

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProfileUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageRes),
                    )
                }

                ProfileUiEffect.SignedOut -> onSignedOut()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileHeader(
                photoUrl = uiState.photoUrl,
                isUploadingPhoto = uiState.isUploadingPhoto,
                onPhotoClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )

            ProfileNameSection(
                displayName = uiState.displayName,
                displayNameInput = uiState.displayNameInput,
                isSavingName = uiState.isSavingName,
                onDisplayNameChanged = viewModel::onDisplayNameChanged,
                onSaveClick = viewModel::onSaveNameClick,
            )

            ProfileInfoSection(
                email = uiState.email,
                phoneNumber = uiState.phoneNumber,
                totalTokens = uiState.totalTokens,
            )

            ThemeSection(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
            )

            LoadingButton(
                text = stringResource(id = R.string.profile_sign_out),
                onClick = viewModel::onSignOutClick,
                isLoading = uiState.isSigningOut,
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    photoUrl: String?,
    isUploadingPhoto: Boolean,
    onPhotoClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable(enabled = !isUploadingPhoto, onClick = onPhotoClick),
                shape = CircleShape,
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = stringResource(id = R.string.profile_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = stringResource(id = R.string.profile_photo),
                            modifier = Modifier.size(72.dp),
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (isUploadingPhoto) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        TextButton(
            onClick = onPhotoClick,
            enabled = !isUploadingPhoto,
        ) {
            Text(text = stringResource(id = R.string.profile_change_photo))
        }
    }
}

@Composable
private fun ProfileNameSection(
    displayName: String,
    displayNameInput: String,
    isSavingName: Boolean,
    onDisplayNameChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Surface(
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.profile_name_label),
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = displayNameInput,
                onValueChange = onDisplayNameChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(text = stringResource(id = R.string.profile_name_label))
                },
                placeholder = {
                    Text(text = displayName.ifBlank { stringResource(id = R.string.profile_no_name) })
                },
                enabled = !isSavingName,
                shape = androidx.compose.material3.MaterialTheme.shapes.large,
            )

            LoadingButton(
                text = stringResource(id = R.string.profile_save_name),
                onClick = onSaveClick,
                enabled = displayNameInput.trim().isNotBlank() && displayNameInput.trim() != displayName,
                isLoading = isSavingName,
            )
        }
    }
}

@Composable
private fun ProfileInfoSection(
    email: String,
    phoneNumber: String,
    totalTokens: Int,
) {
    Surface(
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileInfoRow(
                label = stringResource(id = R.string.profile_email_label),
                value = email,
            )
            ProfileInfoRow(
                label = stringResource(id = R.string.profile_phone_label),
                value = phoneNumber.ifBlank { stringResource(id = R.string.profile_no_phone) },
            )
            ProfileInfoRow(
                label = stringResource(id = R.string.profile_tokens_label),
                value = totalTokens.toString(),
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ThemeSection(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.dark_theme),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.dark_theme_description),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = isDarkTheme,
                onCheckedChange = onThemeToggle,
            )
        }
    }
}
