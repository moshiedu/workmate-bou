package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.utils.BenchmarkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarksScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    var cpuProgress by remember { mutableStateOf(0f) }
    var cpuRunning by remember { mutableStateOf(false) }
    var cpuResult by remember { mutableStateOf<String?>(null) }

    var storageProgress by remember { mutableStateOf(0f) }
    var storageRunning by remember { mutableStateOf(false) }
    var storageResult by remember { mutableStateOf<String?>(null) }

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
    onRunClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = subtitleColor
            )
            
            if (resultText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = resultText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (isRunning) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Running benchmark...",
                    fontSize = 12.sp,
                    color = subtitleColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Button(
                    onClick = onRunClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Benchmark")
                }
            }
        }
    }
}
