package com.moshitech.workmate.feature.deviceinfo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkHistoryDao {
    @Insert
    suspend fun insert(history: BenchmarkHistoryEntity): Long
    
    @Query("SELECT * FROM benchmark_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BenchmarkHistoryEntity>>
    
    @Query("SELECT * FROM benchmark_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): BenchmarkHistoryEntity?
    
    @Query("SELECT * FROM benchmark_history WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<BenchmarkHistoryEntity>>
    
    @Query("DELETE FROM benchmark_history")
    suspend fun deleteAll()
    
    @Query("DELETE FROM benchmark_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
