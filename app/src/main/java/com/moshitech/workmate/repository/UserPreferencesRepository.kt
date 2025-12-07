package com.moshitech.workmate.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    
    companion object {
        private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
    }
    
    enum class ViewMode {
        GRID, LIST, GROUPED
    }
    
    val viewMode: Flow<ViewMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[VIEW_MODE_KEY] ?: ViewMode.GRID.name
        ViewMode.valueOf(mode)
    }
    
    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_FEEDBACK_KEY] ?: false // Default: OFF
    }
    
    suspend fun setViewMode(mode: ViewMode) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode.name
        }
    }
    
    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }
}
