package io.valneva.chatassistant.app.startup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.app.navigation.AppRoute
import io.valneva.chatassistant.feature.auth.domain.AuthInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppLaunchViewModel @Inject constructor(
    authInteractor: AuthInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLaunchUiState())
    val uiState: StateFlow<AppLaunchUiState> = _uiState.asStateFlow()

    init {
        val startDestination = if (authInteractor.getCurrentUser() != null) {
            AppRoute.Main.route
        } else {
            AppRoute.Login.route
        }

        _uiState.value = AppLaunchUiState(
            isReady = true,
            startDestination = startDestination,
        )
    }
}

data class AppLaunchUiState(
    val isReady: Boolean = false,
    val startDestination: String? = null,
)
