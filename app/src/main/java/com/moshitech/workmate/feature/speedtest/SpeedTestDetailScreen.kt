package com.moshitech.workmate.feature.speedtest

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
fun SpeedTestDetailScreen(
    navController: NavController,
    testId: Long,
    viewModel: SpeedTestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    
    // Find the test result
    val result = state.history.find { it.id == testId }
    
    if (result == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Test result not found", color = textColor)
        }
        return
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Test Details", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = """
                            🚀 Workmate Speed Test Results:
                            
                            📅 Date: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(result.timestamp))}
                            
                            ⬇️ Download: ${"%.1f".format(result.downloadSpeed)} Mbps
                            ⬆️ Upload: ${"%.1f".format(result.uploadSpeed)} Mbps
                            📶 Ping: ${result.ping} ms
                            〰️ Jitter: ${result.jitter} ms
                            📉 Packet Loss: ${"%.1f".format(result.packetLoss)}%
                            
                            🌐 Network: ${result.networkType}
                            📡 ISP: ${result.isp}
                            
                            Tested with Workmate App
                        """.trimIndent()
                        
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Speed Test Results")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Test Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(result.timestamp)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
            
            // Speed Results Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Speed Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow("Download Speed", "${"%.2f".format(result.downloadSpeed)} Mbps", Color(0xFF29B6F6), textColor)
                    DetailRow("Upload Speed", "${"%.2f".format(result.uploadSpeed)} Mbps", Color(0xFF66BB6A), textColor)
                    DetailRow("Ping", "${result.ping} ms", Color(0xFFFFA726), textColor)
                    DetailRow("Jitter", "${result.jitter} ms", Color(0xFFAB47BC), textColor)
                    DetailRow("Packet Loss", "${"%.2f".format(result.packetLoss)}%", Color(0xFFEF5350), textColor)
                }
            }
            
            // Network Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Network Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    InfoRow("Network Type", result.networkType, textColor)
                    InfoRow("ISP", result.isp, textColor)
                    InfoRow("IP Address", result.ipAddress, textColor)
                }
            }
            
            // Trend Chart
            if (state.history.size > 1) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Speed Trend",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Last 10 tests",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SpeedTrendChart(
                            history = state.history.sortedByDescending { it.timestamp }.take(10).reversed(),
                            currentResult = result,
                            isDark = isDark,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = color)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f)
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun SpeedTrendChart(
    history: List<SpeedTestResult>,
    currentResult: SpeedTestResult,
    isDark: Boolean,
    textColor: Color
) {
    if (history.isEmpty()) return
    
    val downloadColor = Color(0xFF29B6F6)
    val uploadColor = Color(0xFF66BB6A)
    
    Column {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = downloadColor)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = uploadColor)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 40f
            
            val maxDownload = history.maxOfOrNull { it.downloadSpeed } ?: 100f
            val maxUpload = history.maxOfOrNull { it.uploadSpeed } ?: 100f
            val maxSpeed = maxOf(maxDownload, maxUpload)
            
            val stepX = (width - padding * 2) / (history.size - 1).coerceAtLeast(1)
            
            // Draw grid lines
            val gridColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
            for (i in 0..4) {
                val y = padding + (height - padding * 2) * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }
            
            // Draw download line
            if (history.size > 1) {
                val downloadPath = Path()
                history.forEachIndexed { index, result ->
                    val x = padding + stepX * index
                    val y = height - padding - (result.downloadSpeed / maxSpeed) * (height - padding * 2)
                    if (index == 0) {
                        downloadPath.moveTo(x, y)
                    } else {
                        downloadPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = downloadPath,
                    color = downloadColor,
                    style = Stroke(width = 3f)
                )
            }
            
            // Draw upload line
            if (history.size > 1) {
                val uploadPath = Path()
                history.forEachIndexed { index, result ->
                    val x = padding + stepX * index
                    val y = height - padding - (result.uploadSpeed / maxSpeed) * (height - padding * 2)
                    if (index == 0) {
                        uploadPath.moveTo(x, y)
                    } else {
                        uploadPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = uploadPath,
                    color = uploadColor,
                    style = Stroke(width = 3f)
                )
            }
            
            // Draw points
            history.forEachIndexed { index, result ->
                val x = padding + stepX * index
                val yDownload = height - padding - (result.downloadSpeed / maxSpeed) * (height - padding * 2)
                val yUpload = height - padding - (result.uploadSpeed / maxSpeed) * (height - padding * 2)
                
                drawCircle(
                    color = downloadColor,
                    radius = 4f,
                    center = Offset(x, yDownload)
                )
                drawCircle(
                    color = uploadColor,
                    radius = 4f,
                    center = Offset(x, yUpload)
                )
            }
        }
    }
}
