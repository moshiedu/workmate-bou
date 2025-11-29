package com.moshitech.workmate.feature.deviceinfo.tabs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.data.models.SensorInfo
import com.moshitech.workmate.feature.deviceinfo.utils.SensorUtils

@Composable
fun SensorsTab(
    navController: NavController,
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val hardwareInfo by viewModel.hardwareInfoEnhanced.collectAsState()
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    // Group sensors by category
    val groupedSensors = remember(hardwareInfo.sensors) {
        hardwareInfo.sensors.groupBy { SensorUtils.getSensorCategory(it.type) }
    }
    
    // Order of categories
    val categories = listOf("Motion", "Position", "Environment", "Other")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Sensors (${hardwareInfo.sensors.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        categories.forEach { category ->
            val sensors = groupedSensors[category]
            if (!sensors.isNullOrEmpty()) {
                item {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subtitleColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                items(sensors) { sensor ->
                    SensorItem(
                        sensor = sensor,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        onClick = { 
                            navController.navigate("sensor_detail/${sensor.type}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SensorItem(
    sensor: SensorInfo,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = SensorUtils.getSensorIcon(sensor.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sensor.vendor,
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = subtitleColor
            )
        }
    }
}

@Composable
fun SensorDetailView(
    sensor: SensorInfo,
    onBack: () -> Unit,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val androidSensor = remember { 
        sensorManager.getSensorList(Sensor.TYPE_ALL).find { 
            it.name == sensor.name && it.vendor == sensor.vendor && it.type == sensor.type 
        } 
    }
    
    // Sensor data state
    var sensorValues by remember { mutableStateOf(floatArrayOf()) }
    // History for graph (list of float arrays)
    val historySize = 100
    val sensorHistory = remember { mutableStateListOf<FloatArray>() }
    
    DisposableEffect(androidSensor) {
        if (androidSensor == null) return@DisposableEffect onDispose { }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val values = it.values.clone()
                    sensorValues = values
                    
                    sensorHistory.add(values)
                    if (sensorHistory.size > historySize) {
                        sensorHistory.removeAt(0)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy changes if needed
            }
        }
        
        sensorManager.registerListener(listener, androidSensor, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = sensor.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        
        // Live Data Card
        if (sensorValues.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Data",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Values
                    sensorValues.forEachIndexed { index, value ->
                        val label = when(index) {
                            0 -> "X"
                            1 -> "Y"
                            2 -> "Z"
                            3 -> "Cos"
                            4 -> "Accuracy"
                            else -> "Value ${index + 1}"
                        }
                        
                        val color = when(index) {
                            0 -> Color(0xFF5C6BC0) // Blue
                            1 -> Color(0xFFEF5350) // Red
                            2 -> Color(0xFF66BB6A) // Green
                            else -> Color.Gray
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$label: ${String.format("%.3f", value)}",
                                color = textColor,
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Graph
                    SensorGraph(
                        history = sensorHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textColor = textColor
                    )
                }
            }
        }
        
        // Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                SensorSpecRow("Manufacturer", sensor.vendor, textColor, subtitleColor)
                SensorSpecRow("Model", sensor.name, textColor, subtitleColor) // Using name as model based on typical Android mapping
                SensorSpecRow("Type", sensor.typeString, textColor, subtitleColor)
                SensorSpecRow("Version", sensor.version.toString(), textColor, subtitleColor)
                SensorSpecRow("Resolution", "${sensor.resolution}", textColor, subtitleColor)
                SensorSpecRow("Maximum range", "${sensor.maxRange}", textColor, subtitleColor)
                SensorSpecRow("Power", "${sensor.power} mA", textColor, subtitleColor)
                SensorSpecRow("Reporting mode", sensor.reportingMode, textColor, subtitleColor)
                SensorSpecRow("Wakeup sensor", if (sensor.isWakeUpSensor) "Yes" else "No", textColor, subtitleColor)
                SensorSpecRow("Dynamic sensor", if (sensor.isDynamicSensor) "Yes" else "No", textColor, subtitleColor)
            }
        }
    }
}

@Composable
fun SensorGraph(
    history: List<FloatArray>,
    modifier: Modifier = Modifier,
    textColor: Color
) {
    if (history.isEmpty()) return
    
    val valueCount = history.first().size
    val colors = listOf(
        Color(0xFF5C6BC0), // Blue
        Color(0xFFEF5350), // Red
        Color(0xFF66BB6A), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFFAB47BC), // Purple
        Color(0xFF26C6DA)  // Cyan
    )
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Draw grid lines
        drawLine(
            color = textColor.copy(alpha = 0.2f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = 1.dp.toPx()
        )
        
        drawLine(
            color = textColor.copy(alpha = 0.2f),
            start = Offset(0f, 0f),
            end = Offset(width, 0f),
            strokeWidth = 1.dp.toPx()
        )
        
        drawLine(
            color = textColor.copy(alpha = 0.2f),
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 1.dp.toPx()
        )
        
        // Find min and max for scaling
        var minVal = Float.MAX_VALUE
        var maxVal = Float.MIN_VALUE
        
        history.forEach { values ->
            values.forEach { value ->
                minVal = minOf(minVal, value)
                maxVal = maxOf(maxVal, value)
            }
        }
        
        // Add some padding to range
        val range = maxVal - minVal
        val paddedMin = minVal - (range * 0.1f)
        val paddedMax = maxVal + (range * 0.1f)
        val paddedRange = paddedMax - paddedMin
        
        if (paddedRange == 0f) return@Canvas
        
        // Draw paths for each value index
        for (i in 0 until minOf(valueCount, colors.size)) {
            val path = Path()
            val stepX = width / (history.size - 1).coerceAtLeast(1)
            
            history.forEachIndexed { index, values ->
                if (i < values.size) {
                    val x = index * stepX
                    val y = height - ((values[i] - paddedMin) / paddedRange * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
            }
            
            drawPath(
                path = path,
                color = colors[i],
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun SensorSpecRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = subtitleColor,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
