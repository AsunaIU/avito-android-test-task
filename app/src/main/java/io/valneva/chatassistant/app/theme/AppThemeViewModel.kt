package io.valneva.chatassistant.app.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.valneva.chatassistant.core.data.datastore.ThemePreferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val themePreferencesDataStore: ThemePreferencesDataStore,
) : ViewModel() {

    val uiState: StateFlow<AppThemeUiState> = themePreferencesDataStore.darkThemePreference
        .map { isDarkTheme ->
            AppThemeUiState(isDarkTheme = isDarkTheme)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppThemeUiState(),
        )

    fun onDarkThemeChanged(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesDataStore.setDarkTheme(enabled)
        }
    }
}

data class AppThemeUiState(
    val isDarkTheme: Boolean? = null,
)
