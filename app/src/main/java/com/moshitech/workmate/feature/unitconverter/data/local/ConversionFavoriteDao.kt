package com.moshitech.workmate.feature.unitconverter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.moshitech.workmate.feature.unitconverter.UnitCategory

@Dao
interface ConversionFavoriteDao {
    @Query("SELECT * FROM conversion_favorites")
    fun getFavorites(): Flow<List<ConversionFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: ConversionFavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: ConversionFavoriteEntity)

    @Query("SELECT * FROM conversion_favorites WHERE category = :category AND fromUnit = :fromUnit AND toUnit = :toUnit LIMIT 1")
    suspend fun getFavorite(category: UnitCategory, fromUnit: String, toUnit: String): ConversionFavoriteEntity?
}
