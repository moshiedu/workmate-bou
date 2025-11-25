package com.moshitech.workmate.feature.deviceinfo.data.models

data class DashboardInfo(
    val cpuUsage: Float = 0f,
    val cpuFrequency: String = "--",
    val cpuTemperature: String = "--",
    val ramUsed: Long = 0L,
    val ramTotal: Long = 0L,
    val storageUsed: Long = 0L,
    val storageTotal: Long = 0L,
    val batteryLevel: Int = 0,
    val batteryTemperature: Float = 0f,
    val batteryStatus: String = "Unknown",
    val networkType: String = "None",
    val signalStrength: String = "--",
    val uptime: String = "00:00:00"
)
