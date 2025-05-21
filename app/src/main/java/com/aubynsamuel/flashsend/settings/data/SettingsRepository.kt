package com.aubynsamuel.flashsend.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_MODE = intPreferencesKey("theme_mode")
        private val FONT_SIZE = intPreferencesKey("font_size")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<SettingsState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsState(
                themeMode = ThemeMode.entries[preferences[THEME_MODE] ?: ThemeMode.SYSTEM.ordinal],
                fontSize = preferences[FONT_SIZE] ?: 16,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] != false
            )
        }

    suspend fun saveSettings(state: SettingsState) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = state.themeMode.ordinal
            preferences[FONT_SIZE] = state.fontSize
            preferences[NOTIFICATIONS_ENABLED] = state.notificationsEnabled
        }
    }
}