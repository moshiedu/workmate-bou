package com.moshitech.workmate.feature.imagestudio.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionHistoryDao {
    @Query("SELECT * FROM conversion_history_table ORDER BY date DESC")
    fun getAll(): Flow<List<ConversionHistoryEntity>>

    @Insert
    suspend fun insert(item: ConversionHistoryEntity)

    @Delete
    suspend fun delete(item: ConversionHistoryEntity)
    
    @Query("DELETE FROM conversion_history_table")
    suspend fun deleteAll()
}
