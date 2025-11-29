package com.moshitech.workmate.feature.deviceinfo.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.moshitech.workmate.feature.deviceinfo.utils.SensorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    navController: NavController,
    sensorType: Int
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { 
        sensorManager.getDefaultSensor(sensorType) 
            ?: sensorManager.getSensorList(Sensor.TYPE_ALL).find { it.type == sensorType }
    }

    if (sensor == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sensor not found", color = textColor)
        }
        return
    }

    // Sensor data state
    var sensorValues by remember { mutableStateOf(floatArrayOf()) }
    var accuracy by remember { mutableIntStateOf(-1) } // -1 for initial state
    
    // History for graph
    val historySize = 100
    val sensorHistory = remember { mutableStateListOf<FloatArray>() }

    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val values = it.values.clone()
                    sensorValues = values
                    accuracy = it.accuracy
                    
                    sensorHistory.add(values)
                    if (sensorHistory.size > historySize) {
                        sensorHistory.removeAt(0)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                accuracy = newAccuracy
            }
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(sensor.name, fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Data Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Accuracy Badge
                        val accuracyText = when(accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High Accuracy"
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium Accuracy"
                            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low Accuracy"
                            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
                            -1 -> "Waiting..."
                            else -> "Unknown"
                        }
                        val accuracyColor = when(accuracy) {
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF4CAF50)
                            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFFFC107)
                            -1 -> Color.Gray
                            else -> Color(0xFFEF5350)
                        }
                        
                        if (accuracy != -1 || sensorValues.isEmpty()) { // Show waiting if no data, or actual status
                             Surface(
                                color = accuracyColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = accuracyText,
                                    color = accuracyColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (sensorValues.isNotEmpty()) {
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
                        
                        SensorGraph(
                            history = sensorHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            textColor = textColor
                        )
                    } else {
                         Text("Waiting for data...", color = subtitleColor)
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
                        text = "Sensor Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SensorSpecRow("Name", sensor.name, textColor, subtitleColor)
                    SensorSpecRow("Vendor", sensor.vendor, textColor, subtitleColor)
                    SensorSpecRow("Type", SensorUtils.getSensorCategory(sensor.type), textColor, subtitleColor)
                    SensorSpecRow("Version", sensor.version.toString(), textColor, subtitleColor)
                    SensorSpecRow("Power", "${sensor.power} mA", textColor, subtitleColor)
                    SensorSpecRow("Resolution", "${sensor.resolution}", textColor, subtitleColor)
                    SensorSpecRow("Max Range", "${sensor.maximumRange}", textColor, subtitleColor)
                    SensorSpecRow("Min Delay", "${sensor.minDelay} μs", textColor, subtitleColor)
                    SensorSpecRow("FIFO Reserved", "${sensor.fifoReservedEventCount}", textColor, subtitleColor)
                    SensorSpecRow("FIFO Max", "${sensor.fifoMaxEventCount}", textColor, subtitleColor)
                    SensorSpecRow("Wake-up", if (sensor.isWakeUpSensor) "Yes" else "No", textColor, subtitleColor)
                    SensorSpecRow("Dynamic", if (sensor.isDynamicSensor) "Yes" else "No", textColor, subtitleColor)
                }
            }
        }
    }
}

@Composable
private fun SensorGraph(
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
            color = textColor.copy(alpha = 0.1f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
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
