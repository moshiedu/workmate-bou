package com.moshitech.workmate.feature.rambooster

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamBoosterScreen(
    navController: NavController,
    viewModel: RamBoosterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val memoryFreed by viewModel.memoryFreed.collectAsState()
    val appsStopped by viewModel.appsStopped.collectAsState()
    
    val ramUsageText by viewModel.ramUsageText.collectAsState()
    val ramUsagePercent by viewModel.ramUsagePercent.collectAsState()
    
    val isDark = isSystemInDarkTheme()

    // Colors
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryBlue = Color(0xFF1976D2)
    val trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val borderColor = if (isDark) Color(0xFF334155) else Color.Transparent

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("RAM Booster", fontWeight = FontWeight.Bold, color = textColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // Placeholder for balance
                    IconButton(onClick = { }, enabled = false) { }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Circular Progress
            Box(contentAlignment = Alignment.Center) {
                CircularProgressBar(
                    percentage = if (state == BoosterState.IDLE) ramUsagePercent / 100f else progress,
                    radius = 100.dp,
                    color = primaryBlue,
                    trackColor = trackColor,
                    strokeWidth = 20.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state == BoosterState.BOOSTING) {
                        // Rocket or Spinner could go here
                    }
                    Text(
                        text = when (state) {
                            BoosterState.IDLE -> "$ramUsagePercent%"
                            BoosterState.BOOSTING -> "Boosting..."
                            BoosterState.BOOSTED -> "$ramUsagePercent%"
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "RAM Usage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
            Text(
                text = ramUsageText,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state == BoosterState.BOOSTING) {
                Text(
                    text = "Please wait while we optimize your\ndevice's memory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Memory Freed", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memoryFreed,
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Apps Stopped", color = secondaryTextColor, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appsStopped,
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            val lastBoosted by viewModel.lastBoosted.collectAsState()
            val stoppedAppsList by viewModel.stoppedAppsList.collectAsState()

            Spacer(modifier = Modifier.weight(1f))
            
            if (stoppedAppsList.isNotEmpty()) {
                Text(
                    text = "Stopped Apps:",
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    items(stoppedAppsList.size) { index ->
                        val app = stoppedAppsList[index]
                        Box(
                            modifier = Modifier
                                .background(trackColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${app.memory})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryTextColor
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Last Boosted: $lastBoosted",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            // ... (Button code)
            Button(
                onClick = { viewModel.startBoost() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    disabledContainerColor = primaryBlue.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = state != BoosterState.BOOSTING
            ) {
                Text(
                    text = if (state == BoosterState.BOOSTING) "BOOSTING..." else "BOOST NOW",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            if (!isPermissionGranted && state == BoosterState.IDLE) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant Permission for Real Results", color = Color.White)
                }
            }
            
            // Check permission on resume (simplified)
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.checkPermission()
            }
        }
    }
}

@Composable
fun CircularProgressBar(
    percentage: Float,
    radius: Dp,
    color: Color,
    trackColor: Color,
    strokeWidth: Dp
) {
    Canvas(modifier = Modifier.size(radius * 2)) {
        // Draw Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        // Draw Progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * percentage,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
