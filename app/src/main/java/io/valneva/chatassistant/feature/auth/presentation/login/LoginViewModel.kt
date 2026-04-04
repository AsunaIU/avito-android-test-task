package io.valneva.chatassistant.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.feature.auth.domain.AuthError
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import io.valneva.chatassistant.feature.auth.domain.toAuthError
import io.valneva.chatassistant.feature.auth.presentation.common.AuthFormValidator
import io.valneva.chatassistant.feature.auth.presentation.common.toMessageRes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginUiEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: SharedFlow<LoginUiEffect> = _effects.asSharedFlow()

    private var pendingRetryAction: RetryAction? = null

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailErrorRes = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordErrorRes = null,
            )
        }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onSignInClick() {
        if (_uiState.value.isLoading || !validateInputs()) return
        submitEmailSignIn()
    }

    fun onGoogleSignInClick() {
        if (_uiState.value.isLoading) return

        pendingRetryAction = null
        _uiState.update { it.copy(isLoading = true) }
        _effects.tryEmit(LoginUiEffect.RequestGoogleCredential)
    }

    fun onGoogleCredentialResult(result: Result<String>) {
        val idToken = result.getOrNull()
        if (idToken == null) {
            _uiState.update { it.copy(isLoading = false) }
            handleImmediateFailure(
                error = result.exceptionOrNull()?.toAuthError() ?: AuthError.UNKNOWN,
                retryAction = RetryAction.Google,
            )
            return
        }

        viewModelScope.launch {
            val authResult = authInteractor.signInWithGoogle(idToken = idToken)
            if (authResult.isSuccess) {
                pendingRetryAction = null
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(LoginUiEffect.NavigateToChats)
            } else {
                handleAuthFailure(
                    throwable = authResult.exceptionOrNull(),
                    retryAction = RetryAction.Google,
                )
            }
        }
    }

    fun onRetryRequested() {
        when (pendingRetryAction) {
            RetryAction.EmailPassword -> submitEmailSignIn()
            RetryAction.Google -> onGoogleSignInClick()
            null -> Unit
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        val emailError = AuthFormValidator.validateEmail(state.email)
        val passwordError = AuthFormValidator.validatePassword(state.password)

        _uiState.update {
            it.copy(
                emailErrorRes = emailError,
                passwordErrorRes = passwordError,
            )
        }

        return emailError == null && passwordError == null
    }

    private fun submitEmailSignIn() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authInteractor.signIn(email = email, password = password)
            if (result.isSuccess) {
                pendingRetryAction = null
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(LoginUiEffect.NavigateToChats)
            } else {
                handleAuthFailure(
                    throwable = result.exceptionOrNull(),
                    retryAction = RetryAction.EmailPassword,
                )
            }
        }
    }

    private suspend fun handleAuthFailure(
        throwable: Throwable?,
        retryAction: RetryAction,
    ) {
        val error = throwable?.toAuthError() ?: AuthError.UNKNOWN
        _uiState.update { it.copy(isLoading = false) }

        val canRetry = error == AuthError.NO_NETWORK
        pendingRetryAction = retryAction.takeIf { canRetry }
        _effects.emit(
            LoginUiEffect.ShowSnackbar(
                messageRes = error.toMessageRes(),
                showRetry = canRetry,
            ),
        )
    }

    private fun handleImmediateFailure(
        error: AuthError,
        retryAction: RetryAction,
    ) {
        val canRetry = error == AuthError.NO_NETWORK
        pendingRetryAction = retryAction.takeIf { canRetry }
        _effects.tryEmit(
            LoginUiEffect.ShowSnackbar(
                messageRes = error.toMessageRes(),
                showRetry = canRetry,
            ),
        )
    }

    private enum class RetryAction {
        EmailPassword,
        Google,
    }
}
