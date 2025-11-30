package com.moshitech.workmate.feature.deviceinfo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BenchmarkRepository(private val dao: BenchmarkHistoryDao) {
    
    fun getAllHistory(): Flow<List<BenchmarkHistoryEntity>> = dao.getAll()
    
    suspend fun getLatest(): BenchmarkHistoryEntity? = dao.getLatest()
    
    fun getHistoryByDateRange(startTime: Long, endTime: Long): Flow<List<BenchmarkHistoryEntity>> =
        dao.getByDateRange(startTime, endTime)
    
    suspend fun saveResult(
        cpuSingleCore: Int,
        cpuMultiCore: Int,
        gpuFps: Int,
        gpuRenderTime: String,
        storageWrite: String,
        storageRead: String,
        ramSequential: String,
        ramRandomAccess: String,
        compositeScore: Int,
        tier: String,
        deviceTemp: Float? = null,
        batteryLevel: Int? = null
    ): Long {
        val entity = BenchmarkHistoryEntity(
            timestamp = System.currentTimeMillis(),
            cpuSingleCoreScore = cpuSingleCore,
            cpuMultiCoreScore = cpuMultiCore,
            gpuFps = gpuFps,
            gpuRenderTimeMs = gpuRenderTime,
            storageWriteSpeedMbPs = storageWrite,
            storageReadSpeedMbPs = storageRead,
            ramSequentialSpeedMbPs = ramSequential,
            ramRandomAccessTimeMs = ramRandomAccess,
            compositeScore = compositeScore,
            performanceTier = tier,
            deviceTemp = deviceTemp,
            batteryLevel = batteryLevel
        )
        return dao.insert(entity)
    }
    
    suspend fun deleteAll() = dao.deleteAll()
    
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    
    suspend fun exportToCsv(): String {
        val results = dao.getAll().first()
        if (results.isEmpty()) return ""
        
        val header = "Timestamp,Composite Score,Tier,CPU Single,CPU Multi,GPU FPS,GPU Render Time,Storage Write,Storage Read,RAM Sequential,RAM Random\n"
        val rows = results.joinToString("\n") { result ->
            "${result.timestamp},${result.compositeScore},${result.performanceTier},${result.cpuSingleCoreScore},${result.cpuMultiCoreScore},${result.gpuFps},${result.gpuRenderTimeMs},${result.storageWriteSpeedMbPs},${result.storageReadSpeedMbPs},${result.ramSequentialSpeedMbPs},${result.ramRandomAccessTimeMs}"
        }
        return header + rows
    }
    
    suspend fun exportToJson(): String {
        val results = dao.getAll().first()
        if (results.isEmpty()) return "[]"
        
        val jsonArray = results.joinToString(",\n  ", "[\n  ", "\n]") { result ->
            """
            {
              "timestamp": ${result.timestamp},
              "compositeScore": ${result.compositeScore},
              "tier": "${result.performanceTier}",
              "cpu": {
                "singleCore": ${result.cpuSingleCoreScore},
                "multiCore": ${result.cpuMultiCoreScore}
              },
              "gpu": {
                "fps": ${result.gpuFps},
                "renderTimeMs": "${result.gpuRenderTimeMs}"
              },
              "storage": {
                "writeMbPs": "${result.storageWriteSpeedMbPs}",
                "readMbPs": "${result.storageReadSpeedMbPs}"
              },
              "ram": {
                "sequentialMbPs": "${result.ramSequentialSpeedMbPs}",
                "randomAccessMs": "${result.ramRandomAccessTimeMs}"
              }
            }
            """.trimIndent()
        }
        return jsonArray
    }
}
