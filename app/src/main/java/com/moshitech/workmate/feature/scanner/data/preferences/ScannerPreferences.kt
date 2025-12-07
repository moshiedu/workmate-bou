package com.moshitech.workmate.feature.scanner.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.moshitech.workmate.feature.scanner.domain.model.FlashMode
import com.moshitech.workmate.feature.scanner.domain.model.ScannerConfig

/**
 * Manages scanner settings persistence
 */
class ScannerPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "scanner_preferences",
        Context.MODE_PRIVATE
    )
    
    fun saveConfig(config: ScannerConfig) {
        prefs.edit().apply {
            putString("flash_mode", config.flashMode.name)
            putBoolean("auto_capture", config.autoCapture)
            putBoolean("batch_mode", config.batchMode)
            putBoolean("hd_quality", config.hdQuality)
            putBoolean("sound_enabled", config.soundEnabled)
            apply()
        }
    }
    
    fun loadConfig(): ScannerConfig {
        return ScannerConfig(
            flashMode = FlashMode.valueOf(
                prefs.getString("flash_mode", FlashMode.OFF.name) ?: FlashMode.OFF.name
            ),
            autoCapture = prefs.getBoolean("auto_capture", true),
            batchMode = prefs.getBoolean("batch_mode", false),
            hdQuality = prefs.getBoolean("hd_quality", true),
            soundEnabled = prefs.getBoolean("sound_enabled", true)
        )
    }
}
