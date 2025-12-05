package com.moshitech.workmate.feature.unitconverter

import androidx.compose.ui.graphics.Color

/**
 * Logical grouping of unit conversion categories for better organization
 */
enum class CategoryGroup(
    val title: String,
    val emoji: String,
    val description: String,
    val accentColor: Color
) {
    BASIC_MEASUREMENTS(
        title = "Basic Measurements",
        emoji = "📏",
        description = "Everyday measurements for common use",
        accentColor = Color(0xFF2196F3)
    ),
    MOTION_PHYSICS(
        title = "Motion & Physics",
        emoji = "⚡",
        description = "Speed, force, and physical properties",
        accentColor = Color(0xFFFF9800)
    ),
    ENERGY_POWER(
        title = "Energy & Power",
        emoji = "🔋",
        description = "Electrical and energy conversions",
        accentColor = Color(0xFFFFC107)
    ),
    DIGITAL_TECHNOLOGY(
        title = "Digital & Technology",
        emoji = "💻",
        description = "Computer and digital measurements",
        accentColor = Color(0xFF3F51B5)
    ),
    TIME_DATE(
        title = "Time & Date",
        emoji = "⏰",
        description = "Time-related calculations and conversions",
        accentColor = Color(0xFF1976D2)
    ),
    SPECIALIZED(
        title = "Specialized",
        emoji = "🎓",
        description = "Professional and specialized conversions",
        accentColor = Color(0xFF9C27B0)
    ),
    CALCULATORS_TOOLS(
        title = "Calculators & Tools",
        emoji = "🧮",
        description = "Special calculators and utilities",
        accentColor = Color(0xFF4CAF50)
    );

    /**
     * Get all categories that belong to this group
     */
    fun getCategories(): List<UnitCategory> {
        return when (this) {
            BASIC_MEASUREMENTS -> listOf(
                UnitCategory.LENGTH,
                UnitCategory.WEIGHT,
                UnitCategory.TEMPERATURE,
                UnitCategory.VOLUME,
                UnitCategory.AREA
            )
            MOTION_PHYSICS -> listOf(
                UnitCategory.SPEED,
                UnitCategory.ACCELERATION,
                UnitCategory.FORCE,
                UnitCategory.TORQUE,
                UnitCategory.PRESSURE,
                UnitCategory.DENSITY
            )
            ENERGY_POWER -> listOf(
                UnitCategory.ENERGY,
                UnitCategory.POWER,
                UnitCategory.FREQUENCY,
                UnitCategory.ILLUMINANCE
            )
            DIGITAL_TECHNOLOGY -> listOf(
                UnitCategory.DATA_STORAGE,
                UnitCategory.DATA_RATE,
                UnitCategory.DIGITAL_IMAGE,
                UnitCategory.TYPOGRAPHY,
                UnitCategory.SCREEN_PPI
            )
            TIME_DATE -> listOf(
                UnitCategory.TIME,
                UnitCategory.TIME_DATE_CALC,
                UnitCategory.TIME_DIFFERENCE,
                UnitCategory.TIME_TIMESTAMP,
                UnitCategory.TIME_ZONES,
                UnitCategory.TIME_BIZ_DAYS,
                UnitCategory.TIME_AGE
            )
            SPECIALIZED -> listOf(
                UnitCategory.ANGLE,
                UnitCategory.FUEL,
                UnitCategory.PAPER_SIZE,
                UnitCategory.BLOOD_GLUCOSE
            )
            CALCULATORS_TOOLS -> listOf(
                UnitCategory.BMI,
                UnitCategory.MATH_HELPER,
                UnitCategory.CURRENCY
            )
        }
    }

    companion object {
        /**
         * Get the group for a specific category
         */
        fun getGroupForCategory(category: UnitCategory): CategoryGroup {
            return values().first { group ->
                category in group.getCategories()
            }
        }

        /**
         * Get all groups with their categories
         */
        fun getAllGroupedCategories(): Map<CategoryGroup, List<UnitCategory>> {
            return values().associateWith { it.getCategories() }
        }
    }
}
