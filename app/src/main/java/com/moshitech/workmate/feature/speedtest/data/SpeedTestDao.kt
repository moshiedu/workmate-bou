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
    
    @Query("DELETE FROM speed_test_results")
    suspend fun clearHistory()
}
