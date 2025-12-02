package com.moshitech.workmate.feature.speedtest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                // Server Selection
                if (!state.isTesting) {
                    ServerSelectionCard(
                        servers = state.servers,
                        selectedServer = state.selectedServer,
                        onServerSelected = { viewModel.selectServer(it) },
                        isDark = isDark,
                        textColor = textColor,
                        cardBg = cardBg
                    )
                }

                // Schedule Test
                if (!state.isTesting) {
                    ScheduleTestCard(
                        isScheduled = state.isScheduled,
                        intervalHours = state.scheduleInterval,
                        onToggleSchedule = { enable, interval -> viewModel.toggleSchedule(enable, interval) },
                        isDark = isDark,
                        textColor = textColor,
                        cardBg = cardBg
                    )
                }

                // Network Info Card
                NetworkInfoCard(state.networkInfo, isDark = isDark, textColor = textColor, cardBg = cardBg)

                // Speed Gauge
                SpeedGauge(
                    progress = if (state.currentStage == TestStage.DOWNLOAD) state.downloadProgress else state.uploadProgress,
                    speedText = state.currentSpeedDisplay,
                    statusText = when (state.currentStage) {
                        TestStage.IDLE -> "Ready to Test"
                        TestStage.PING -> "Pinging Server..."
                        TestStage.DOWNLOAD -> "Testing Download..."
                        TestStage.UPLOAD -> "Testing Upload..."
                        TestStage.FINISHED -> "Test Complete"
                    },
                    isDark = isDark,
                    textColor = textColor,
                    cardBg = cardBg
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
                        color = Color(0xFFFFA726),
                        isDark = isDark
                    )
                    ResultItem(
                        icon = Icons.Default.Download,
                        label = "Download",
                        value = if (state.downloadSpeed > 0) "%.1f".format(state.downloadSpeed) else "--",
                        unit = "Mbps",
                        color = Color(0xFF29B6F6),
                        isDark = isDark
                    )
                    ResultItem(
                        icon = Icons.Default.Upload,
                        label = "Upload",
                        value = if (state.uploadSpeed > 0) "%.1f".format(state.uploadSpeed) else "--",
                        unit = "Mbps",
                        color = Color(0xFF66BB6A),
                        isDark = isDark
                    )
                }
                
                // Advanced Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultItem(
                        icon = Icons.Default.GraphicEq,
                        label = "Jitter",
                        value = if (state.jitter > 0) "${state.jitter}" else "--",
                        unit = "ms",
                        color = Color(0xFFAB47BC), // Purple
                        isDark = isDark
                    )
                    ResultItem(
                        icon = Icons.Default.ErrorOutline,
                        label = "Loss",
                        value = if (state.packetLoss >= 0 && state.currentStage != TestStage.IDLE && state.currentStage != TestStage.PING) "%.1f".format(state.packetLoss) else "--",
                        unit = "%",
                        color = Color(0xFFEF5350), // Red
                        isDark = isDark
                    )
                }

                // Start Button
                Button(
                    onClick = { viewModel.startTest() },
                    enabled = !state.isTesting && state.isNetworkAvailable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isNetworkAvailable) Color(0xFF3B82F6) else Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isTesting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing...")
                    } else {
                        Text(if (state.isNetworkAvailable) "Start Test" else "No Connection")
                    }
                }

                // Share Button
                if (state.currentStage == TestStage.FINISHED) {
                    Button(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981) // Green
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Results")
                    }
                }

                // History List
                if (state.history.isNotEmpty()) {
                    DetailedHistoryList(state.history, isDark = isDark, textColor = textColor, cardBg = cardBg)
                }
            }
        }
    }
}

@Composable
fun NetworkInfoCard(info: NetworkInfo, isDark: Boolean, textColor: Color, cardBg: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
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
    speedText: String,
    statusText: String,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
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
            Canvas(modifier = Modifier.size(220.dp)) {
                // Background Arc
                drawArc(
                    color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.2f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Progress Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = speedText.replace(" Mbps", ""),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp
                    ),
                    color = textColor
                )
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun ResultItem(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String = "",
    color: Color,
    isDark: Boolean = true
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
            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color(0xFF111827)
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionCard(
    servers: List<SpeedTestServer>,
    selectedServer: SpeedTestServer?,
    onServerSelected: (SpeedTestServer) -> Unit,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Test Server",
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedServer?.name ?: "Select Server",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = textColor.copy(alpha = 0.5f),
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
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
}

@Composable
fun ScheduleTestCard(
    isScheduled: Boolean,
    intervalHours: Long,
    onToggleSchedule: (Boolean, Long) -> Unit,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scheduled Tests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = if (isScheduled) "Running every $intervalHours hours" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = isScheduled,
                    onCheckedChange = { onToggleSchedule(it, intervalHours) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF3B82F6),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = cardBg
                    )
                )
            }
            
            if (isScheduled) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Interval (Hours)",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(6L, 12L, 24L).forEach { hours ->
                        FilterChip(
                            selected = intervalHours == hours,
                            onClick = { onToggleSchedule(true, hours) },
                            label = { Text("$hours h") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF3B82F6),
                                labelColor = textColor
                            )
                        )
                    }
                }
            }
        }
    }
}
