package io.valneva.chatassistant.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.themePreferencesDataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val darkThemePreference: Flow<Boolean?> = context.themePreferencesDataStore.data
        .map { preferences ->
            preferences[DARK_THEME_KEY]
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.themePreferencesDataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
    }
}
