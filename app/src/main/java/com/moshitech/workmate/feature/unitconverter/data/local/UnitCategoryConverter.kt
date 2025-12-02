package com.moshitech.workmate.feature.unitconverter.data.local

import androidx.room.TypeConverter
import com.moshitech.workmate.feature.unitconverter.UnitCategory

class UnitCategoryConverter {
    @TypeConverter
    fun fromUnitCategory(value: UnitCategory): String {
        return value.name
    }

    @TypeConverter
    fun toUnitCategory(value: String): UnitCategory {
        return UnitCategory.valueOf(value)
    }
}
