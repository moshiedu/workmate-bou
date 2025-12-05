package com.moshitech.workmate.feature.unitconverter

/**
 * Help information for a unit conversion category
 */
data class CategoryHelp(
    val category: UnitCategory,
    val title: String,
    val description: String,
    val useCases: List<String>,
    val tips: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val disclaimer: String? = null  // Optional medical/legal disclaimer
)

/**
 * Repository providing help content for all unit conversion categories
 */
object CategoryHelpRepository {
    
    fun getHelpForCategory(category: UnitCategory): CategoryHelp {
        return when (category) {
            UnitCategory.LENGTH -> CategoryHelp(
                category = UnitCategory.LENGTH,
                title = "Length Converter",
                description = "Convert between different units of distance and length. Supports metric (meters, kilometers) and imperial (feet, miles) systems.",
                useCases = listOf(
                    "Measuring room dimensions for furniture",
                    "Converting height between cm and feet",
                    "Calculating travel distances",
                    "Engineering and construction projects"
                ),
                tips = listOf(
                    "1 meter = 3.28 feet",
                    "1 kilometer = 0.62 miles",
                    "Use centimeters for small measurements"
                ),
                examples = listOf(
                    "Convert 6 feet to meters for international forms",
                    "Calculate room size: 12 feet × 15 feet in meters"
                )
            )

            UnitCategory.WEIGHT -> CategoryHelp(
                category = UnitCategory.WEIGHT,
                title = "Weight Converter",
                description = "Convert mass and weight between metric (kilograms, grams) and imperial (pounds, ounces) systems.",
                useCases = listOf(
                    "Cooking and recipe conversions",
                    "Fitness and body weight tracking",
                    "Shipping package weight",
                    "Luggage weight for travel"
                ),
                tips = listOf(
                    "1 kilogram = 2.2 pounds",
                    "1 pound = 16 ounces",
                    "Use grams for precise measurements"
                ),
                examples = listOf(
                    "Convert recipe ingredients from cups to grams",
                    "Check if luggage is under 23 kg (50 lbs) limit"
                )
            )

            UnitCategory.TEMPERATURE -> CategoryHelp(
                category = UnitCategory.TEMPERATURE,
                title = "Temperature Converter",
                description = "Convert between Celsius, Fahrenheit, and Kelvin temperature scales.",
                useCases = listOf(
                    "Understanding weather forecasts",
                    "Cooking and baking temperatures",
                    "Scientific calculations",
                    "International travel"
                ),
                tips = listOf(
                    "Water freezes at 0°C / 32°F / 273K",
                    "Water boils at 100°C / 212°F / 373K",
                    "Body temperature: 37°C / 98.6°F"
                ),
                examples = listOf(
                    "Convert oven temperature from Fahrenheit to Celsius",
                    "Understand weather: 25°C is comfortable (77°F)"
                )
            )

            UnitCategory.VOLUME -> CategoryHelp(
                category = UnitCategory.VOLUME,
                title = "Volume Converter",
                description = "Convert liquid and volume measurements between liters, gallons, cups, and more.",
                useCases = listOf(
                    "Cooking and recipe measurements",
                    "Fuel tank capacity",
                    "Container and bottle sizes",
                    "Aquarium and pool volumes"
                ),
                tips = listOf(
                    "1 liter = 4.23 cups (US)",
                    "1 gallon (US) = 3.79 liters",
                    "1 tablespoon = 3 teaspoons"
                ),
                examples = listOf(
                    "Convert recipe from cups to milliliters",
                    "Calculate pool capacity in gallons"
                )
            )

            UnitCategory.AREA -> CategoryHelp(
                category = UnitCategory.AREA,
                title = "Area Converter",
                description = "Convert surface area measurements for land, rooms, and spaces.",
                useCases = listOf(
                    "Real estate and property size",
                    "Room and floor area",
                    "Land and farm measurements",
                    "Paint and flooring calculations"
                ),
                tips = listOf(
                    "1 acre = 4,047 square meters",
                    "1 hectare = 2.47 acres",
                    "Square footage = length × width in feet"
                ),
                examples = listOf(
                    "Convert apartment size from sq ft to sq meters",
                    "Calculate land area in acres"
                )
            )

            UnitCategory.BLOOD_GLUCOSE -> CategoryHelp(
                category = UnitCategory.BLOOD_GLUCOSE,
                title = "Blood Glucose Converter",
                description = "Convert blood sugar readings between mg/dL (US standard) and mmol/L (international standard).",
                useCases = listOf(
                    "Understanding glucose meter readings from different countries",
                    "Comparing lab results",
                    "Diabetes management and monitoring",
                    "Medical documentation and records"
                ),
                tips = listOf(
                    "Normal fasting: 70-100 mg/dL or 3.9-5.6 mmol/L",
                    "1 mmol/L = 18 mg/dL",
                    "Always consult healthcare provider for medical decisions",
                    "Different countries use different standards"
                ),
                examples = listOf(
                    "Convert US glucose reading to international format",
                    "Understand lab results from different countries",
                    "Track diabetes readings consistently"
                ),
                disclaimer = "⚠️ MEDICAL DISCLAIMER: This tool is for informational purposes only and should not be used as a substitute for professional medical advice, diagnosis, or treatment. Always consult with a qualified healthcare provider regarding any medical condition or treatment. Do not use this tool to make medical decisions."
            )

            UnitCategory.CURRENCY -> CategoryHelp(
                category = UnitCategory.CURRENCY,
                title = "Currency Converter",
                description = "Convert between major world currencies. Exchange rates can be customized and updated manually.",
                useCases = listOf(
                    "Travel planning and budgeting",
                    "International shopping and e-commerce",
                    "Business transactions",
                    "Comparing prices across countries"
                ),
                tips = listOf(
                    "Exchange rates change daily - update before important conversions",
                    "Actual exchanges may include transaction fees",
                    "Tap 'Edit Rates' to customize exchange rates",
                    "Rates shown are for reference only"
                ),
                examples = listOf(
                    "Budget for European vacation in USD",
                    "Compare product prices across Amazon regions",
                    "Calculate salary in different currencies"
                )
            )

            UnitCategory.SCREEN_PPI -> CategoryHelp(
                category = UnitCategory.SCREEN_PPI,
                title = "Screen PPI Calculator",
                description = "Calculate pixels per inch (PPI) for any display. Higher PPI means sharper, more detailed screens.",
                useCases = listOf(
                    "Comparing display quality between devices",
                    "Choosing monitors or smartphones",
                    "Understanding screen sharpness",
                    "Design and development work"
                ),
                tips = listOf(
                    "300+ PPI: Retina quality (very sharp)",
                    "200-300 PPI: High quality",
                    "100-200 PPI: Standard quality",
                    "Below 100 PPI: Low quality",
                    "Apple's 'Retina' displays are typically 300+ PPI"
                ),
                examples = listOf(
                    "Compare iPhone vs Android display quality",
                    "Calculate if monitor is sharp enough for design work",
                    "Determine if screen is suitable for reading"
                )
            )

            UnitCategory.BMI -> CategoryHelp(
                category = UnitCategory.BMI,
                title = "BMI Calculator",
                description = "Calculate Body Mass Index using height and weight. BMI is a screening tool for weight categories.",
                useCases = listOf(
                    "Health screening and assessment",
                    "Fitness tracking and progress",
                    "Medical evaluations",
                    "Setting weight management goals"
                ),
                tips = listOf(
                    "BMI < 18.5: Underweight",
                    "BMI 18.5-24.9: Normal weight",
                    "BMI 25-29.9: Overweight",
                    "BMI ≥ 30: Obese",
                    "BMI doesn't account for muscle mass or body composition",
                    "Always consult healthcare provider for health decisions"
                ),
                examples = listOf(
                    "Track fitness progress over time",
                    "Set healthy weight goals",
                    "Understand health screening results"
                ),
                disclaimer = "⚠️ HEALTH DISCLAIMER: This BMI calculator is for informational and educational purposes only. BMI is a screening tool and does not diagnose health conditions. It does not account for muscle mass, bone density, overall body composition, or individual health factors. Always consult with a qualified healthcare provider for personalized health advice and before making any health-related decisions."
            )

            UnitCategory.SPEED -> CategoryHelp(
                category = UnitCategory.SPEED,
                title = "Speed Converter",
                description = "Convert velocity and speed between different units.",
                useCases = listOf(
                    "Understanding speed limits in different countries",
                    "Running and cycling pace",
                    "Wind speed and weather",
                    "Vehicle speedometer conversions"
                ),
                tips = listOf(
                    "60 mph ≈ 100 km/h (highway speed)",
                    "1 knot = 1.15 mph (nautical speed)",
                    "Marathon pace: ~5 min/km or ~8 min/mile"
                ),
                examples = listOf(
                    "Convert European speed limits to mph",
                    "Calculate running pace in km/h"
                )
            )

            UnitCategory.DATA_STORAGE -> CategoryHelp(
                category = UnitCategory.DATA_STORAGE,
                title = "Data Storage Converter",
                description = "Convert file sizes and storage capacity between bytes, kilobytes, megabytes, gigabytes, and beyond.",
                useCases = listOf(
                    "Understanding file sizes",
                    "Hard drive and SSD capacity",
                    "Download sizes and internet usage",
                    "Cloud storage planning"
                ),
                tips = listOf(
                    "1 GB = 1,024 MB (binary)",
                    "1 TB = 1,000 GB (decimal, marketing)",
                    "HD movie: ~4-8 GB",
                    "4K movie: ~25-50 GB"
                ),
                examples = listOf(
                    "Calculate how many photos fit on 64 GB phone",
                    "Understand cloud storage limits"
                )
            )

            UnitCategory.DATA_RATE -> CategoryHelp(
                category = UnitCategory.DATA_RATE,
                title = "Data Rate Converter",
                description = "Convert internet and transfer speeds between different units.",
                useCases = listOf(
                    "Understanding internet speed test results",
                    "Comparing ISP plans",
                    "Network performance analysis",
                    "Estimating download times"
                ),
                tips = listOf(
                    "8 Mbps = 1 MB/s (megabits vs megabytes)",
                    "100 Mbps is good for HD streaming",
                    "1 Gbps is excellent for households",
                    "ISPs advertise in Mbps (megabits)"
                ),
                examples = listOf(
                    "Convert 100 Mbps internet to MB/s downloads",
                    "Calculate download time for large files"
                )
            )

            UnitCategory.TYPOGRAPHY -> CategoryHelp(
                category = UnitCategory.TYPOGRAPHY,
                title = "Typography Converter",
                description = "Convert font sizes between pixels, points, ems, and other CSS units.",
                useCases = listOf(
                    "Web design and development",
                    "Graphic design projects",
                    "CSS styling",
                    "Print vs screen design"
                ),
                tips = listOf(
                    "16px is standard body text size",
                    "1em = parent font size",
                    "1rem = root font size (usually 16px)",
                    "72pt = 1 inch in print"
                ),
                examples = listOf(
                    "Convert print design (points) to web (pixels)",
                    "Calculate responsive font sizes"
                )
            )

            UnitCategory.TIME -> CategoryHelp(
                category = UnitCategory.TIME,
                title = "Time Duration Converter",
                description = "Convert time durations between seconds, minutes, hours, days, and more.",
                useCases = listOf(
                    "Project time estimation",
                    "Cooking and baking timers",
                    "Work hour calculations",
                    "Scientific measurements"
                ),
                tips = listOf(
                    "1 hour = 60 minutes = 3,600 seconds",
                    "1 day = 24 hours",
                    "1 week = 7 days = 168 hours"
                ),
                examples = listOf(
                    "Convert project deadline from days to hours",
                    "Calculate total work hours in a month"
                )
            )

            UnitCategory.POWER -> CategoryHelp(
                category = UnitCategory.POWER,
                title = "Power Converter",
                description = "Convert electrical power and engine power between watts, horsepower, and other units.",
                useCases = listOf(
                    "Understanding appliance power consumption",
                    "Comparing engine specifications",
                    "Solar panel output",
                    "Electrical engineering"
                ),
                tips = listOf(
                    "1 HP ≈ 746 watts",
                    "1 kW = 1,000 watts",
                    "Typical microwave: 1,000W",
                    "Car engine: 100-400 HP"
                ),
                examples = listOf(
                    "Convert car horsepower to kilowatts",
                    "Calculate appliance energy usage"
                )
            )

            UnitCategory.ENERGY -> CategoryHelp(
                category = UnitCategory.ENERGY,
                title = "Energy Converter",
                description = "Convert energy between joules, calories, kilowatt-hours, and other units.",
                useCases = listOf(
                    "Nutrition and food energy",
                    "Electricity consumption",
                    "Physics calculations",
                    "Battery capacity"
                ),
                tips = listOf(
                    "1 food calorie (Cal) = 1,000 calories (cal)",
                    "1 kWh = 3,600,000 joules",
                    "Average daily intake: 2,000 Cal"
                ),
                examples = listOf(
                    "Convert food calories to joules",
                    "Calculate electricity bill from kWh"
                )
            )

            UnitCategory.PRESSURE -> CategoryHelp(
                category = UnitCategory.PRESSURE,
                title = "Pressure Converter",
                description = "Convert pressure measurements for tires, weather, and scientific use.",
                useCases = listOf(
                    "Tire pressure checking",
                    "Weather barometric pressure",
                    "Scuba diving calculations",
                    "Engineering applications"
                ),
                tips = listOf(
                    "Car tires: typically 32-35 PSI",
                    "Standard atmosphere: 1 atm = 14.7 PSI",
                    "Weather: 1013 mbar is standard"
                ),
                examples = listOf(
                    "Convert tire pressure from bar to PSI",
                    "Understand weather pressure readings"
                )
            )

            UnitCategory.ANGLE -> CategoryHelp(
                category = UnitCategory.ANGLE,
                title = "Angle Converter",
                description = "Convert angular measurements between degrees, radians, and other units.",
                useCases = listOf(
                    "Navigation and compass bearings",
                    "Geometry and trigonometry",
                    "Astronomy calculations",
                    "Engineering and CAD"
                ),
                tips = listOf(
                    "Full circle: 360° = 2π radians",
                    "Right angle: 90° = π/2 radians",
                    "π ≈ 3.14159"
                ),
                examples = listOf(
                    "Convert compass bearing to radians",
                    "Calculate angles for construction"
                )
            )

            UnitCategory.FUEL -> CategoryHelp(
                category = UnitCategory.FUEL,
                title = "Fuel Efficiency Converter",
                description = "Convert fuel economy between km/L, MPG (US), and MPG (UK).",
                useCases = listOf(
                    "Comparing car fuel efficiency",
                    "Trip fuel cost estimation",
                    "Understanding vehicle specs",
                    "Environmental impact calculation"
                ),
                tips = listOf(
                    "Higher numbers = better efficiency",
                    "US gallon ≠ UK gallon (US is smaller)",
                    "Average car: 25-30 MPG (US)"
                ),
                examples = listOf(
                    "Convert European car specs to US MPG",
                    "Calculate fuel costs for road trip"
                )
            )

            UnitCategory.TIME_AGE -> CategoryHelp(
                category = UnitCategory.TIME_AGE,
                title = "Age Calculator",
                description = "Calculate exact age in years, months, and days from birthdate.",
                useCases = listOf(
                    "Calculating exact age for documents",
                    "Birthday countdowns",
                    "Age milestones tracking",
                    "Legal age verification"
                ),
                tips = listOf(
                    "Accounts for leap years",
                    "Shows age in multiple formats",
                    "Useful for official forms"
                ),
                examples = listOf(
                    "Calculate age for passport application",
                    "Find exact age in days for milestone celebration"
                )
            )

            UnitCategory.TIME_ZONES -> CategoryHelp(
                category = UnitCategory.TIME_ZONES,
                title = "Time Zone Converter",
                description = "Convert times between different time zones around the world.",
                useCases = listOf(
                    "Scheduling international calls",
                    "Travel planning",
                    "Remote work coordination",
                    "Event timing across countries"
                ),
                tips = listOf(
                    "Remember daylight saving time changes",
                    "UTC is the universal reference",
                    "Some countries don't use DST"
                ),
                examples = listOf(
                    "Schedule meeting across US and Europe",
                    "Convert flight arrival time to local time"
                )
            )

            UnitCategory.DIGITAL_IMAGE -> CategoryHelp(
                category = UnitCategory.DIGITAL_IMAGE,
                title = "Digital Image Converter",
                description = "Convert image dimensions between pixels and physical units using DPI.",
                useCases = listOf(
                    "Photo printing size calculation",
                    "Web vs print design",
                    "Image resolution planning",
                    "Display size estimation"
                ),
                tips = listOf(
                    "72 DPI: standard for web",
                    "300 DPI: standard for print",
                    "Higher DPI = better print quality"
                ),
                examples = listOf(
                    "Calculate print size for 3000×2000 pixel photo",
                    "Determine image resolution needed for poster"
                )
            )

            UnitCategory.TORQUE -> CategoryHelp(
                category = UnitCategory.TORQUE,
                title = "Torque Converter",
                description = "Convert rotational force between newton-meters, pound-force feet, and other torque units.",
                useCases = listOf(
                    "Automotive specifications and repairs",
                    "Tightening bolts to proper specifications",
                    "Engine and motor comparisons",
                    "Mechanical engineering calculations"
                ),
                tips = listOf(
                    "1 N·m ≈ 0.74 lbf·ft",
                    "Car wheel nuts: typically 80-120 N·m",
                    "Torque = Force × Distance from pivot",
                    "Critical for proper bolt tightening"
                ),
                examples = listOf(
                    "Convert car manual torque specs from N·m to lbf·ft",
                    "Calculate proper wrench force needed"
                )
            )

            UnitCategory.ACCELERATION -> CategoryHelp(
                category = UnitCategory.ACCELERATION,
                title = "Acceleration Converter",
                description = "Convert acceleration rates between m/s², ft/s², and standard gravity (g).",
                useCases = listOf(
                    "Physics and motion calculations",
                    "Vehicle performance specifications",
                    "Roller coaster and ride design",
                    "Aerospace engineering"
                ),
                tips = listOf(
                    "1 g = 9.81 m/s² (Earth's gravity)",
                    "Car acceleration: 0-60 mph in 5s ≈ 5.4 m/s²",
                    "Astronauts experience 3-4 g during launch",
                    "Acceleration = Change in velocity / Time"
                ),
                examples = listOf(
                    "Convert car 0-60 time to g-force",
                    "Calculate roller coaster g-forces"
                )
            )

            UnitCategory.DENSITY -> CategoryHelp(
                category = UnitCategory.DENSITY,
                title = "Density Converter",
                description = "Convert density measurements for materials and substances.",
                useCases = listOf(
                    "Material science and engineering",
                    "Chemistry calculations",
                    "Determining if objects float or sink",
                    "Quality control in manufacturing"
                ),
                tips = listOf(
                    "Water density: 1,000 kg/m³ or 1 g/cm³",
                    "Objects denser than water sink",
                    "Density = Mass / Volume",
                    "Gold: 19,300 kg/m³, Air: 1.2 kg/m³"
                ),
                examples = listOf(
                    "Determine if material will float in water",
                    "Calculate material weight from volume"
                )
            )

            UnitCategory.FORCE -> CategoryHelp(
                category = UnitCategory.FORCE,
                title = "Force Converter",
                description = "Convert force measurements between newtons, pounds-force, and other units.",
                useCases = listOf(
                    "Physics and engineering calculations",
                    "Structural load analysis",
                    "Weight vs mass understanding",
                    "Mechanical system design"
                ),
                tips = listOf(
                    "1 N = force to accelerate 1 kg at 1 m/s²",
                    "1 lbf ≈ 4.45 N",
                    "Weight is a force (mass × gravity)",
                    "Force = Mass × Acceleration"
                ),
                examples = listOf(
                    "Convert weight force from pounds to newtons",
                    "Calculate force needed to lift object"
                )
            )

            UnitCategory.ILLUMINANCE -> CategoryHelp(
                category = UnitCategory.ILLUMINANCE,
                title = "Illuminance Converter",
                description = "Convert light intensity measurements for lighting design and photography.",
                useCases = listOf(
                    "Photography and videography",
                    "Lighting design for buildings",
                    "Workplace lighting standards",
                    "Plant growing light requirements"
                ),
                tips = listOf(
                    "Office lighting: 300-500 lux",
                    "Direct sunlight: ~100,000 lux",
                    "Reading: minimum 300 lux recommended",
                    "1 lux = 1 lumen per square meter"
                ),
                examples = listOf(
                    "Determine if room lighting is adequate for reading",
                    "Calculate lighting needs for photography studio"
                )
            )

            UnitCategory.FREQUENCY -> CategoryHelp(
                category = UnitCategory.FREQUENCY,
                title = "Frequency Converter",
                description = "Convert frequency between hertz, kilohertz, megahertz, and gigahertz.",
                useCases = listOf(
                    "Radio and wireless communications",
                    "Audio and sound engineering",
                    "Computer processor speeds",
                    "Electrical power systems"
                ),
                tips = listOf(
                    "Human hearing: 20 Hz to 20 kHz",
                    "FM radio: 88-108 MHz",
                    "WiFi: 2.4 GHz or 5 GHz",
                    "1 Hz = 1 cycle per second"
                ),
                examples = listOf(
                    "Convert radio station frequency to Hz",
                    "Compare processor speeds in GHz"
                )
            )

            UnitCategory.PAPER_SIZE -> CategoryHelp(
                category = UnitCategory.PAPER_SIZE,
                title = "Paper Size Converter",
                description = "Compare and convert between international paper sizes (A-series, B-series, Letter, Legal, etc.).",
                useCases = listOf(
                    "Printing and document preparation",
                    "International document standards",
                    "Office supplies ordering",
                    "Graphic design and layout"
                ),
                tips = listOf(
                    "A4 is international standard (210×297 mm)",
                    "Letter is US standard (8.5×11 in)",
                    "Each A-size is half the area of previous",
                    "A4 ≈ Letter but slightly narrower and taller"
                ),
                examples = listOf(
                    "Determine if US Letter fits in A4 envelope",
                    "Convert design from A4 to Letter size"
                )
            )

            UnitCategory.MATH_HELPER -> CategoryHelp(
                category = UnitCategory.MATH_HELPER,
                title = "Math Helper",
                description = "Convert between percentages, decimals, and fractions for quick calculations.",
                useCases = listOf(
                    "Calculating discounts and sales",
                    "Grade and test score conversions",
                    "Financial calculations",
                    "Recipe scaling and proportions"
                ),
                tips = listOf(
                    "50% = 0.5 = 1/2",
                    "To convert decimal to %: multiply by 100",
                    "To convert % to decimal: divide by 100",
                    "25% = 0.25 = 1/4"
                ),
                examples = listOf(
                    "Calculate 15% tip on restaurant bill",
                    "Convert test score 17/20 to percentage"
                )
            )

            UnitCategory.TIME_DATE_CALC -> CategoryHelp(
                category = UnitCategory.TIME_DATE_CALC,
                title = "Date Calculator",
                description = "Calculate dates by adding or subtracting days, weeks, months, or years.",
                useCases = listOf(
                    "Project deadline planning",
                    "Event date calculation",
                    "Pregnancy due date estimation",
                    "Contract expiration dates"
                ),
                tips = listOf(
                    "Accounts for leap years automatically",
                    "Useful for 90-day, 6-month calculations",
                    "Can add or subtract time periods",
                    "Handles month-end dates correctly"
                ),
                examples = listOf(
                    "Calculate date 90 days from today",
                    "Find date 6 months before contract end"
                )
            )

            UnitCategory.TIME_DIFFERENCE -> CategoryHelp(
                category = UnitCategory.TIME_DIFFERENCE,
                title = "Time Difference Calculator",
                description = "Calculate the exact time difference between two dates or times.",
                useCases = listOf(
                    "Project duration tracking",
                    "Age calculation between dates",
                    "Event countdown timers",
                    "Work hours and timesheet calculations"
                ),
                tips = listOf(
                    "Shows difference in multiple formats",
                    "Accounts for leap years and DST",
                    "Useful for time tracking",
                    "Can calculate past or future differences"
                ),
                examples = listOf(
                    "Calculate days until vacation",
                    "Find hours worked between clock-in and clock-out"
                )
            )

            UnitCategory.TIME_TIMESTAMP -> CategoryHelp(
                category = UnitCategory.TIME_TIMESTAMP,
                title = "Timestamp Converter",
                description = "Convert between Unix timestamps and human-readable dates/times.",
                useCases = listOf(
                    "Programming and software development",
                    "Database timestamp interpretation",
                    "Log file analysis",
                    "API response debugging"
                ),
                tips = listOf(
                    "Unix time = seconds since Jan 1, 1970",
                    "Timestamp 0 = 1970-01-01 00:00:00 UTC",
                    "Used widely in databases and APIs",
                    "Always in UTC, convert to local time"
                ),
                examples = listOf(
                    "Convert database timestamp to readable date",
                    "Debug API response with Unix time"
                )
            )

            UnitCategory.TIME_BIZ_DAYS -> CategoryHelp(
                category = UnitCategory.TIME_BIZ_DAYS,
                title = "Business Days Calculator",
                description = "Calculate business days (excluding weekends and optionally holidays) between dates.",
                useCases = listOf(
                    "Project timeline planning",
                    "Delivery date estimation",
                    "SLA and contract calculations",
                    "Work schedule planning"
                ),
                tips = listOf(
                    "Excludes Saturdays and Sundays",
                    "Useful for shipping estimates",
                    "Standard business week = 5 days",
                    "Important for contract deadlines"
                ),
                examples = listOf(
                    "Calculate 5 business days for shipping",
                    "Find project completion date in work days"
                )
            )

            // Add more categories with detailed help...
            else -> CategoryHelp(
                category = category,
                title = category.title,
                description = "Convert between different ${category.title.lowercase()} units.",
                useCases = listOf("General ${category.title.lowercase()} conversions"),
                tips = emptyList(),
                examples = emptyList()
            )
        }
    }

    /**
     * Get all help content
     */
    fun getAllHelp(): Map<UnitCategory, CategoryHelp> {
        return UnitCategory.values()
            .filter { it != UnitCategory.MORE }
            .associateWith { getHelpForCategory(it) }
    }
}
