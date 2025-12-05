package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkHistoryEntity
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkResultsScreen(
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

    val benchmarkRepository = remember {
        val database = AppDatabase.getDatabase(context)
        BenchmarkRepository(database.benchmarkHistoryDao())
    }

    val historyList by benchmarkRepository.getAllHistory().collectAsState(initial = emptyList())
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Benchmark History", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    if (historyList.isNotEmpty()) {
                        TextButton(onClick = { navController.navigate("benchmark_trends") }) {
                            Text("Trends", color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = { showExportDialog = true }) {
                            Text("Export", color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                benchmarkRepository.deleteAll()
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Clear All", tint = textColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No benchmark results yet",
                    color = subtitleColor,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { result ->
                    BenchmarkResultCard(
                        result = result,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtitleColor = subtitleColor,
                        onDelete = {
                            scope.launch {
                                benchmarkRepository.deleteById(result.id)
                            }
                        }
                    )
                }
            }
        }
        
        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Results") },
                text = { Text("Choose export format:") },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            scope.launch {
                                val csv = benchmarkRepository.exportToCsv()
                                shareText(context, csv, "benchmark_results.csv", "text/csv")
                                showExportDialog = false
                            }
                        }) {
                            Text("CSV")
                        }
                        TextButton(onClick = {
                            scope.launch {
                                val json = benchmarkRepository.exportToJson()
                                shareText(context, json, "benchmark_results.json", "application/json")
                                showExportDialog = false
                            }
                        }) {
                            Text("JSON")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun shareText(context: android.content.Context, text: String, filename: String, mimeType: String) {
    try {
        val file = java.io.File(context.cacheDir, filename)
        file.writeText(text)
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Results"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun BenchmarkResultCard(
    result: BenchmarkHistoryEntity,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date(result.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = result.compositeScore.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = " / 10000",
                            fontSize = 13.sp,
                            color = subtitleColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when (result.performanceTier) {
                                    "Excellent" -> Color(0xFF10B981)
                                    "Good" -> Color(0xFF3B82F6)
                                    "Average" -> Color(0xFFFFA500)
                                    else -> Color(0xFFEF4444)
                                }.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.performanceTier,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (result.performanceTier) {
                                "Excellent" -> Color(0xFF10B981)
                                "Good" -> Color(0xFF3B82F6)
                                "Average" -> Color(0xFFFFA500)
                                else -> Color(0xFFEF4444)
                            }
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = subtitleColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = date,
                fontSize = 11.sp,
                color = subtitleColor.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            Divider(color = subtitleColor.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(10.dp))

            // Scores breakdown in grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreItem("CPU", "${result.cpuSingleCoreScore} / ${result.cpuMultiCoreScore}", subtitleColor, textColor)
                ScoreItem("GPU", "${result.gpuFps} FPS", subtitleColor, textColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreItem("Storage", "${result.storageWriteSpeedMbPs}/${result.storageReadSpeedMbPs} MB/s", subtitleColor, textColor)
                ScoreItem("RAM", "${result.ramSequentialSpeedMbPs} MB/s", subtitleColor, textColor)
            }
        }
    }
}

@Composable
fun ScoreItem(label: String, value: String, subtitleColor: Color, textColor: Color) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = subtitleColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
