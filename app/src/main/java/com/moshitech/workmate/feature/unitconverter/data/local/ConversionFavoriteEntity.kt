package com.moshitech.workmate.feature.unitconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moshitech.workmate.feature.unitconverter.UnitCategory

@Entity(tableName = "conversion_favorites")
data class ConversionFavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: UnitCategory,
    val fromUnit: String,
    val toUnit: String
)
