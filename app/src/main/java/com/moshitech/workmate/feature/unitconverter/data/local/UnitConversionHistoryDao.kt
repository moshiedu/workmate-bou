package com.moshitech.workmate.feature.unitconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitConversionHistoryDao {
    @Query("SELECT * FROM unit_conversion_history ORDER BY timestamp DESC LIMIT 20")
    fun getHistory(): Flow<List<UnitConversionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(conversion: UnitConversionHistoryEntity)

    @Query("DELETE FROM unit_conversion_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM unit_conversion_history WHERE category = :category ORDER BY timestamp DESC LIMIT 20")
    fun getHistoryByCategory(category: String): Flow<List<UnitConversionHistoryEntity>>
}
