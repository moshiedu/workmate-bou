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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moshitech.workmate.feature.imagestudio.data.ConversionPreset

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

class UserPreferencesRepository(private val context: Context) {
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val APP_LOCK_PIN_KEY = stringPreferencesKey("app_lock_pin")
    private val LOCKED_APPS_KEY = stringSetPreferencesKey("locked_apps")
    private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")

    private val USE_PIN_AUTH_KEY = booleanPreferencesKey("use_pin_auth")
    private val USE_BIOMETRIC_AUTH_KEY = booleanPreferencesKey("use_biometric_auth")
    private val SECURITY_QUESTION_KEY = stringPreferencesKey("security_question")
    private val SECURITY_ANSWER_KEY = stringPreferencesKey("security_answer")

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

    val usePinAuth: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[USE_PIN_AUTH_KEY] ?: true }

    val useBiometricAuth: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[USE_BIOMETRIC_AUTH_KEY] ?: true }

    val securityQuestion: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SECURITY_QUESTION_KEY] }

    val securityAnswer: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SECURITY_ANSWER_KEY] }

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

    suspend fun setUsePinAuth(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_PIN_AUTH_KEY] = enabled
        }
    }

    suspend fun setUseBiometricAuth(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_BIOMETRIC_AUTH_KEY] = enabled
        }
    }

    suspend fun setSecurityQuestion(question: String, answer: String) {
        context.dataStore.edit { preferences ->
            preferences[SECURITY_QUESTION_KEY] = question
            preferences[SECURITY_ANSWER_KEY] = answer.lowercase().trim()
        }
    }

    suspend fun verifySecurityAnswer(answer: String): Boolean {
        val savedAnswer = securityAnswer.firstOrNull()
        return savedAnswer != null && savedAnswer == answer.lowercase().trim()
    }
    
    // Batch Converter
    private val BATCH_OUTPUT_FOLDER_KEY = stringPreferencesKey("batch_output_folder")
    val batchOutputFolder: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[BATCH_OUTPUT_FOLDER_KEY] }
        
    suspend fun setBatchOutputFolder(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[BATCH_OUTPUT_FOLDER_KEY] = uri
        }
    }
    
    // Custom Presets Logic
    private val CUSTOM_PRESETS_KEY = stringPreferencesKey("custom_image_presets")
    private val gson = Gson()
    
    val customPresets: Flow<List<ConversionPreset>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[CUSTOM_PRESETS_KEY]
            if (json.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<ConversionPreset>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        
    suspend fun savePreset(preset: ConversionPreset) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_PRESETS_KEY]
            val currentList = if (currentJson.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    val type = object : TypeToken<MutableList<ConversionPreset>>() {}.type
                    gson.fromJson<MutableList<ConversionPreset>>(currentJson, type)
                } catch (e: Exception) {
                    mutableListOf()
                }
            }
            
            // Remove existing with same name if any (overwrite)
            currentList.removeAll { it.name == preset.name }
            currentList.add(preset)
            
            preferences[CUSTOM_PRESETS_KEY] = gson.toJson(currentList)
        }
    }
    
    suspend fun deletePreset(presetName: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_PRESETS_KEY]
            if (!currentJson.isNullOrBlank()) {
                try {
                    val type = object : TypeToken<MutableList<ConversionPreset>>() {}.type
                    val currentList = gson.fromJson<MutableList<ConversionPreset>>(currentJson, type)
                    currentList.removeAll { it.name == presetName }
                    preferences[CUSTOM_PRESETS_KEY] = gson.toJson(currentList)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
