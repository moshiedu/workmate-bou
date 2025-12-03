package com.moshitech.workmate.feature.compass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.compass.data.TripEntity

@Composable
fun TrackerTab(
    viewModel: CompassViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val isTracking by viewModel.isTracking.collectAsState()
    val currentSpeed by viewModel.currentSpeed.collectAsState()
    val tripDistance by viewModel.tripDistance.collectAsState()
    val tripDuration by viewModel.tripDuration.collectAsState()
    val avgSpeed by viewModel.avgSpeed.collectAsState()
    val maxSpeed by viewModel.maxSpeed.collectAsState()
    val trips by viewModel.trips.collectAsState(initial = emptyList())
    val useTrueNorth by viewModel.useTrueNorth.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()
    
    var showAllTrips by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // GPS Warning if not enabled
        if (!useTrueNorth || locationInfo == null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Enable True North in Compass tab for tracking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF92400E)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Current Speed Display
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrackerSpeedGauge(
                    currentSpeed = currentSpeed,
                    isDark = isDark,
                    textColor = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trip Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                "Distance", 
                NavigationUtils.formatDistance(tripDistance), 
                Icons.Default.Straighten,
                Color(0xFF3B82F6), // Blue
                isDark, 
                textColor,
                Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                "Duration", 
                NavigationUtils.formatDuration(tripDuration), 
                Icons.Default.Timer,
                Color(0xFFF59E0B), // Orange
                isDark, 
                textColor,
                Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                "Avg Speed", 
                NavigationUtils.formatSpeed(avgSpeed), 
                Icons.Default.Speed,
                Color(0xFF10B981), // Green
                isDark, 
                textColor,
                Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                "Max Speed", 
                NavigationUtils.formatSpeed(maxSpeed), 
                Icons.Default.Bolt,
                Color(0xFF8B5CF6), // Purple
                isDark, 
                textColor,
                Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!isTracking) {
                Button(
                    onClick = { 
                        if (useTrueNorth && locationInfo != null) {
                            viewModel.startTrip()
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = useTrueNorth && locationInfo != null
                ) {
                    Icon(Icons.Default.PlayArrow, "Start")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Trip")
                }
            } else {
                Button(
                    onClick = { viewModel.stopTrip() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.Stop, "Stop")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Trip")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { viewModel.resetTrip() },
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isTracking
            ) {
                Icon(Icons.Default.Refresh, "Reset")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trip History
        if (trips.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Trip History (${trips.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row {
                    TextButton(onClick = { viewModel.clearAllTrips() }) {
                        Text("Clear All", color = Color.Red)
                    }
                    if (trips.size > 3) {
                        TextButton(onClick = { showAllTrips = !showAllTrips }) {
                            Text(if (showAllTrips) "Show Less" else "Show All")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val displayTrips = if (showAllTrips) trips else trips.take(3)
            displayTrips.forEach { trip ->
                TripHistoryItem(trip, isDark, textColor, onDelete = {
                    viewModel.deleteTrip(trip)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    isDark: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TripHistoryItem(trip: TripEntity, isDark: Boolean, textColor: Color, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Straighten,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        NavigationUtils.formatDistance(trip.distance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        NavigationUtils.formatDuration(trip.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Avg: ${NavigationUtils.formatSpeed(trip.avgSpeed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Max: ${NavigationUtils.formatSpeed(trip.maxSpeed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun TrackerSpeedGauge(
    currentSpeed: Float,
    isDark: Boolean,
    textColor: Color
) {
    val maxSpeed = 120f // Max speed for gauge visualization
    val progress = (currentSpeed / maxSpeed).coerceIn(0f, 1f)
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
            Canvas(modifier = Modifier.size(260.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 20.dp.toPx()
                val strokeWidth = 16.dp.toPx()
                
                // Gradient Colors (Green -> Yellow -> Orange -> Red)
                val gradientColors = listOf(
                    Color(0xFF10B981), // Green
                    Color(0xFFFACC15), // Yellow
                    Color(0xFFF97316), // Orange
                    Color(0xFFEF4444)  // Red
                )
                
                // 1. Background Track
                drawCircle(
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                    radius = radius,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Gradient Progress Arc
                // Start from -225 degrees (bottom left) to +45 degrees (bottom right) -> 270 degrees total span
                val startAngle = 135f
                val sweepAngle = 270f
                
                // Draw background arc for the full range
                drawArc(
                    color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw active progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 3. Inner Ticks
                val tickRadius = radius - 25.dp.toPx()
                val tickCount = 9 // 0, 15, 30, ... 120
                val totalAngle = 270f
                
                for (i in 0..tickCount) {
                    val angle = startAngle + (totalAngle / tickCount) * i
                    val angleRad = Math.toRadians(angle.toDouble())
                    
                    val start = Offset(
                        (center.x + tickRadius * Math.cos(angleRad)).toFloat(),
                        (center.y + tickRadius * Math.sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + (tickRadius - 10.dp.toPx()) * Math.cos(angleRad)).toFloat(),
                        (center.y + (tickRadius - 10.dp.toPx()) * Math.sin(angleRad)).toFloat()
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
                Text(
                    text = "SPEED",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = String.format("%.1f", currentSpeed),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    ),
                    color = textColor
                )
                
                Text(
                    text = "km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
