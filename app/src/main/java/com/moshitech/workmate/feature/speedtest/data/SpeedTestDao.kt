package com.moshitech.workmate.feature.speedtest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Insert
    suspend fun insert(result: SpeedTestResult)

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<SpeedTestResult>>

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastResult(): SpeedTestResult?
    
    @Query("SELECT * FROM speed_test_results WHERE id = :id")
    suspend fun getResultById(id: Long): SpeedTestResult?
    
    @Query("DELETE FROM speed_test_results")
    suspend fun clearHistory()
    
    @Query("DELETE FROM speed_test_results")
    suspend fun deleteAll() // Alias for clearHistory
    
    @Query("SELECT AVG(downloadSpeed) FROM speed_test_results")
    suspend fun getAverageDownloadSpeed(): Float?
    
    @Query("SELECT AVG(uploadSpeed) FROM speed_test_results")
    suspend fun getAverageUploadSpeed(): Float?
    
    @Query("DELETE FROM speed_test_results WHERE id = :id")
    suspend fun deleteById(id: Long)
}
