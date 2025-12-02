package com.moshitech.workmate.feature.unitconverter.repository

import android.content.Context
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.unitconverter.UnitCategory
import com.moshitech.workmate.feature.unitconverter.data.local.ConversionFavoriteEntity
import com.moshitech.workmate.feature.unitconverter.data.local.UnitConversionHistoryEntity
import kotlinx.coroutines.flow.Flow

class UnitConverterRepository(context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val favoriteDao by lazy { database.conversionFavoriteDao() }
    private val historyDao by lazy { database.unitConversionHistoryDao() }

    fun getFavorites(): Flow<List<ConversionFavoriteEntity>> = favoriteDao.getFavorites()

    suspend fun addFavorite(category: UnitCategory, fromUnit: String, toUnit: String) {
        val favorite = ConversionFavoriteEntity(
            category = category,
            fromUnit = fromUnit,
            toUnit = toUnit
        )
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(favorite: ConversionFavoriteEntity) {
        favoriteDao.deleteFavorite(favorite)
    }

    suspend fun isFavorite(category: UnitCategory, fromUnit: String, toUnit: String): Boolean {
        return favoriteDao.getFavorite(category, fromUnit, toUnit) != null
    }
    
    suspend fun toggleFavorite(category: UnitCategory, fromUnit: String, toUnit: String) {
        val existing = favoriteDao.getFavorite(category, fromUnit, toUnit)
        if (existing != null) {
            favoriteDao.deleteFavorite(existing)
        } else {
            addFavorite(category, fromUnit, toUnit)
        }
    }

    // History methods
    fun getHistory() = historyDao.getHistory()
    
    fun getHistoryByCategory(category: String) = historyDao.getHistoryByCategory(category)

    suspend fun saveConversion(
        category: String,
        fromUnit: String,
        toUnit: String,
        inputValue: String,
        resultValue: String
    ) {
        val entity = UnitConversionHistoryEntity(
            category = category,
            fromUnit = fromUnit,
            toUnit = toUnit,
            inputValue = inputValue,
            resultValue = resultValue,
            timestamp = System.currentTimeMillis()
        )
        historyDao.insertConversion(entity)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}
