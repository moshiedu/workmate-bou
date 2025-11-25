package com.moshitech.workmate.feature.deviceinfo.utils

import android.text.format.Formatter
import java.util.concurrent.TimeUnit

object FormatUtils {
    
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    fun formatFrequency(khz: Long): String {
        return when {
            khz < 1000 -> "$khz KHz"
            khz < 1000000 -> String.format("%.2f MHz", khz / 1000.0)
            else -> String.format("%.2f GHz", khz / 1000000.0)
        }
    }
    
    fun formatUptime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    fun formatTemperature(celsius: Float): String {
        return String.format("%.1f°C", celsius)
    }
    
    fun formatPercentage(value: Float): String {
        return String.format("%.1f%%", value)
    }
}
