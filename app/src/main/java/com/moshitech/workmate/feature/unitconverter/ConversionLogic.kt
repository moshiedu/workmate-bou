package com.moshitech.workmate.feature.unitconverter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class UnitCategory(val title: String, val icon: ImageVector) {
    LENGTH("Length", Icons.Default.Straighten),
    WEIGHT("Weight", Icons.Default.FitnessCenter),
    TEMPERATURE("Temperature", Icons.Default.Thermostat),
    VOLUME("Volume", Icons.Default.Opacity),
    AREA("Area", Icons.Default.SquareFoot),
    SPEED("Speed", Icons.Default.Speed),
    TIME("Time", Icons.Default.Schedule),
    DATA_STORAGE("Data Storage", Icons.Default.DataUsage),
    FUEL("Fuel", Icons.Default.LocalGasStation),
    PRESSURE("Pressure", Icons.Default.Compress),
    ENERGY("Energy", Icons.Default.Bolt),
    FREQUENCY("Frequency", Icons.Default.Waves),
    DIGITAL_IMAGE("Digital Image", Icons.Default.Image),
    MORE("More", Icons.Default.MoreHoriz)
}

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val factor: Double, // Factor to convert TO base unit
    val offset: Double = 0.0 // For Temperature
)

data class ConversionFavorite(
    val id: String,
    val category: UnitCategory,
    val fromUnit: String,
    val toUnit: String
)

