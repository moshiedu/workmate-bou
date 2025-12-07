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
    TIME_DATE(
        title = "Time & Date",
        emoji = "⏰",
        description = "Time-related calculations and conversions",
        accentColor = Color(0xFF1976D2)
    ),
    CALCULATORS_TOOLS(
        title = "Calculators & Tools",
        emoji = "🧮",
        description = "Special calculators and utilities",
        accentColor = Color(0xFF4CAF50)
    ),
    DIGITAL_TECHNOLOGY(
        title = "Digital & Technology",
        emoji = "💻",
        description = "Computer and digital measurements",
        accentColor = Color(0xFF3F51B5)
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
    SPECIALIZED(
        title = "Specialized",
        emoji = "🎓",
        description = "Professional and specialized conversions",
        accentColor = Color(0xFF9C27B0)
    );

    /**
     * Get all categories that belong to this group
     */
    fun getCategories(): List<UnitCategory> {
        return when (this) {
            BASIC_MEASUREMENTS -> listOf(
                UnitCategory.LENGTH,        // Most common
                UnitCategory.WEIGHT,        // Very common
                UnitCategory.TEMPERATURE,   // Common
                UnitCategory.VOLUME,        // Common
                UnitCategory.AREA           // Less common
            )
            MOTION_PHYSICS -> listOf(
                UnitCategory.SPEED,         // Most common
                UnitCategory.PRESSURE,      // Common (tires, weather)
                UnitCategory.FORCE,         // Moderate
                UnitCategory.ACCELERATION,  // Moderate
                UnitCategory.DENSITY,       // Less common
                UnitCategory.TORQUE         // Specialized
            )
            ENERGY_POWER -> listOf(
                UnitCategory.POWER,         // Most common (electricity bills)
                UnitCategory.ENERGY,        // Common
                UnitCategory.FREQUENCY,     // Moderate (radio, sound)
                UnitCategory.ILLUMINANCE    // Specialized
            )
            DIGITAL_TECHNOLOGY -> listOf(
                UnitCategory.DATA_STORAGE,  // Most common (files, storage)
                UnitCategory.DATA_RATE,     // Common (internet speed)
                UnitCategory.SCREEN_PPI,    // Moderate (displays)
                UnitCategory.DIGITAL_IMAGE, // Moderate
                UnitCategory.TYPOGRAPHY     // Specialized
            )
            TIME_DATE -> listOf(
                UnitCategory.TIME,              // Most common
                UnitCategory.TIME_DATE_CALC,    // Very common (date calculations)
                UnitCategory.TIME_DIFFERENCE,   // Common
                UnitCategory.TIME_AGE,          // Common
                UnitCategory.TIME_ZONES,        // Moderate
                UnitCategory.TIME_TIMESTAMP,    // Moderate (developers)
                UnitCategory.TIME_BIZ_DAYS      // Specialized
            )
            SPECIALIZED -> listOf(
                UnitCategory.BLOOD_GLUCOSE, // Most important (health)
                UnitCategory.FUEL,          // Common (cars)
                UnitCategory.ANGLE,         // Moderate (math, navigation)
                UnitCategory.PAPER_SIZE     // Less common
            )
            CALCULATORS_TOOLS -> listOf(
                UnitCategory.CURRENCY,      // Most common
                UnitCategory.BMI,           // Common (health)
                UnitCategory.MATH_HELPER    // Moderate
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
