package com.moshitech.workmate.feature.speedtest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_results")
data class SpeedTestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val downloadSpeed: Float, // Mbps
    val uploadSpeed: Float, // Mbps
    val ping: Long, // ms
    val networkType: String, // WiFi, 5G, 4G, etc.
    val ipAddress: String,
    val isp: String
)
