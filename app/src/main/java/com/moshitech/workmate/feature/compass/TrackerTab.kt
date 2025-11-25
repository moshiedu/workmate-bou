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
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Current Speed",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    String.format("%.1f", currentSpeed),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isTracking) Color(0xFF10B981) else textColor,
                    fontSize = 64.sp
                )
                Text(
                    "km/h",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trip Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Distance", NavigationUtils.formatDistance(tripDistance), isDark, textColor)
            StatCard("Duration", NavigationUtils.formatDuration(tripDuration), isDark, textColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Avg Speed", NavigationUtils.formatSpeed(avgSpeed), isDark, textColor)
            StatCard("Max Speed", NavigationUtils.formatSpeed(maxSpeed), isDark, textColor)
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
                if (trips.size > 3) {
                    TextButton(onClick = { showAllTrips = !showAllTrips }) {
                        Text(if (showAllTrips) "Show Less" else "Show All")
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
fun StatCard(label: String, value: String, isDark: Boolean, textColor: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun TripHistoryItem(trip: TripEntity, isDark: Boolean, textColor: Color, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    NavigationUtils.formatDistance(trip.distance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    NavigationUtils.formatDuration(trip.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Avg: ${NavigationUtils.formatSpeed(trip.avgSpeed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    "Max: ${NavigationUtils.formatSpeed(trip.maxSpeed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}
