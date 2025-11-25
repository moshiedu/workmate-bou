package com.moshitech.workmate.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

class UserPreferencesRepository(private val context: Context) {
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val APP_LOCK_PIN_KEY = stringPreferencesKey("app_lock_pin")
    private val LOCKED_APPS_KEY = stringSetPreferencesKey("locked_apps")
    private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")

    val theme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            AppTheme.valueOf(themeName)
        }

    val appLockPin: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[APP_LOCK_PIN_KEY] }

    val lockedApps: Flow<Set<String>> = context.dataStore.data
        .map { preferences -> preferences[LOCKED_APPS_KEY] ?: emptySet() }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[APP_LOCK_ENABLED_KEY] ?: false }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_PIN_KEY] = pin
        }
    }

    suspend fun setLockedApps(apps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[LOCKED_APPS_KEY] = apps
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] = enabled
        }
    }
}
