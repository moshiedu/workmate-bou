package com.moshitech.workmate.feature.speedtest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    navController: NavController,
    viewModel: SpeedTestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark Blue/Slate
            Color(0xFF1E293B)  // Lighter Slate
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Internet Speed Test", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Network Info Card
                NetworkInfoCard(state.networkInfo)

                // Speed Gauge
                SpeedGauge(
                    currentSpeed = if (state.currentStage == TestStage.UPLOAD) state.uploadSpeed else state.downloadSpeed,
                    isTesting = state.isTesting,
                    statusText = state.currentSpeedDisplay,
                    stage = state.currentStage
                )

                // Error Message
                if (state.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = state.error ?: "", color = Color.White)
                        }
                    }
                }

                // Results Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultItem(
                        icon = Icons.Default.Speed,
                        label = "Ping",
                        value = if (state.ping > 0) "${state.ping}" else "--",
                        unit = "ms",
                        color = Color(0xFFFFA726) // Orange
                    )
                    ResultItem(
                        icon = Icons.Default.Download,
                        label = "Download",
                        value = if (state.downloadSpeed > 0) "%.1f".format(state.downloadSpeed) else "--",
                        unit = "Mbps",
                        color = Color(0xFF29B6F6) // Light Blue
                    )
                    ResultItem(
                        icon = Icons.Default.Upload,
                        label = "Upload",
                        value = if (state.uploadSpeed > 0) "%.1f".format(state.uploadSpeed) else "--",
                        unit = "Mbps",
                        color = Color(0xFF66BB6A) // Green
                    )
                }

                // Start Button
                Button(
                    onClick = { viewModel.startTest() },
                    enabled = !state.isTesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6), // Vibrant Blue
                        disabledContainerColor = Color(0xFF3B82F6).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (state.isTesting) "Testing..." else "Start Test",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // History Graph
                if (state.history.isNotEmpty()) {
                    HistoryGraph(state.history)
                }
            }
        }
    }
}

@Composable
fun NetworkInfoCard(info: NetworkInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = "Network", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = info.type.ifEmpty { "Unknown" }, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = "IP", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = info.ip.ifEmpty { "--" }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "ISP", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(text = info.isp.ifEmpty { "--" }, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryGraph(history: List<SpeedTestResult>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("History (Last 10 Tests)", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val data = history.take(10).reversed() // Show last 10, oldest to newest
                    
                    if (data.isEmpty()) return@Canvas

                    val maxSpeed = data.maxOf { it.downloadSpeed }.coerceAtLeast(10f)
                    val points = data.mapIndexed { index, result ->
                        val x = (width / (data.size - 1).coerceAtLeast(1)) * index
                        val y = height - (result.downloadSpeed / maxSpeed * height)
                        Offset(x, y)
                    }

                    // Draw Path
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF29B6F6),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Points
                    points.forEach { point ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedGauge(
    currentSpeed: Float,
    isTesting: Boolean,
    statusText: String,
    stage: TestStage
) {
    val maxSpeed = 100f // Max speed for gauge in Mbps
    val progress = (currentSpeed / maxSpeed).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    val gaugeColor = when (stage) {
        TestStage.DOWNLOAD -> Color(0xFF29B6F6)
        TestStage.UPLOAD -> Color(0xFF66BB6A)
        else -> Color(0xFF3B82F6)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
            Canvas(modifier = Modifier.size(220.dp)) {
                // Background Arc
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )

                // Progress Arc
                drawArc(
                    color = if (isTesting) gaugeColor else Color.Gray.copy(alpha = 0.5f),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(currentSpeed),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun ResultItem(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String = "",
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
    }
}
