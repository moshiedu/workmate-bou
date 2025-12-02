package com.moshitech.workmate.feature.speedtest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import java.util.*

import androidx.navigation.NavController
import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import com.moshitech.workmate.MainViewModel
import com.moshitech.workmate.data.repository.AppTheme
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    navController: NavController,
    viewModel: SpeedTestViewModel,
    mainViewModel: MainViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val themeState by mainViewModel.theme.collectAsState()

    val isDark = when (themeState) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    // Theme-aware colors
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White

    // Gradient Background
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF1F5F9),
                Color(0xFFE2E8F0)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Internet Speed Test", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("speed_test_history") }) {
                        Icon(Icons.Default.History, contentDescription = "View History", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (state.currentStage == TestStage.FINISHED) {
                FloatingActionButton(
                    onClick = {
                        val shareText = """
                            🚀 Workmate Speed Test Results:
                            
                            ⬇️ Download: ${"%.1f".format(state.downloadSpeed)} Mbps
                            ⬆️ Upload: ${"%.1f".format(state.uploadSpeed)} Mbps
                            📶 Ping: ${state.ping} ms
                            〰️ Jitter: ${state.jitter} ms
                            📉 Packet Loss: ${"%.1f".format(state.packetLoss)}%
                            
                            Tested with Workmate App
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Speed Test Results")
                        context.startActivity(shareIntent)
                    },
                    containerColor = if (isDark) Color(0xFF7C3AED) else Color(0xFF06B6D4), // Purple or Cyan
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Results")
                }
            }
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
                verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced from 24dp
            ) {
                // Compact Settings Row
                if (!state.isTesting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Server Selection (Compact)
                        CompactServerCard(
                            modifier = Modifier.weight(1f),
                            servers = state.servers,
                            selectedServer = state.selectedServer,
                            onServerSelected = { viewModel.selectServer(it) },
                            isDark = isDark,
                            textColor = textColor,
                            cardBg = cardBg
                        )

                        // Schedule Test (Compact)
                        CompactScheduleCard(
                            modifier = Modifier.weight(1f),
                            isScheduled = state.isScheduled,
                            intervalHours = state.scheduleInterval,
                            onToggleSchedule = { enable, interval -> viewModel.toggleSchedule(enable, interval) },
                            isDark = isDark,
                            textColor = textColor,
                            cardBg = cardBg
                        )
                    }
                }

                // Network Info Card
                NetworkInfoCard(state.networkInfo, isDark = isDark, textColor = textColor, cardBg = cardBg)

                // Speed Gauge
                SpeedGauge(
                    progress = if (state.currentStage == TestStage.DOWNLOAD) state.downloadProgress else state.uploadProgress,
                    downloadSpeed = state.downloadSpeed,
                    uploadSpeed = state.uploadSpeed,
                    currentStage = state.currentStage,
                    isDark = isDark,
                    textColor = textColor
                )

                // Error Message
                if (state.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = state.error ?: "", color = if (isDark) Color.White else Color(0xFF991B1B))
                        }
                    }
                }

                // Results Grid (Summary Cards)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryResultCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            label = "Ping",
                            value = if (state.ping > 0) "${state.ping}" else "--",
                            unit = "ms",
                            iconColor = Color(0xFFFFA726), // Orange
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor
                        )
                        SummaryResultCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.GraphicEq,
                            label = "Jitter",
                            value = if (state.jitter > 0) "${state.jitter}" else "--",
                            unit = "ms",
                            iconColor = Color(0xFFAB47BC), // Purple
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryResultCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Download,
                            label = "Download",
                            value = if (state.downloadSpeed > 0) "%.1f".format(state.downloadSpeed) else "--",
                            unit = "Mbps",
                            iconColor = Color(0xFF29B6F6), // Blue
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor
                        )
                        SummaryResultCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Upload,
                            label = "Upload",
                            value = if (state.uploadSpeed > 0) "%.1f".format(state.uploadSpeed) else "--",
                            unit = "Mbps",
                            iconColor = Color(0xFF66BB6A), // Green
                            isDark = isDark,
                            cardBg = cardBg,
                            textColor = textColor
                        )
                    }
                }

                // Start Button
                StartButton(
                    onClick = { viewModel.startTest() },
                    enabled = !state.isTesting && state.isNetworkAvailable,
                    isTesting = state.isTesting,
                    isNetworkAvailable = state.isNetworkAvailable,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StartButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isTesting: Boolean,
    isNetworkAvailable: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Colors based on theme and state
    val buttonColors = when {
        !enabled -> listOf(Color.Gray, Color.Gray)
        isDark -> listOf(Color(0xFF4C1D95), Color(0xFF3B82F6)) // Deep Purple to Neon Blue
        else -> listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)) // Cyan to Blue
    }

    val glowColor = if (isDark) Color(0xFF818CF8) else Color(0xFF67E8F9) // Indigo/Cyan glow

    Box(
        modifier = modifier
            .height(64.dp) // Slightly taller for the glow effect
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(buttonColors))
            .clickable(enabled = enabled, onClick = onClick)
            .then(
                if (isTesting) Modifier.scale(pulseAlpha) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bottom Glow Effect
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                glowColor.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isTesting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "TESTING...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            } else {
                Text(
                    text = if (isNetworkAvailable) "START TEST" else "NO CONNECTION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = if (enabled) 1f else 0.7f)
                )
            }
        }
    }
}

@Composable
fun DetailedHistoryList(
    history: List<SpeedTestResult>,
    isDark: Boolean = true,
    textColor: Color = Color.White,
    cardBg: Color = Color.White.copy(alpha = 0.05f)
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = "History", tint = textColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("History", color = textColor, fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = {
                    val csvHeader = "Timestamp,Download (Mbps),Upload (Mbps),Ping (ms),Jitter (ms),Packet Loss (%)\n"
                    val csvBody = history.sortedByDescending { it.timestamp }.joinToString("\n") { result ->
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                            Date(result.timestamp)
                        )
                        "$date,${result.downloadSpeed},${result.uploadSpeed},${result.ping},${result.jitter},${result.packetLoss}"
                    }
                    val csvContent = csvHeader + csvBody
                    
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csvContent)
                        type = "text/csv"
                        putExtra(Intent.EXTRA_TITLE, "Speed Test History.csv")
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export History to CSV")
                    context.startActivity(shareIntent)
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", color = Color(0xFF3B82F6))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val sortedHistory = history.sortedByDescending { it.timestamp }
        
        sortedHistory.take(10).forEachIndexed { index, result ->
            val previousResult = if (index < sortedHistory.size - 1) sortedHistory[index + 1] else null
            val downloadTrend = if (previousResult != null) {
                result.downloadSpeed - previousResult.downloadSpeed
            } else 0f

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(result.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF29B6F6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(result.downloadSpeed),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            // Trend Indicator
                            if (previousResult != null && kotlin.math.abs(downloadTrend) > 0.1f) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val isImprovement = downloadTrend > 0
                                Icon(
                                    imageVector = if (isImprovement) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isImprovement) "Improved" else "Declined",
                                    tint = if (isImprovement) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.1f".format(result.uploadSpeed),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Upload, contentDescription = null, tint = Color(0xFF66BB6A), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.ping} ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedGauge(
    progress: Float,
    downloadSpeed: Float,
    uploadSpeed: Float,
    currentStage: TestStage,
    isDark: Boolean,
    textColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "GaugeProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(280.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 20.dp.toPx()
                val strokeWidth = 12.dp.toPx()
                
                // Gradient Colors (Cyan -> Green -> Pink/Purple)
                val gradientColors = listOf(
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF10B981), // Green
                    Color(0xFFD946EF), // Pink
                    Color(0xFF8B5CF6)  // Purple
                )
                
                // 1. Background Track
                drawCircle(
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                    radius = radius,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Gradient Progress Arc
                rotate(degrees = -90f) {
                    drawArc(
                        brush = Brush.sweepGradient(gradientColors),
                        startAngle = 0f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 3. Inner Ticks
                val tickRadius = radius - 25.dp.toPx()
                val tickCount = 12
                for (i in 0 until tickCount) {
                    val angle = (360f / tickCount) * i - 90f
                    val angleRad = Math.toRadians(angle.toDouble())
                    
                    val start = Offset(
                        (center.x + tickRadius * Math.cos(angleRad)).toFloat(),
                        (center.y + tickRadius * Math.sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + (tickRadius - 8.dp.toPx()) * Math.cos(angleRad)).toFloat(),
                        (center.y + (tickRadius - 8.dp.toPx()) * Math.sin(angleRad)).toFloat()
                    )
                    
                    drawLine(
                        color = textColor.copy(alpha = 0.3f),
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Central Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon (Download or Upload based on stage)
                val icon = when (currentStage) {
                    TestStage.UPLOAD -> Icons.Default.Upload
                    else -> Icons.Default.Download
                }
                val iconTint = when (currentStage) {
                    TestStage.UPLOAD -> Color(0xFF8B5CF6) // Purple for Upload
                    else -> Color(0xFF06B6D4) // Cyan for Download
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Values Side-by-Side
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Download Value
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Download",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "%.1f".format(downloadSpeed),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                    
                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .padding(horizontal = 12.dp)
                            .background(textColor.copy(alpha = 0.2f))
                    )
                    
                    // Upload Value
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Upload",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "%.1f".format(uploadSpeed),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryResultCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    iconColor: Color,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) cardBg else Color.White),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.5f)), // Border matching icon color
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactServerCard(
    modifier: Modifier = Modifier,
    servers: List<SpeedTestServer>,
    selectedServer: SpeedTestServer?,
    onServerSelected: (SpeedTestServer) -> Unit,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Light Theme: White bg, Blue Icon. Dark Theme: Transparent bg, White Icon.
    val containerColor = if (isDark) Color.Transparent else Color.White
    val iconColor = if (isDark) Color.White else Color(0xFF3B82F6) // Blue in light mode
    val labelColor = if (isDark) textColor.copy(alpha = 0.7f) else Color(0xFF6B7280) // Gray-500 in light mode

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFF3B82F6)), // Blue border
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(80.dp)
            .clickable { expanded = true }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Label + Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Test Server",
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = labelColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Bottom Row: Icon + Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Public,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedServer?.name ?: "Select",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(if (isDark) Color(0xFF1E293B) else Color.White)
            ) {
                servers.forEach { server ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(server.name, color = textColor)
                                Text(server.location, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
                            }
                        },
                        onClick = {
                            onServerSelected(server)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompactScheduleCard(
    modifier: Modifier = Modifier,
    isScheduled: Boolean,
    intervalHours: Long,
    onToggleSchedule: (Boolean, Long) -> Unit,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
) {
    // Light Theme: White bg. Dark Theme: Transparent bg.
    val containerColor = if (isDark) Color.Transparent else Color.White
    val labelColor = if (isDark) textColor.copy(alpha = 0.7f) else Color(0xFF6B7280) // Gray-500 in light mode
    
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFF4CAF50)), // Green border
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Label + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                
                Switch(
                    checked = isScheduled,
                    onCheckedChange = { onToggleSchedule(it, intervalHours) },
                    modifier = Modifier.scale(0.6f).height(20.dp), // Smaller switch
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFF4CAF50), // Green track in light mode
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Gray
                    )
                )
            }
            
            // Bottom Row: Status Text
            if (isScheduled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Cycle interval
                        val nextInterval = when (intervalHours) {
                            6L -> 12L
                            12L -> 24L
                            else -> 6L
                        }
                        onToggleSchedule(true, nextInterval)
                    }
                ) {
                    Text(
                        text = "Every ${intervalHours}h",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            } else {
                Text(
                    text = "Disabled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun NetworkInfoCard(info: NetworkInfo, isDark: Boolean, textColor: Color, cardBg: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) cardBg else Color.White),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6)), // Purple border
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Network",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = info.type.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Provider",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = info.isp.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
