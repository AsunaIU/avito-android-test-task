package io.valneva.chatassistant.feature.auth.presentation.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.valneva.chatassistant.R
import io.valneva.chatassistant.designsystem.component.AppTextField
import io.valneva.chatassistant.designsystem.component.ErrorSnackbar
import io.valneva.chatassistant.designsystem.component.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var isFormVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isFormVisible = true
    }

    LaunchedEffect(viewModel, context) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RegisterUiEffect.ShowSnackbar -> {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = context.resources.getString(effect.messageRes),
                        actionLabel = effect.showRetry.takeIf { it }?.let {
                            context.resources.getString(R.string.retry)
                        },
                        duration = SnackbarDuration.Long,
                    )

                    if (effect.showRetry && snackbarResult == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.onRetryRequested()
                    }
                }

                RegisterUiEffect.NavigateToChats -> onNavigateToChats()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !uiState.isLoading,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.register_title))
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
                AnimatedVisibility(
                    visible = isFormVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 420,
                            easing = FastOutSlowInEasing,
                        ),
                    ) + slideInVertically(
                        animationSpec = tween(
                            durationMillis = 420,
                            easing = FastOutSlowInEasing,
                        ),
                        initialOffsetY = { it / 8 },
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.register_subtitle),
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
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    passwordFocusRequester.requestFocus()
                                },
                            ),
                        )

                        AppTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChanged,
                            label = stringResource(id = R.string.password_label),
                            modifier = Modifier.focusRequester(passwordFocusRequester),
                            enabled = !uiState.isLoading,
                            isError = uiState.passwordErrorRes != null,
                            errorText = uiState.passwordErrorRes?.let(context::getString),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    confirmPasswordFocusRequester.requestFocus()
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

                        AppTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            label = stringResource(id = R.string.confirm_password_label),
                            modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                            enabled = !uiState.isLoading,
                            isError = uiState.confirmPasswordErrorRes != null,
                            errorText = uiState.confirmPasswordErrorRes?.let(context::getString),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onSubmitClick()
                                },
                            ),
                            visualTransformation = if (uiState.isConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = viewModel::onConfirmPasswordVisibilityToggle) {
                                    Icon(
                                        imageVector = if (uiState.isConfirmPasswordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = stringResource(
                                            id = if (uiState.isConfirmPasswordVisible) {
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
                            text = stringResource(id = R.string.create_account_button),
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.onSubmitClick()
                            },
                            isLoading = uiState.isLoading,
                        )
                    }
                }
            }
        }
    }
}
