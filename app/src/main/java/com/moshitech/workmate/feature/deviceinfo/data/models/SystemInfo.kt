package com.moshitech.workmate.feature.deviceinfo.data.models

data class SystemInfo(
    val androidVersion: String = "Unknown",
    val androidApiLevel: Int = 0,
    val securityPatch: String = "Unknown",
    val kernelVersion: String = "Unknown",
    val buildNumber: String = "Unknown",
    val buildFingerprint: String = "Unknown",
    val bootloader: String = "Unknown",
    val baseband: String = "Unknown",
    val javaVm: String = "Unknown",
    val openGlVersion: String = "Unknown",
    val rootStatus: Boolean = false,
    val seLinuxStatus: String = "Unknown",
    val uptimeMillis: Long = 0L
)
