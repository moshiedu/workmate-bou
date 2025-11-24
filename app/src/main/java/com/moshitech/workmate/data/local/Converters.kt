package com.moshitech.workmate.data.local

import androidx.room.TypeConverter
import com.moshitech.workmate.feature.unitconverter.UnitCategory

class Converters {
    @TypeConverter
    fun fromUnitCategory(category: UnitCategory): String {
        return category.name
    }

    @TypeConverter
    fun toUnitCategory(value: String): UnitCategory {
        return try {
            UnitCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            UnitCategory.LENGTH // Fallback
        }
    }
}