object ConversionRepository {
    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> listOf(
                ConversionUnit("Meter", "m", 1.0),
                ConversionUnit("Kilometer", "km", 1000.0),
                ConversionUnit("Centimeter", "cm", 0.01),
                ConversionUnit("Millimeter", "mm", 0.001),
                ConversionUnit("Mile", "mi", 1609.34),
                ConversionUnit("Yard", "yd", 0.9144),
                ConversionUnit("Foot", "ft", 0.3048),
                ConversionUnit("Inch", "in", 0.0254),
                ConversionUnit("Nautical Mile", "nmi", 1852.0)
            )
            UnitCategory.WEIGHT -> listOf(
                ConversionUnit("Kilogram", "kg", 1.0),
                ConversionUnit("Gram", "g", 0.001),
                ConversionUnit("Milligram", "mg", 0.000001),
                ConversionUnit("Tonne", "t", 1000.0),
                ConversionUnit("Pound", "lb", 0.453592),
                ConversionUnit("Ounce", "oz", 0.0283495),
                ConversionUnit("Stone", "st", 6.35029)
            )
            UnitCategory.TEMPERATURE -> listOf(
                ConversionUnit("Celsius", "°C", 1.0, 0.0),
                ConversionUnit("Fahrenheit", "°F", 0.5555555555555556, -32.0), // (F - 32) * 5/9 = C
                ConversionUnit("Kelvin", "K", 1.0, -273.15) // K - 273.15 = C
            )
            UnitCategory.VOLUME -> listOf(
                ConversionUnit("Liter", "L", 1.0),
                ConversionUnit("Milliliter", "mL", 0.001),
                ConversionUnit("Cubic Meter", "m³", 1000.0),
                ConversionUnit("Gallon (US)", "gal", 3.78541),
                ConversionUnit("Gallon (UK)", "gal", 4.54609),
                ConversionUnit("Quart (US)", "qt", 0.946353),
                ConversionUnit("Pint (US)", "pt", 0.473176),
                ConversionUnit("Cup (US)", "cup", 0.236588),
                ConversionUnit("Fluid Ounce (US)", "fl oz", 0.0295735),
                ConversionUnit("Tablespoon (US)", "tbsp", 0.0147868),
                ConversionUnit("Teaspoon (US)", "tsp", 0.00492892)
            )
            UnitCategory.AREA -> listOf(
                ConversionUnit("Square Meter", "m²", 1.0),
                ConversionUnit("Square Kilometer", "km²", 1000000.0),
                ConversionUnit("Hectare", "ha", 10000.0),
                ConversionUnit("Acre", "ac", 4046.86),
                ConversionUnit("Square Mile", "mi²", 2589988.11),
                ConversionUnit("Square Yard", "yd²", 0.836127),
                ConversionUnit("Square Foot", "ft²", 0.092903),
                ConversionUnit("Square Inch", "in²", 0.00064516)
            )
            UnitCategory.SPEED -> listOf(
                ConversionUnit("Meter/Second", "m/s", 1.0),
                ConversionUnit("Kilometer/Hour", "km/h", 0.277778),
                ConversionUnit("Mile/Hour", "mph", 0.44704),
                ConversionUnit("Knot", "kn", 0.514444),
                ConversionUnit("Foot/Second", "ft/s", 0.3048)
            )
            UnitCategory.TIME -> listOf(
                ConversionUnit("Second", "s", 1.0),
                ConversionUnit("Millisecond", "ms", 0.001),
                ConversionUnit("Microsecond", "μs", 0.000001),
                ConversionUnit("Nanosecond", "ns", 0.000000001),
                ConversionUnit("Minute", "min", 60.0),
                ConversionUnit("Hour", "h", 3600.0),
                ConversionUnit("Day", "d", 86400.0),
                ConversionUnit("Week", "wk", 604800.0),
                ConversionUnit("Month (Avg)", "mo", 2628000.0),
                ConversionUnit("Year (Avg)", "yr", 31536000.0)
            )
            UnitCategory.DATA_STORAGE -> listOf(
                ConversionUnit("Bit", "b", 0.125),
                ConversionUnit("Byte", "B", 1.0),
                ConversionUnit("Kilobit", "Kb", 128.0),
                ConversionUnit("Kilobyte", "KB", 1024.0),
                ConversionUnit("Megabit", "Mb", 131072.0),
                ConversionUnit("Megabyte", "MB", 1048576.0),
                ConversionUnit("Gigabit", "Gb", 134217728.0),
                ConversionUnit("Gigabyte", "GB", 1073741824.0),
                ConversionUnit("Terabyte", "TB", 1099511627776.0),
                ConversionUnit("Petabyte", "PB", 1125899906842624.0),
                ConversionUnit("Exabyte", "EB", 1.152921504606847e18),
                ConversionUnit("Zettabyte", "ZB", 1.1805916207174113e21),
                ConversionUnit("Yottabyte", "YB", 1.2089258196146292e24)
            )
            UnitCategory.FUEL -> listOf(
                ConversionUnit("Kilometers/Liter", "km/L", 1.0), // Base
                ConversionUnit("Miles/Gallon (US)", "mpg", 0.425144),
                ConversionUnit("Miles/Gallon (UK)", "mpg", 0.354006)
            )
            UnitCategory.PRESSURE -> listOf(
                ConversionUnit("Pascal", "Pa", 1.0),
                ConversionUnit("Bar", "bar", 100000.0),
                ConversionUnit("PSI", "psi", 6894.76),
                ConversionUnit("Standard Atmosphere", "atm", 101325.0),
                ConversionUnit("Torr", "Torr", 133.322)
            )
            UnitCategory.ENERGY -> listOf(
                ConversionUnit("Joule", "J", 1.0),
                ConversionUnit("Kilojoule", "kJ", 1000.0),
                ConversionUnit("Gram Calorie", "cal", 4.184),
                ConversionUnit("Kilocalorie", "kcal", 4184.0),
                ConversionUnit("Watt Hour", "Wh", 3600.0),
                ConversionUnit("Kilowatt Hour", "kWh", 3600000.0),
                ConversionUnit("Electronvolt", "eV", 1.60218e-19)
            )
            UnitCategory.FREQUENCY -> listOf(
                ConversionUnit("Hertz", "Hz", 1.0),
                ConversionUnit("Kilohertz", "kHz", 1000.0),
                ConversionUnit("Megahertz", "MHz", 1000000.0),
                ConversionUnit("Gigahertz", "GHz", 1000000000.0)
            )
            UnitCategory.DIGITAL_IMAGE -> listOf(
                ConversionUnit("Pixel", "px", 1.0),
                ConversionUnit("Inch", "in", 0.0), // Factor depends on DPI
                ConversionUnit("Centimeter", "cm", 0.0), // Factor depends on DPI
                ConversionUnit("Millimeter", "mm", 0.0) // Factor depends on DPI
            )
            else -> emptyList()
        }
    }
}
