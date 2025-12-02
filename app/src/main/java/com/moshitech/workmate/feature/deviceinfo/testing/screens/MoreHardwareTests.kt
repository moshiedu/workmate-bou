package com.moshitech.workmate.feature.deviceinfo.testing.screens

import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccelerometerTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var xValue by remember { mutableStateOf(0f) }
    var yValue by remember { mutableStateOf(0f) }
    var zValue by remember { mutableStateOf(0f) }
    var magnitude by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                event?.let {
                    xValue = it.values[0]
                    yValue = it.values[1]
                    zValue = it.values[2]
                    magnitude = sqrt(xValue * xValue + yValue * yValue + zValue * zValue)
                }
            }
            
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accelerometer Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ScreenRotation,
                contentDescription = "Accelerometer",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Move your device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))
            
            // X Axis
            AxisDisplay("X Axis", xValue, Color(0xFFEF4444))
            Spacer(Modifier.height(16.dp))
            
            // Y Axis
            AxisDisplay("Y Axis", yValue, Color(0xFF10B981))
            Spacer(Modifier.height(16.dp))
            
            // Z Axis
            AxisDisplay("Z Axis", zValue, Color(0xFF1890FF))
            Spacer(Modifier.height(24.dp))
            
            // Magnitude
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Magnitude", fontWeight = FontWeight.Bold)
                    Text(
                        String.format("%.2f m/s²", magnitude),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1890FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun AxisDisplay(label: String, value: Float, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(
                String.format("%.2f m/s²", value),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashlightTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isOn by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashlight Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.FlashlightOn,
                contentDescription = "Flashlight",
                modifier = Modifier.size(120.dp),
                tint = if (isOn) Color(0xFFFFA500) else Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (isOn) "Flashlight ON" else "Flashlight OFF",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOn) Color(0xFFFFA500) else Color.Gray
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = {
                    isOn = !isOn
                    try {
                        val cameraManager = context.getSystemService(android.content.Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                        val cameraId = cameraManager.cameraIdList.firstOrNull()
                        cameraId?.let {
                            cameraManager.setTorchMode(it, isOn)
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOn) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            ) {
                Icon(
                    if (isOn) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
                    contentDescription = if (isOn) "Turn Off" else "Turn On",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            if (isOn) {
                try {
                    val cameraManager = context.getSystemService(android.content.Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                    val cameraId = cameraManager.cameraIdList.firstOrNull()
                    cameraId?.let {
                        cameraManager.setTorchMode(it, false)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrationTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var intensity by remember { mutableStateOf(0.5f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vibration Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Vibration,
                contentDescription = "Vibration",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Intensity: ${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    try {
                        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                            vibratorManager.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                        }
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                android.os.VibrationEffect.createOneShot(
                                    500,
                                    (intensity * 255).toInt()
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(500)
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1890FF))
            ) {
                Icon(
                    Icons.Default.Vibration,
                    contentDescription = "Vibrate",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Tap to test vibration",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyroscopeTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var xValue by remember { mutableStateOf(0f) }
    var yValue by remember { mutableStateOf(0f) }
    var zValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val gyroscope = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                event?.let {
                    xValue = it.values[0]
                    yValue = it.values[1]
                    zValue = it.values[2]
                }
            }
            
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, gyroscope, android.hardware.SensorManager.SENSOR_DELAY_UI)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyroscope Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.RotateRight,
                contentDescription = "Gyroscope",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF8B5CF6)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Rotate your device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))
            
            // X Axis
            AxisDisplay("X Rotation", xValue, Color(0xFFEF4444))
            Spacer(Modifier.height(16.dp))
            
            // Y Axis
            AxisDisplay("Y Rotation", yValue, Color(0xFF10B981))
            Spacer(Modifier.height(16.dp))
            
            // Z Axis
            AxisDisplay("Z Rotation", zValue, Color(0xFF8B5CF6))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagnetometerTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var xValue by remember { mutableStateOf(0f) }
    var yValue by remember { mutableStateOf(0f) }
    var zValue by remember { mutableStateOf(0f) }
    var magnitude by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val magnetometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                event?.let {
                    xValue = it.values[0]
                    yValue = it.values[1]
                    zValue = it.values[2]
                    magnitude = kotlin.math.sqrt(xValue * xValue + yValue * yValue + zValue * zValue)
                }
            }
            
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, magnetometer, android.hardware.SensorManager.SENSOR_DELAY_UI)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Magnetometer Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Explore,
                contentDescription = "Magnetometer",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Magnetic Field Strength",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))
            
            // X Axis
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("X Axis", fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.2f µT", xValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            
            // Y Axis
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Y Axis", fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.2f µT", yValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            
            // Z Axis
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Z Axis", fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.2f µT", zValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1890FF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            
            // Magnitude
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Magnitude", fontWeight = FontWeight.Bold)
                    Text(
                        String.format("%.2f µT", magnitude),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1890FF)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTGTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasOTGSupport by remember { mutableStateOf(false) }
    var connectedDevices by remember { mutableStateOf(0) }
    var deviceNames by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Function to update device list
    fun updateDeviceList() {
        try {
            val usbManager = context.getSystemService(android.content.Context.USB_SERVICE) as android.hardware.usb.UsbManager
            val devices = usbManager.deviceList
            connectedDevices = devices.size
            deviceNames = devices.values.map { device ->
                device.productName ?: device.deviceName ?: "Unknown Device"
            }
        } catch (e: Exception) {
            connectedDevices = 0
            deviceNames = emptyList()
        }
    }
    
    DisposableEffect(context) {
        val packageManager = context.packageManager
        hasOTGSupport = packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_USB_HOST)
        
        // Initial device list
        updateDeviceList()
        
        // Listen for USB device attach/detach
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED,
                    android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        updateDeviceList()
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(receiver, filter)
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USB OTG Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onResult(false)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, "Failed")
                    Spacer(Modifier.width(8.dp))
                    Text("Failed")
                }
                Button(
                    onClick = {
                        onResult(true)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, "Passed")
                    Spacer(Modifier.width(8.dp))
                    Text("Passed")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Usb,
                contentDescription = "USB OTG",
                modifier = Modifier.size(120.dp),
                tint = if (hasOTGSupport) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (hasOTGSupport) "✅ OTG Supported" else "❌ OTG Not Supported",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasOTGSupport) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (!hasOTGSupport) {
                Text(
                    "This device does not support USB OTG",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (connectedDevices > 0) Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Connected Devices", fontWeight = FontWeight.Bold)
                    Text(
                        connectedDevices.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (connectedDevices > 0) Color(0xFF10B981) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (connectedDevices > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Device List:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        deviceNames.forEach { name ->
                            Text(
                                "• $name",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    if (hasOTGSupport) "Connect a USB device via OTG adapter" else "No USB devices connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

