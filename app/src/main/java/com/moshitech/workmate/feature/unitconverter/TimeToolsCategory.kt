package com.moshitech.workmate.feature.unitconverter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class TimeToolsCategory(
    val title: String,
    val icon: ImageVector
) {
    TIME_CONVERTER("Converter", Icons.Default.AccessTime),
    TIME_DATE_CALC("Date Calc", Icons.Default.DateRange),
    TIME_DIFFERENCE("Difference", Icons.Default.History),
    TIME_TIMESTAMP("Timestamp", Icons.Default.Schedule),
    TIME_ZONES("Zones", Icons.Default.Public),
    TIME_BIZ_DAYS("Biz Days", Icons.Default.Calculate),
    TIME_AGE("Age", Icons.Default.Timer)
}
