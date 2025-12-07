package com.moshitech.workmate.feature.scanner.domain.model

/**
 * Scanner configuration options
 */
data class ScannerConfig(
    val flashMode: FlashMode = FlashMode.OFF,
    val autoCapture: Boolean = true,
    val batchMode: Boolean = false,
    val hdQuality: Boolean = true,
    val soundEnabled: Boolean = true
)

enum class FlashMode {
    OFF,
    ON,
    AUTO,
    TORCH  // Always on (flashlight)
}
