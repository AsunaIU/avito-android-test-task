package io.valneva.chatassistant.feature.auth.presentation.authgate

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authInteractor: AuthInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthGateUiState())
    val uiState: StateFlow<AuthGateUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = AuthGateUiState(
            isAuthorized = authInteractor.getCurrentUser() != null,
        )
    }
}
