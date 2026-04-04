package io.valneva.chatassistant.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.valneva.chatassistant.R
import io.valneva.chatassistant.core.common.findActivity
import io.valneva.chatassistant.core.common.isNetworkAvailable
import io.valneva.chatassistant.designsystem.component.AppTextField
import io.valneva.chatassistant.designsystem.component.ErrorSnackbar
import io.valneva.chatassistant.designsystem.component.LoadingButton
import io.valneva.chatassistant.feature.auth.domain.AuthError
import io.valneva.chatassistant.feature.auth.domain.AuthException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToChats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel, context, credentialManager) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowSnackbar -> {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageRes),
                        actionLabel = effect.showRetry.takeIf { it }?.let {
                            context.getString(R.string.retry)
                        },
                        duration = SnackbarDuration.Long,
                    )

                    if (effect.showRetry && snackbarResult == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.onRetryRequested()
                    }
                }

                LoginUiEffect.NavigateToChats -> onNavigateToChats()
                LoginUiEffect.RequestGoogleCredential -> {
                    val activity = context.findActivity()
                    if (activity == null) {
                        viewModel.onGoogleCredentialResult(
                            Result.failure(AuthException(AuthError.GOOGLE_FAILED)),
                        )
                        return@collect
                    }

                    if (!context.isNetworkAvailable()) {
                        viewModel.onGoogleCredentialResult(
                            Result.failure(AuthException(AuthError.NO_NETWORK)),
                        )
                        return@collect
                    }

                    val request = buildGoogleCredentialRequest(
                        serverClientId = context.getString(R.string.default_web_client_id),
                    )
                    val result = requestGoogleCredential(
                        credentialManager = credentialManager,
                        request = request,
                        activity = activity,
                    )
                    if (result != null) {
                        viewModel.onGoogleCredentialResult(result)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.login_title))
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !uiState.isLoading,
                    ) {
                        Text(text = stringResource(id = R.string.register_action))
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                ErrorSnackbar(snackbarData = snackbarData)
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.login_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = stringResource(id = R.string.email_label),
                        enabled = !uiState.isLoading,
                        isError = uiState.emailErrorRes != null,
                        errorText = uiState.emailErrorRes?.let(context::getString),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    AppTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = stringResource(id = R.string.password_label),
                        enabled = !uiState.isLoading,
                        isError = uiState.passwordErrorRes != null,
                        errorText = uiState.passwordErrorRes?.let(context::getString),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.onSignInClick()
                            },
                        ),
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::onPasswordVisibilityToggle) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = stringResource(
                                        id = if (uiState.isPasswordVisible) {
                                            R.string.hide_password
                                        } else {
                                            R.string.show_password
                                        },
                                    ),
                                )
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LoadingButton(
                        text = stringResource(id = R.string.sign_in_button),
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onSignInClick()
                        },
                        isLoading = uiState.isLoading,
                    )

                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onGoogleSignInClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(text = stringResource(id = R.string.continue_with_google_button))
                    }
                }
            }
        }
    }
}

private fun buildGoogleCredentialRequest(serverClientId: String): GetCredentialRequest {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

private suspend fun requestGoogleCredential(
    credentialManager: CredentialManager,
    request: GetCredentialRequest,
    activity: android.app.Activity,
): Result<String>? {
    return try {
        val result = credentialManager.getCredential(
            context = activity,
            request = request,
        )

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(googleIdTokenCredential.idToken)
        } else {
            Result.failure(AuthException(AuthError.GOOGLE_FAILED))
        }
    } catch (_: GetCredentialCancellationException) {
        null
    } catch (_: NoCredentialException) {
        Result.failure(AuthException(AuthError.GOOGLE_FAILED))
    } catch (_: GoogleIdTokenParsingException) {
        Result.failure(AuthException(AuthError.GOOGLE_FAILED))
    } catch (_: GetCredentialException) {
        Result.failure(AuthException(AuthError.GOOGLE_FAILED))
    }
}
