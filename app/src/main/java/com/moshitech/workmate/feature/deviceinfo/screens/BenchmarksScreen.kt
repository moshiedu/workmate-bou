package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.utils.BenchmarkUtils
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarksScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Read theme from repository
    val userPreferencesRepository = remember { com.moshitech.workmate.data.repository.UserPreferencesRepository(context) }
    val theme by userPreferencesRepository.theme.collectAsState(initial = com.moshitech.workmate.data.repository.AppTheme.SYSTEM)
    
    // Determine if dark mode should be used
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (theme) {
        com.moshitech.workmate.data.repository.AppTheme.LIGHT -> false
        com.moshitech.workmate.data.repository.AppTheme.DARK -> true
        com.moshitech.workmate.data.repository.AppTheme.SYSTEM -> systemDark
    }
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    // Initialize repository
    val benchmarkRepository = remember {
        val database = AppDatabase.getDatabase(context)
        BenchmarkRepository(database.benchmarkHistoryDao())
    }

    var cpuProgress by remember { mutableStateOf(0f) }
    var cpuRunning by remember { mutableStateOf(false) }
    var cpuResult by remember { mutableStateOf<String?>(null) }

    var storageProgress by remember { mutableStateOf(0f) }
    var storageRunning by remember { mutableStateOf(false) }
    var storageResult by remember { mutableStateOf<String?>(null) }

    var gpuProgress by remember { mutableStateOf(0f) }
    var gpuRunning by remember { mutableStateOf(false) }
    var gpuResult by remember { mutableStateOf<String?>(null) }

    var ramProgress by remember { mutableStateOf(0f) }
    var ramRunning by remember { mutableStateOf(false) }
    var ramResult by remember { mutableStateOf<String?>(null) }

    var batteryProgress by remember { mutableStateOf(0f) }
    var batteryRunning by remember { mutableStateOf(false) }
    var batteryResult by remember { mutableStateOf<String?>(null) }
    var currentTemp by remember { mutableStateOf(0f) }

    var compositeScore by remember { mutableStateOf<String?>(null) }
    var runningAll by remember { mutableStateOf(false) }
    var canCancel by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Benchmarks", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate("benchmark_results") }) {
                        Text("View History", color = MaterialTheme.colorScheme.primary)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Composite Score Card (if available)
            if (compositeScore != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OVERALL SCORE",
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val scoreValue = compositeScore?.split("/")?.get(0)?.trim()?.toIntOrNull() ?: 0
                        val tier = compositeScore?.split("\n")?.getOrNull(1) ?: ""
                        
                        Text(
                            text = scoreValue.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "/ 10,000",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    when (tier) {
                                        "Excellent" -> Color(0xFF10B981)
                                        "Good" -> Color(0xFF3B82F6)
                                        "Average" -> Color(0xFFFFA500)
                                        else -> Color(0xFFEF4444)
                                    }.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tier,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (tier) {
                                    "Excellent" -> Color(0xFF10B981)
                                    "Good" -> Color(0xFF3B82F6)
                                    "Average" -> Color(0xFFFFA500)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                        }
                        
                        // Device Comparison
                        scoreValue.let {
                            if (it > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(
                                    modifier = Modifier.fillMaxWidth(0.3f),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = com.moshitech.workmate.feature.deviceinfo.utils.DeviceComparison.getPercentileRanking(it),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                val closest = com.moshitech.workmate.feature.deviceinfo.utils.DeviceComparison.getClosestDevice(it)
                                closest?.let { device ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Similar to ${device.name}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Run All Tests Button
            Button(
                onClick = {
                    scope.launch {
                        runningAll = true
                        compositeScore = null
                        
                        // Run CPU
                        cpuRunning = true
                        cpuProgress = 0f
                        cpuResult = null
                        val cpuRes = BenchmarkUtils.runCpuBenchmark { cpuProgress = it }
                        cpuResult = "Single-Core: ${cpuRes.singleCoreScore}\nMulti-Core: ${cpuRes.multiCoreScore}"
                        cpuRunning = false
                        
                        // Run GPU
                        gpuRunning = true
                        gpuProgress = 0f
                        gpuResult = null
                        val gpuRes = BenchmarkUtils.runGpuBenchmark { gpuProgress = it }
                        gpuResult = "Average FPS: ${gpuRes.averageFps}\nRender Time: ${gpuRes.renderTimeMs} ms"
                        gpuRunning = false
                        
                        // Run Storage
                        storageRunning = true
                        storageProgress = 0f
                        storageResult = null
                        val storageRes = BenchmarkUtils.runStorageBenchmark(context) { storageProgress = it }
                        storageResult = "Write: ${storageRes.writeSpeedMbPs} MB/s\nRead: ${storageRes.readSpeedMbPs} MB/s"
                        storageRunning = false
                        
                        // Run RAM
                        ramRunning = true
                        ramProgress = 0f
                        ramResult = null
                        val ramRes = BenchmarkUtils.runRamBenchmark { ramProgress = it }
                        ramResult = "Sequential: ${ramRes.sequentialSpeedMbPs} MB/s\nRandom Access: ${ramRes.randomAccessTimeMs} ms"
                        ramRunning = false
                        
                        // Calculate composite score
                        val score = BenchmarkUtils.calculateCompositeScore(
                            cpuRes.singleCoreScore,
                            cpuRes.multiCoreScore,
                            gpuRes.averageFps,
                            storageRes.writeSpeedMbPs.toDouble(),
                            storageRes.readSpeedMbPs.toDouble(),
                            ramRes.sequentialSpeedMbPs.toDouble()
                        )
                        compositeScore = "${score.totalScore} / 10000\n${score.tier}"
                        
                        // Save to database
                        withContext(Dispatchers.IO) {
                            benchmarkRepository.saveResult(
                                cpuSingleCore = cpuRes.singleCoreScore,
                                cpuMultiCore = cpuRes.multiCoreScore,
                                gpuFps = gpuRes.averageFps,
                                gpuRenderTime = gpuRes.renderTimeMs,
                                storageWrite = storageRes.writeSpeedMbPs,
                                storageRead = storageRes.readSpeedMbPs,
                                ramSequential = ramRes.sequentialSpeedMbPs,
                                ramRandomAccess = ramRes.randomAccessTimeMs,
                                compositeScore = score.totalScore,
                                tier = score.tier
                            )
                        }
                        
                        runningAll = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !runningAll && !cpuRunning && !gpuRunning && !storageRunning && !ramRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (runningAll) "Running All Tests..." else "Run All Tests",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cancel Button (shown when any test is running)
            if (cpuRunning || gpuRunning || storageRunning || ramRunning || batteryRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        cpuRunning = false
                        gpuRunning = false
                        storageRunning = false
                        ramRunning = false
                        batteryRunning = false
                        runningAll = false
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Tests", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            // CPU Benchmark Card
            BenchmarkCard(
                title = "CPU Benchmark",
                icon = Icons.Default.Memory,
                description = "Measure Single-Core and Multi-Core performance.",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor,
                isRunning = cpuRunning,
                progress = cpuProgress,
                resultText = cpuResult,
                estimatedDuration = BenchmarkUtils.CPU_BENCHMARK_DURATION,
                onRunClick = {
                    scope.launch {
                        cpuRunning = true
                        cpuProgress = 0f
                        cpuResult = null
                        val result = BenchmarkUtils.runCpuBenchmark { progress ->
                            cpuProgress = progress
                        }
                        cpuResult = "Single-Core: ${result.singleCoreScore}\nMulti-Core: ${result.multiCoreScore}"
                        cpuRunning = false
                    }
                }
            )

            // Storage Benchmark Card
            BenchmarkCard(
                title = "Storage Benchmark",
                icon = Icons.Default.Storage,
                description = "Test Internal Storage Read/Write speeds.",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor,
                isRunning = storageRunning,
                progress = storageProgress,
                resultText = storageResult,
                estimatedDuration = BenchmarkUtils.STORAGE_BENCHMARK_DURATION,
                onRunClick = {
                    scope.launch {
                        storageRunning = true
                        storageProgress = 0f
                        storageResult = null
                        val result = BenchmarkUtils.runStorageBenchmark(context) { progress ->
                            storageProgress = progress
                        }
                        storageResult = "Write: ${result.writeSpeedMbPs} MB/s\nRead: ${result.readSpeedMbPs} MB/s"
                        storageRunning = false
                    }
                }
            )

            // GPU Benchmark Card
            BenchmarkCard(
                title = "GPU Benchmark",
                icon = Icons.Default.Memory,
                description = "Test graphics rendering performance.",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor,
                isRunning = gpuRunning,
                progress = gpuProgress,
                resultText = gpuResult,
                estimatedDuration = BenchmarkUtils.GPU_BENCHMARK_DURATION,
                onRunClick = {
                    scope.launch {
                        gpuRunning = true
                        gpuProgress = 0f
                        gpuResult = null
                        val result = BenchmarkUtils.runGpuBenchmark { progress ->
                            gpuProgress = progress
                        }
                        gpuResult = "Average FPS: ${result.averageFps}\nRender Time: ${result.renderTimeMs} ms"
                        gpuRunning = false
                    }
                }
            )

            // RAM Benchmark Card
            BenchmarkCard(
                title = "RAM Benchmark",
                icon = Icons.Default.Storage,
                description = "Test memory access speed and performance.",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor,
                isRunning = ramRunning,
                progress = ramProgress,
                resultText = ramResult,
                estimatedDuration = BenchmarkUtils.RAM_BENCHMARK_DURATION,
                onRunClick = {
                    scope.launch {
                        ramRunning = true
                        ramProgress = 0f
                        ramResult = null
                        val result = BenchmarkUtils.runRamBenchmark { progress ->
                            ramProgress = progress
                        }
                        ramResult = "Sequential: ${result.sequentialSpeedMbPs} MB/s\nRandom Access: ${result.randomAccessTimeMs} ms"
                        ramRunning = false
                    }
                }
            )

            // Battery Stress Test Card
            BenchmarkCard(
                title = "Battery Stress Test",
                icon = Icons.Default.Memory,
                description = "Run all tests simultaneously. Monitor battery drain and temperature.",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor,
                isRunning = batteryRunning,
                progress = batteryProgress,
                resultText = batteryResult,
                extraInfo = if (batteryRunning && currentTemp > 0) "Temp: ${String.format("%.1f", currentTemp)}°C" else null,
                estimatedDuration = BenchmarkUtils.BATTERY_STRESS_DURATION,
                onRunClick = {
                    scope.launch {
                        batteryRunning = true
                        batteryProgress = 0f
                        batteryResult = null
                        currentTemp = 0f
                        val result = BenchmarkUtils.runBatteryStressTest(context,
                            onProgress = { batteryProgress = it },
                            onTempUpdate = { currentTemp = it }
                        )
                        batteryResult = "Duration: ${result.durationSeconds}s\nBattery Drain: ${result.batteryDrainPercent}%\nTemp Increase: ${String.format("%.1f", result.tempIncreaseC)}°C\nMax Temp: ${String.format("%.1f", result.maxTempC)}°C"
                        batteryRunning = false
                    }
                }
            )
        }
    }
}

@Composable
fun BenchmarkCard(
    title: String,
    icon: ImageVector,
    description: String,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    isRunning: Boolean,
    progress: Float,
    resultText: String?,
    extraInfo: String? = null,
    estimatedDuration: Int = 0,
    onRunClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    if (estimatedDuration > 0 && !isRunning) {
                        Text(
                            text = "~${estimatedDuration}s",
                            fontSize = 11.sp,
                            color = subtitleColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = subtitleColor,
                lineHeight = 16.sp
            )
            
            if (resultText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = resultText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Extra info (e.g., temperature)
            if (extraInfo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = extraInfo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (isRunning) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = subtitleColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Button(
                    onClick = onRunClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Test", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
