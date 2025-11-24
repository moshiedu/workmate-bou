package com.moshitech.workmate.feature.unitconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unit_conversion_history")
data class UnitConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val fromUnit: String,
    val toUnit: String,
    val inputValue: String,
    val resultValue: String,
    val timestamp: Long
)
