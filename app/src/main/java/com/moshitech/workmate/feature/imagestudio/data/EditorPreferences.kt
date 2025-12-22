package com.moshitech.workmate.feature.imagestudio.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.editorPreferences by preferencesDataStore(name = "editor_preferences")

class EditorPreferences(private val context: Context) {
    
    companion object {
        val AUTO_APPLY_LAYERS = stringPreferencesKey("auto_apply_layers")
        val SHOW_CROP_CONFIRMATION = booleanPreferencesKey("show_crop_confirmation")
        val HAS_SEEN_LAYER_PREFERENCE_DIALOG = booleanPreferencesKey("has_seen_layer_pref_dialog")
    }
    
    enum class LayerApplicationMode {
        ALWAYS_AUTO,    // Auto-apply before crop/rotate
        ASK_EACH_TIME,  // Show dialog each time
        NEVER_AUTO      // Manual apply only
    }
    
    val layerApplicationMode: Flow<LayerApplicationMode> = context.editorPreferences.data
        .map { preferences ->
            val mode = preferences[AUTO_APPLY_LAYERS] ?: LayerApplicationMode.ASK_EACH_TIME.name
            LayerApplicationMode.valueOf(mode)
        }
    
    val showCropConfirmation: Flow<Boolean> = context.editorPreferences.data
        .map { preferences ->
            preferences[SHOW_CROP_CONFIRMATION] ?: true
        }
    
    val hasSeenLayerPreferenceDialog: Flow<Boolean> = context.editorPreferences.data
        .map { preferences ->
            preferences[HAS_SEEN_LAYER_PREFERENCE_DIALOG] ?: false
        }
    
    suspend fun setLayerApplicationMode(mode: LayerApplicationMode) {
        context.editorPreferences.edit { preferences ->
            preferences[AUTO_APPLY_LAYERS] = mode.name
            preferences[HAS_SEEN_LAYER_PREFERENCE_DIALOG] = true
        }
    }
    
    suspend fun setShowCropConfirmation(show: Boolean) {
        context.editorPreferences.edit { preferences ->
            preferences[SHOW_CROP_CONFIRMATION] = show
        }
    }
}
