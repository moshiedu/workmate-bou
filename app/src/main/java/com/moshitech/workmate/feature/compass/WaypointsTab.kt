package com.moshitech.workmate.feature.compass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.compass.data.WaypointEntity

@Composable
fun WaypointsTab(
    viewModel: CompassViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val waypoints by viewModel.waypoints.collectAsState(initial = emptyList())
    val selectedWaypoint by viewModel.selectedWaypoint.collectAsState()
    val useTrueNorth by viewModel.useTrueNorth.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (!useTrueNorth || locationInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.LocationOn, "Location Required", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("GPS Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Waypoints need your location. Please enable 'True North' in the Compass tab.", textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (waypoints.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No waypoints saved",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Text(
                    "Tap + to save current location",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(waypoints) { waypoint ->
                    WaypointItem(
                        waypoint = waypoint,
                        isSelected = selectedWaypoint?.id == waypoint.id,
                        distance = viewModel.getWaypointDistance(waypoint),
                        bearing = viewModel.getWaypointBearing(waypoint),
                        isDark = isDark,
                        textColor = textColor,
                        onSelect = { 
                            // Toggle: if already selected, deselect it
                            if (selectedWaypoint?.id == waypoint.id) {
                                viewModel.selectWaypoint(null)
                            } else {
                                viewModel.selectWaypoint(waypoint)
                            }
                        },
                        onDelete = { viewModel.deleteWaypoint(waypoint) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add Waypoint")
        }
    }

    if (showAddDialog) {
        AddWaypointDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, notes ->
                viewModel.saveWaypoint(name, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WaypointItem(
    waypoint: WaypointEntity,
    isSelected: Boolean,
    distance: String,
    bearing: Float,
    isDark: Boolean,
    textColor: Color,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF3B82F6).copy(alpha = 0.2f) 
            else if (isDark) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    waypoint.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (waypoint.notes.isNotEmpty()) {
                    Text(
                        waypoint.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "$distance • ${bearing.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = "Navigating",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddWaypointDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Waypoint") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name, notes) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
