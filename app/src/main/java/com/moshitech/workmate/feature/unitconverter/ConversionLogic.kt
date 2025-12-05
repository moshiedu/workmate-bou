package com.moshitech.workmate.feature.unitconverter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
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
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class UnitCategory(val title: String, val icon: ImageVector, val accentColor: Color) {
    LENGTH("Length", Icons.Default.Straighten, Color(0xFF2196F3)), // Blue
    WEIGHT("Weight", Icons.Default.FitnessCenter, Color(0xFF4CAF50)), // Green
    TEMPERATURE("Temperature", Icons.Default.Thermostat, Color(0xFFF44336)), // Red
    VOLUME("Volume", Icons.Default.Opacity, Color(0xFF00BCD4)), // Cyan
    AREA("Area", Icons.Default.SquareFoot, Color(0xFF9C27B0)), // Purple
    SPEED("Speed", Icons.Default.Speed, Color(0xFFFF9800)), // Orange
    TIME("Time", Icons.Default.Schedule, Color(0xFF1976D2)), // Dark Blue
    DATA_STORAGE("Data Storage", Icons.Default.DataUsage, Color(0xFF3F51B5)), // Indigo
    FUEL("Fuel", Icons.Default.LocalGasStation, Color(0xFFFFEB3B)), // Yellow
    PRESSURE("Pressure", Icons.Default.Compress, Color(0xFF795548)), // Brown
    ENERGY("Energy", Icons.Default.Bolt, Color(0xFFFFEB3B)), // Yellow/Gold
    FREQUENCY("Frequency", Icons.Default.Waves, Color(0xFF009688)), // Teal
    DIGITAL_IMAGE("Digital Image", Icons.Default.Image, Color(0xFFE91E63)), // Pink
    TORQUE("Torque", Icons.Default.Build, Color(0xFF607D8B)), // Blue Grey
    ACCELERATION("Acceleration", Icons.Default.Speed, Color(0xFFFF5722)), // Deep Orange
    DENSITY("Density", Icons.Default.Grain, Color(0xFF795548)), // Brown
    ANGLE("Angle", Icons.Default.RotateRight, Color(0xFF9C27B0)), // Purple
    DATA_RATE("Data Rate", Icons.Default.NetworkCheck, Color(0xFF4CAF50)), // Green
    FORCE("Force", Icons.Default.Hardware, Color(0xFF607D8B)), // Blue Grey
    ILLUMINANCE("Illuminance", Icons.Default.Lightbulb, Color(0xFFFFC107)), // Amber
    BLOOD_GLUCOSE("Blood Glucose", Icons.Default.WaterDrop, Color(0xFFE53935)), // Red
    POWER("Power", Icons.Default.ElectricBolt, Color(0xFFFFD700)), // Gold
    TYPOGRAPHY("Typography", Icons.Default.TextFields, Color(0xFF3F51B5)), // Indigo
    PAPER_SIZE("Paper Size", Icons.Default.Description, Color(0xFF00BCD4)), // Cyan
    MATH_HELPER("Math Helper", Icons.Default.Calculate, Color(0xFFFF9800)), // Orange
    CURRENCY("Currency", Icons.Default.AttachMoney, Color(0xFF4CAF50)), // Green
    SCREEN_PPI("Screen PPI", Icons.Default.PhoneAndroid, Color(0xFF2196F3)), // Blue
    BMI("BMI", Icons.Default.MonitorWeight, Color(0xFF00BCD4)), // Cyan
    TIME_DATE_CALC("Date Calc", Icons.Default.DateRange, Color(0xFF1976D2)),
    TIME_DIFFERENCE("Difference", Icons.Default.History, Color(0xFF1976D2)),
    TIME_TIMESTAMP("Timestamp", Icons.Default.Schedule, Color(0xFF1976D2)),
    TIME_ZONES("Zones", Icons.Default.Public, Color(0xFF1976D2)),
    TIME_BIZ_DAYS("Biz Days", Icons.Default.Calculate, Color(0xFF1976D2)),
    TIME_AGE("Age", Icons.Default.Timer, Color(0xFF1976D2)),
    MORE("More", Icons.Default.MoreHoriz, Color(0xFF9E9E9E)) // Gray
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
            UnitCategory.TORQUE -> listOf(
                ConversionUnit("Newton Meter", "N·m", 1.0),
                ConversionUnit("Pound-Force Foot", "lbf·ft", 1.355818),
                ConversionUnit("Pound-Force Inch", "lbf·in", 0.112985),
                ConversionUnit("Kilogram-Force Meter", "kgf·m", 9.80665)
            )
            UnitCategory.ACCELERATION -> listOf(
                ConversionUnit("Meter/Second²", "m/s²", 1.0),
                ConversionUnit("Foot/Second²", "ft/s²", 0.3048),
                ConversionUnit("Standard Gravity", "g", 9.80665),
                ConversionUnit("Gal", "Gal", 0.01)
            )
            UnitCategory.DENSITY -> listOf(
                ConversionUnit("Kilogram/Cubic Meter", "kg/m³", 1.0),
                ConversionUnit("Gram/Cubic Centimeter", "g/cm³", 1000.0),
                ConversionUnit("Pound/Cubic Foot", "lb/ft³", 16.0185),
                ConversionUnit("Pound/Cubic Inch", "lb/in³", 27679.9)
            )
            UnitCategory.ANGLE -> listOf(
                ConversionUnit("Degree", "°", 1.0),
                ConversionUnit("Radian", "rad", 57.2958),
                ConversionUnit("Gradian", "grad", 0.9),
                ConversionUnit("Arcminute", "′", 0.0166667),
                ConversionUnit("Arcsecond", "″", 0.000277778)
            )
            UnitCategory.DATA_RATE -> listOf(
                ConversionUnit("Megabit/Second", "Mbps", 1.0),
                ConversionUnit("Kilobit/Second", "Kbps", 0.001),
                ConversionUnit("Gigabit/Second", "Gbps", 1000.0),
                ConversionUnit("Kilobyte/Second", "KB/s", 0.008),
                ConversionUnit("Megabyte/Second", "MB/s", 8.0),
                ConversionUnit("Gigabyte/Second", "GB/s", 8000.0)
            )
            UnitCategory.FORCE -> listOf(
                ConversionUnit("Newton", "N", 1.0),
                ConversionUnit("Kilonewton", "kN", 1000.0),
                ConversionUnit("Pound-Force", "lbf", 4.44822),
                ConversionUnit("Kilogram-Force", "kgf", 9.80665),
                ConversionUnit("Dyne", "dyn", 0.00001)
            )
            UnitCategory.ILLUMINANCE -> listOf(
                ConversionUnit("Lux", "lx", 1.0),
                ConversionUnit("Foot-Candle", "fc", 10.7639),
                ConversionUnit("Phot", "ph", 10000.0)
            )
            UnitCategory.BLOOD_GLUCOSE -> listOf(
                ConversionUnit("mg/dL", "mg/dL", 1.0),
                ConversionUnit("mmol/L", "mmol/L", 18.0) // 1 mmol/L = 18 mg/dL
            )
            UnitCategory.POWER -> listOf(
                ConversionUnit("Watt", "W", 1.0),
                ConversionUnit("Kilowatt", "kW", 1000.0),
                ConversionUnit("Megawatt", "MW", 1000000.0),
                ConversionUnit("Horsepower (Mechanical)", "HP", 745.7),
                ConversionUnit("Metric Horsepower", "PS", 735.5),
                ConversionUnit("BTU/Hour", "BTU/h", 0.293071),
                ConversionUnit("dBm", "dBm", Double.NaN) // Special handling required
            )
            UnitCategory.TYPOGRAPHY -> listOf(
                ConversionUnit("Pixel", "px", 1.0),
                ConversionUnit("Point", "pt", 1.333), // 1pt = 1.333px at 96 DPI
                ConversionUnit("Em", "em", Double.NaN), // Context-dependent
                ConversionUnit("Rem", "rem", Double.NaN), // Context-dependent
                ConversionUnit("Percent", "%", Double.NaN) // Context-dependent
            )
            UnitCategory.PAPER_SIZE -> listOf(
                // ISO A-Series
                ConversionUnit("A0", "841×1189 mm", 999679.0), // Area in mm²
                ConversionUnit("A1", "594×841 mm", 499554.0),
                ConversionUnit("A2", "420×594 mm", 249480.0),
                ConversionUnit("A3", "297×420 mm", 124740.0),
                ConversionUnit("A4", "210×297 mm", 62370.0),
                ConversionUnit("A5", "148×210 mm", 31080.0),
                ConversionUnit("A6", "105×148 mm", 15540.0),
                ConversionUnit("A7", "74×105 mm", 7770.0),
                // ISO B-Series
                ConversionUnit("B4", "250×353 mm", 88250.0),
                ConversionUnit("B5", "176×250 mm", 44000.0),
                // North American
                ConversionUnit("Letter", "216×279 mm", 60264.0),
                ConversionUnit("Legal", "216×356 mm", 76896.0),
                ConversionUnit("Tabloid", "279×432 mm", 120528.0)
            )
            UnitCategory.MATH_HELPER -> listOf(
                ConversionUnit("Percent", "%", 1.0),
                ConversionUnit("Decimal", "dec", 100.0), // 1 decimal = 100%
                ConversionUnit("Fraction", "frac", Double.NaN) // Special handling
            )
            UnitCategory.CURRENCY -> listOf(
                ConversionUnit("US Dollar", "USD", 1.0),
                ConversionUnit("Euro", "EUR", Double.NaN), // User-defined rate
                ConversionUnit("British Pound", "GBP", Double.NaN),
                ConversionUnit("Japanese Yen", "JPY", Double.NaN),
                ConversionUnit("Chinese Yuan", "CNY", Double.NaN),
                ConversionUnit("Indian Rupee", "INR", Double.NaN),
                ConversionUnit("Australian Dollar", "AUD", Double.NaN),
                ConversionUnit("Canadian Dollar", "CAD", Double.NaN),
                ConversionUnit("Swiss Franc", "CHF", Double.NaN),
                ConversionUnit("Hong Kong Dollar", "HKD", Double.NaN),
                ConversionUnit("Singapore Dollar", "SGD", Double.NaN),
                ConversionUnit("Swedish Krona", "SEK", Double.NaN),
                ConversionUnit("South Korean Won", "KRW", Double.NaN),
                ConversionUnit("Norwegian Krone", "NOK", Double.NaN),
                ConversionUnit("Mexican Peso", "MXN", Double.NaN),
                ConversionUnit("Brazilian Real", "BRL", Double.NaN),
                ConversionUnit("South African Rand", "ZAR", Double.NaN),
                ConversionUnit("Russian Ruble", "RUB", Double.NaN),
                ConversionUnit("UAE Dirham", "AED", Double.NaN),
                ConversionUnit("Saudi Riyal", "SAR", Double.NaN)
            )
            else -> emptyList()
        }
    }
}
