package com.moshitech.workmate.feature.widgets

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.moshitech.workmate.widget.NetworkSpeedService
import com.moshitech.workmate.widget.BatteryMonitorService
import com.moshitech.workmate.widget.FloatingMonitorService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(NetworkSpeedService.isServiceRunning) }
    var isBatteryServiceRunning by remember { mutableStateOf(BatteryMonitorService.isServiceRunning) }
    var isFloatingServiceRunning by remember { mutableStateOf(FloatingMonitorService.isServiceRunning) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        )
    }

    // Re-check overlay permission when composition is active
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500) // Check every 500ms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val currentPermission = Settings.canDrawOverlays(context)
                if (currentPermission != hasOverlayPermission) {
                    hasOverlayPermission = currentPermission
                }
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Widgets", fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Network Speed Widget Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = "Network Speed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Network Speed Monitor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "Real-time upload/download speeds",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                    // Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isServiceRunning) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.fillMaxSize()
                            ) {}
                        }
                        Text(
                            if (isServiceRunning) "Service Running" else "Service Stopped",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }

                    // Permission Warning
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Notification permission required for foreground service",
                                    fontSize = 12.sp,
                                    color = Color(0xFF856404)
                                )
                            }
                        }
                    }

                    // Overlay Permission Warning
                    if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Overlay permission required to display speed on top of apps",
                                    fontSize = 12.sp,
                                    color = Color(0xFF856404)
                                )
                            }
                        }
                    }

                    // Notification Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Hide Notification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    "To hide the icon, disable notifications for this category in System Settings.",
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            putExtra(Settings.EXTRA_CHANNEL_ID, "network_speed_monitor")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback to app notification settings
                                            val appIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(appIntent)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Settings, "Settings", tint = Color(0xFF10B981))
                                }
                            }
                        }
                    }

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                            ) {
                                Icon(Icons.Default.Notifications, "Permission", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Notification")
                            }
                        } else if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                            ) {
                                Icon(Icons.Default.Visibility, "Overlay", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Overlay Permission")
                            }
                        } else {
                            if (isServiceRunning) {
                                Button(
                                    onClick = {
                                        val serviceIntent = Intent(context, NetworkSpeedService::class.java)
                                        context.stopService(serviceIntent)
                                        isServiceRunning = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Stop Service")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val serviceIntent = Intent(context, NetworkSpeedService::class.java)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                        isServiceRunning = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Start Service")
                                }
                            }
                        }
                    }

                    // Instructions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "How to add widget:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            InstructionStep("1", "Long-press on your home screen", textColor)
                            InstructionStep("2", "Tap 'Widgets'", textColor)
                            InstructionStep("3", "Find 'Workmate' → 'Network Speed Monitor'", textColor)
                            InstructionStep("4", "Drag widget to home screen", textColor)
                        }
                    }
                }
            }

            // Battery Monitor Widget Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Battery Monitor Widget",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "Real-time battery stats & temperature",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                    // Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isBatteryServiceRunning) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.fillMaxSize()
                            ) {}
                        }
                        Text(
                            if (isBatteryServiceRunning) "Service Running" else "Service Stopped",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isBatteryServiceRunning) {
                            Button(
                                onClick = {
                                    val serviceIntent = Intent(context, BatteryMonitorService::class.java)
                                    context.stopService(serviceIntent)
                                    isBatteryServiceRunning = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Stop Service")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val serviceIntent = Intent(context, BatteryMonitorService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                    isBatteryServiceRunning = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start Service")
                            }
                        }
                    }

                    // Instructions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "How to add widget:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            InstructionStep("1", "Long-press on your home screen", textColor)
                            InstructionStep("2", "Tap 'Widgets'", textColor)
                            InstructionStep("3", "Find 'Workmate' → 'Battery Monitor'", textColor)
                            InstructionStep("4", "Drag widget to home screen", textColor)
                        }
                    }
                }
            }

            // Floating Hardware Monitor Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Floating Monitor",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Floating Hardware Monitor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "Overlay CPU, RAM & Network stats",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                    // Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isFloatingServiceRunning) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.fillMaxSize()
                            ) {}
                        }
                        Text(
                            if (isFloatingServiceRunning) "Overlay Active" else "Overlay Inactive",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }

                    // Overlay Permission Warning
                    if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Overlay permission required",
                                    fontSize = 12.sp,
                                    color = Color(0xFF856404)
                                )
                            }
                        }
                    }

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                            ) {
                                Icon(Icons.Default.Visibility, "Overlay", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Grant Permission")
                            }
                        } else {
                            if (isFloatingServiceRunning) {
                                Button(
                                    onClick = {
                                        val serviceIntent = Intent(context, FloatingMonitorService::class.java)
                                        context.stopService(serviceIntent)
                                        isFloatingServiceRunning = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Stop Overlay")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val serviceIntent = Intent(context, FloatingMonitorService::class.java)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                        isFloatingServiceRunning = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Start", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Start Overlay")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF10B981),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Text(
            text,
            fontSize = 13.sp,
            color = textColor.copy(alpha = 0.8f)
        )
    }
}
