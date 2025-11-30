package com.moshitech.workmate.feature.deviceinfo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benchmark_history")
data class BenchmarkHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val cpuSingleCoreScore: Int,
    val cpuMultiCoreScore: Int,
    val gpuFps: Int,
    val gpuRenderTimeMs: String,
    val storageWriteSpeedMbPs: String,
    val storageReadSpeedMbPs: String,
    val ramSequentialSpeedMbPs: String,
    val ramRandomAccessTimeMs: String,
    val compositeScore: Int,
    val performanceTier: String,
    val deviceTemp: Float? = null,
    val batteryLevel: Int? = null
)
