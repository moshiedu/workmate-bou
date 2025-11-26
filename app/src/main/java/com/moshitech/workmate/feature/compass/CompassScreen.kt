package com.moshitech.workmate.feature.compass

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moshitech.workmate.feature.compass.CompassViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    navController: NavController,
    viewModel: CompassViewModel = viewModel()
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Compass", "Leveler", "Waypoints", "Tracker", "AR")

    // Permission Launcher for True North
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.toggleTrueNorth(true)
        }
    }

    // Lifecycle handling
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startSensors()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopSensors()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensors()
        }
    }

    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        CompassHelpDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Compass Tools", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Info, "Help", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            androidx.compose.material3.ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = backgroundColor,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> CompassTab(viewModel, isDark, textColor, onTrueNorthToggle = { enabled ->
                        if (enabled) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.toggleTrueNorth(true)
                            } else {
                                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        } else {
                            viewModel.toggleTrueNorth(false)
                        }
                    })
                    1 -> LevelerTab(viewModel, isDark, textColor)
                    2 -> WaypointsTab(viewModel, isDark, textColor)
                    3 -> TrackerTab(viewModel, isDark, textColor)
                    4 -> ARTab(viewModel, isDark, textColor)
                }
            }
        }
    }
}

@Composable
fun CompassTab(
    viewModel: CompassViewModel, 
    isDark: Boolean, 
    textColor: Color,
    onTrueNorthToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val azimuth by viewModel.azimuth.collectAsState()
    val cardinalDirection by viewModel.cardinalDirection.collectAsState()
    val accuracy by viewModel.accuracy.collectAsState()
    val useTrueNorth by viewModel.useTrueNorth.collectAsState()
    val qiblaDirection by viewModel.qiblaDirection.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val magneticField by viewModel.magneticFieldStrength.collectAsState()
    val sunTimes by viewModel.sunTimes.collectAsState()
    
    // Theme State (Simple toggle for now)
    var themeIndex by remember { mutableStateOf(0) }
    val accentColors = listOf(Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFFF59E0B))
    val accentColor = accentColors[themeIndex % accentColors.size]

    val rotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(durationMillis = 200),
        label = "CompassRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Degree Display
        Text(
            text = "${azimuth.toInt()}° $cardinalDirection",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Compass Dial
        Box(
            modifier = Modifier
                .size(300.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .background(if (isDark) Color(0xFF1E293B) else Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val center = center
                val radius = size.minDimension / 2
                
                rotate(degrees = rotation, pivot = center) {
                    // Ticks
                    for (i in 0 until 360 step 2) {
                        val isMajor = i % 30 == 0
                        val isCardinal = i % 90 == 0
                        val tickLength = if (isCardinal) 20.dp.toPx() else if (isMajor) 15.dp.toPx() else 8.dp.toPx()
                        val strokeWidth = if (isCardinal) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                        val color = if (isCardinal && i == 0) accentColor else if (isDark) Color.LightGray else Color.DarkGray
                        
                        val angleRad = Math.toRadians(i.toDouble() - 90)
                        val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                        val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                        val endX = center.x + radius * cos(angleRad).toFloat()
                        val endY = center.y + radius * sin(angleRad).toFloat()
                        
                        drawLine(color = color, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    }

                    // Draw Cardinal Directions (N, E, S, W)
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textSize = 24.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        val directions = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
                        val textRadius = radius - 45.dp.toPx()

                        directions.forEach { (text, angle) ->
                            val angleRad = Math.toRadians(angle.toDouble() - 90)
                            val x = center.x + textRadius * cos(angleRad).toFloat()
                            val y = center.y + textRadius * sin(angleRad).toFloat() + (paint.textSize / 3)

                            paint.color = if (text == "N") accentColor.toArgb() else textColor.toArgb()
                            
                            // Save canvas state to rotate text to be upright relative to the center
                            canvas.save()
                            canvas.rotate(angle.toFloat(), x, y)
                            canvas.nativeCanvas.drawText(text, x, y, paint)
                            canvas.restore()
                        }
                    }
                    
                    // Qibla Indicator (Green Arrow)
                    if (useTrueNorth && qiblaDirection != 0f) {
                        rotate(degrees = qiblaDirection, pivot = center) {
                            val qiblaPath = Path().apply {
                                moveTo(center.x, center.y - radius + 30.dp.toPx())
                                lineTo(center.x - 8.dp.toPx(), center.y - radius + 45.dp.toPx())
                                lineTo(center.x + 8.dp.toPx(), center.y - radius + 45.dp.toPx())
                                close()
                            }
                            drawPath(qiblaPath, Color(0xFF10B981)) // Green for Qibla
                        }
                    }
                }
                
                // Static North Indicator
                val indicatorPath = Path().apply {
                    moveTo(center.x, center.y - radius - 10.dp.toPx())
                    lineTo(center.x - 10.dp.toPx(), center.y - radius - 25.dp.toPx())
                    lineTo(center.x + 10.dp.toPx(), center.y - radius - 25.dp.toPx())
                    close()
                }
                drawPath(indicatorPath, accentColor)
            }
            
            // Center decoration
            Box(modifier = Modifier.size(10.dp).background(accentColor, CircleShape))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // GPS Dashboard
        if (locationInfo != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = locationInfo?.address ?: "Unknown Location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        GPSItem("Lat", String.format("%.4f", locationInfo?.latitude), textColor)
                        GPSItem("Lon", String.format("%.4f", locationInfo?.longitude), textColor)
                        GPSItem("Alt", "${locationInfo?.altitude?.toInt()}m", textColor)
                        GPSItem("Spd", "${locationInfo?.speed?.toInt()}km/h", textColor)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        GPSItem("Mag", "${magneticField.toInt()}μT", textColor)
                        if (sunTimes != null) {
                            GPSItem("Sunrise", sunTimes!!.sunrise, textColor)
                            GPSItem("Sunset", sunTimes!!.sunset, textColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { viewModel.shareCoordinates(context) }) {
                            Icon(Icons.Default.Share, "Share", tint = accentColor)
                        }
                        IconButton(onClick = { viewModel.copyCoordinates(context) }) {
                            Icon(Icons.Default.ContentCopy, "Copy", tint = accentColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tools Row (Flashlight, SOS, Haptic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flashlight
            IconButton(onClick = { viewModel.toggleFlashlight() }) {
                Icon(
                    if (isFlashlightOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    "Flashlight",
                    tint = if (isFlashlightOn) Color(0xFFF59E0B) else textColor
                )
            }

            // SOS
            IconButton(onClick = { viewModel.toggleSos() }) {
                Icon(
                    Icons.Rounded.Sos,
                    "SOS",
                    tint = if (isSosActive) Color(0xFFEF4444) else textColor
                )
            }

            // Haptic
            IconButton(onClick = { viewModel.toggleHaptic(!hapticEnabled) }) {
                Icon(
                    Icons.Default.Vibration,
                    "Haptic Feedback",
                    tint = if (hapticEnabled) accentColor else Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("True North & Qibla", color = textColor)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = useTrueNorth, onCheckedChange = onTrueNorthToggle)
        }
        
        // Legend
        if (useTrueNorth) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(Color(0xFFEF4444), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("North", style = MaterialTheme.typography.bodySmall, color = textColor)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Qibla (Mecca)", style = MaterialTheme.typography.bodySmall, color = textColor)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        IconButton(onClick = { themeIndex++ }) {
            Icon(Icons.Default.ColorLens, "Change Theme", tint = accentColor)
        }
        
        // Calibration Warning
        if (accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calibrate: Wave in Figure 8", color = Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
fun LevelerTab(viewModel: CompassViewModel, isDark: Boolean, textColor: Color) {
    val pitch by viewModel.pitch.collectAsState()
    val roll by viewModel.roll.collectAsState()
    
    val isLevel = kotlin.math.abs(pitch) < 1 && kotlin.math.abs(roll) < 1
    val bubbleColor = if (isLevel) Color(0xFF10B981) else (if (isDark) Color(0xFF3B82F6) else Color(0xFF1976D2))
    val gridColor = if (isDark) Color.Gray else Color.LightGray

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Surface Leveler",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )
        Text(
            text = "Place device on a surface",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Bubble Level UI
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(if (isDark) Color(0xFF1E293B) else Color.White, CircleShape)
                .border(if (isLevel) 4.dp else 2.dp, if (isLevel) Color(0xFF10B981) else gridColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = center
                val radius = size.minDimension / 2
                
                // Crosshair
                drawLine(gridColor, Offset(center.x, center.y - 20.dp.toPx()), Offset(center.x, center.y + 20.dp.toPx()), 2.dp.toPx())
                drawLine(gridColor, Offset(center.x - 20.dp.toPx(), center.y), Offset(center.x + 20.dp.toPx(), center.y), 2.dp.toPx())
                
                // Center Target Circle
                drawCircle(gridColor, radius * 0.1f, center, style = Stroke(2.dp.toPx()))
                
                // Outer Ring
                drawCircle(gridColor, radius * 0.5f, center, style = Stroke(1.dp.toPx()))
                
                // Bubble (Clamped to circle)
                val maxAngle = 45f
                val xOffset = (roll.coerceIn(-maxAngle, maxAngle) / maxAngle) * radius
                val yOffset = (-pitch.coerceIn(-maxAngle, maxAngle) / maxAngle) * radius 
                
                drawCircle(
                    color = bubbleColor,
                    radius = 20.dp.toPx(),
                    center = center + Offset(xOffset, -yOffset)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "X: ${roll.toInt()}°  Y: ${pitch.toInt()}°",
            style = MaterialTheme.typography.headlineMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isLevel) "Perfectly Level!" else "Tilt to center bubble",
            color = if (isLevel) Color(0xFF10B981) else Color.Gray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun CompassHelpDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compass Guide") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HelpItem("🧭 Compass", "Shows your magnetic heading. Keep the device flat and away from magnets/metal for accuracy.")
                HelpItem("📍 True North", "Uses GPS to correct for magnetic declination, pointing to the geographic North Pole.")
                HelpItem("🕋 Qibla", "Points to Mecca (Kaaba). Requires 'True North' to be enabled.")
                HelpItem("📡 GPS Dashboard", "Displays your current coordinates, altitude, speed, and address.")
                HelpItem("🧲 Magnetic Field", "Shows magnetic field strength in microteslas (μT). Useful for detecting metal or interference.")
                HelpItem("🌅 Sun Times", "Displays sunrise and sunset times based on your GPS location (offline calculation).")
                HelpItem("📤 Share/Copy", "Share your location via messaging apps or copy coordinates to clipboard.")
                HelpItem("🔦 Flashlight & SOS", "Toggle the torch for light. SOS mode flashes the international distress signal (••• ——— •••) to signal for help in emergencies.")
                HelpItem("📳 Haptic Feedback", "Vibrates when aligned with North or Qibla for eyes-free usage.")
                HelpItem("⚖️ Leveler", "Checks if a surface is perfectly horizontal. Green means level.")
                HelpItem("📍 Waypoints", "Save your current location with a custom name. View distance and bearing to saved points. Tap a waypoint to navigate.")
                HelpItem("🏃 Speed Tracker", "Track your trip distance, speed, and duration. View trip history and statistics.")
                HelpItem("📱 AR Mode", "Augmented reality view with camera overlay showing compass directions and waypoint bearings.")
                HelpItem("⚠️ Calibration", "If accuracy is low, wave your phone in a figure-8 motion to recalibrate.")
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Disclaimer: This app is for reference only. Do not rely on it for critical navigation or life-threatening emergency situations. Sensor accuracy varies by device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun HelpItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun GPSItem(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = textColor, fontWeight = FontWeight.Bold)
    }
}
