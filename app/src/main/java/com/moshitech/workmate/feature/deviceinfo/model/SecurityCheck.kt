package com.moshitech.workmate.feature.deviceinfo.model

enum class SecuritySeverity {
    CRITICAL,
    WARNING,
    INFO,
    PASS
}

data class SecurityCheck(
    val id: String,
    val title: String,
    val description: String,
    val severity: SecuritySeverity,
    val passed: Boolean,
    val recommendation: String,
    val details: String = "",
    val fixSteps: List<String> = emptyList(),
    val settingsAction: String? = null  // Intent action to open relevant settings
)

data class SecurityScanResult(
    val timestamp: Long,
    val score: Int, // 0-100
    val checks: List<SecurityCheck>,
    val overallStatus: Boolean
)
