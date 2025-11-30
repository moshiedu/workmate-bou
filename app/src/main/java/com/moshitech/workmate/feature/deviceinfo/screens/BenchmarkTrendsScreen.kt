package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.navigation.NavController
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkHistoryEntity
import com.moshitech.workmate.feature.deviceinfo.data.BenchmarkRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkTrendsScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val context = LocalContext.current
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    val repository = remember {
        val database = AppDatabase.getDatabase(context)
        BenchmarkRepository(database.benchmarkHistoryDao())
    }

    val historyList by repository.getAllHistory().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Benchmark Trends", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
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
                    "No benchmark data yet. Run some tests to see trends!",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TrendChart(
                        title = "Composite Score Trend",
                        data = historyList.map { it.compositeScore.toFloat() },
                        maxValue = 10000f,
                        cardColor = cardColor,
                        textColor = textColor,
                        subtitleColor = subtitleColor
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniTrendChart(
                            title = "CPU",
                            data = historyList.map { (it.cpuSingleCoreScore + it.cpuMultiCoreScore) / 2f },
                            cardColor = cardColor,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        MiniTrendChart(
                            title = "GPU",
                            data = historyList.map { it.gpuFps.toFloat() },
                            cardColor = cardColor,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniTrendChart(
                            title = "Storage",
                            data = historyList.map { 
                                val write = it.storageWriteSpeedMbPs.toFloatOrNull() ?: 0f
                                val read = it.storageReadSpeedMbPs.toFloatOrNull() ?: 0f
                                (write + read) / 2f
                            },
                            cardColor = cardColor,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        MiniTrendChart(
                            title = "RAM",
                            data = historyList.map { it.ramSequentialSpeedMbPs.toFloatOrNull() ?: 0f },
                            cardColor = cardColor,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendChart(
    title: String,
    data: List<Float>,
    maxValue: Float,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
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
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (data.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.size - 1).coerceAtLeast(1)
                    
                    val path = Path()
                    data.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value / maxValue * height)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        
                        // Draw point
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 5f,
                            center = Offset(x, y)
                        )
                    }
                    
                    // Draw line
                    drawPath(
                        path = path,
                        color = Color(0xFF3B82F6),
                        style = Stroke(width = 3f)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Latest: ${data.lastOrNull()?.toInt() ?: 0}",
                        fontSize = 11.sp,
                        color = subtitleColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${data.size} tests",
                        fontSize = 11.sp,
                        color = subtitleColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniTrendChart(
    title: String,
    data: List<Float>,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (data.isNotEmpty()) {
                val maxValue = data.maxOrNull() ?: 1f
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.size - 1).coerceAtLeast(1)
                    
                    val path = Path()
                    data.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = height - (value / maxValue * height)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 2.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "${data.lastOrNull()?.toInt() ?: 0}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
