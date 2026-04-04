package io.valneva.chatassistant.feature.auth.presentation.register

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
class RegisterViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<RegisterUiEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: SharedFlow<RegisterUiEffect> = _effects.asSharedFlow()

    private var canRetrySubmit: Boolean = false

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

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordErrorRes = null,
            )
        }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update {
            it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
        }
    }

    fun onSubmitClick() {
        if (_uiState.value.isLoading || !validateInputs()) return
        submitRegistration()
    }

    fun onRetryRequested() {
        if (canRetrySubmit) {
            submitRegistration()
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        val emailError = AuthFormValidator.validateEmail(state.email)
        val passwordError = AuthFormValidator.validatePassword(state.password)
        val confirmPasswordError = AuthFormValidator.validateConfirmPassword(
            password = state.password,
            confirmPassword = state.confirmPassword,
        )

        _uiState.update {
            it.copy(
                emailErrorRes = emailError,
                passwordErrorRes = passwordError,
                confirmPasswordErrorRes = confirmPasswordError,
            )
        }

        return emailError == null && passwordError == null && confirmPasswordError == null
    }

    private fun submitRegistration() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authInteractor.signUp(email = email, password = password)
            if (result.isSuccess) {
                canRetrySubmit = false
                _uiState.update { it.copy(isLoading = false) }
                _effects.emit(RegisterUiEffect.NavigateToChats)
            } else {
                handleAuthFailure(result.exceptionOrNull())
            }
        }
    }

    private suspend fun handleAuthFailure(throwable: Throwable?) {
        val error = throwable?.toAuthError() ?: AuthError.UNKNOWN
        val canRetry = error == AuthError.NO_NETWORK
        canRetrySubmit = canRetry
        _uiState.update { it.copy(isLoading = false) }
        _effects.emit(
            RegisterUiEffect.ShowSnackbar(
                messageRes = error.toMessageRes(),
                showRetry = canRetry,
            ),
        )
    }
}