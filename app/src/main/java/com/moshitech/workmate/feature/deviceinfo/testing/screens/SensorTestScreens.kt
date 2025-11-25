package com.moshitech.workmate.feature.deviceinfo.testing.screens

import android.Manifest
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var hasPermission by remember { 
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Microphone Test") },
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
            if (!hasPermission) {
                Text(
                    "Microphone permission required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Grant Permission")
                }
            } else {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(120.dp),
                    tint = if (isRecording) Color(0xFFEF4444) else Color(0xFF1890FF)
                )
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    if (isRecording) "Recording..." else "Ready to record",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Color(0xFFEF4444) else Color.Black
                )
                
                Spacer(Modifier.height(48.dp))
                
                Button(
                    onClick = {
                        if (isRecording) {
                            // Stop recording
                            try {
                                recorder?.stop()
                                recorder?.release()
                                recorder = null
                                isRecording = false
                            } catch (e: Exception) {
                                // Ignore
                            }
                        } else {
                            // Start recording
                            try {
                                recordingFile = File(context.cacheDir, "test_recording.3gp")
                                recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }.apply {
                                    setAudioSource(MediaRecorder.AudioSource.MIC)
                                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                    setOutputFile(recordingFile?.absolutePath)
                                    prepare()
                                    start()
                                }
                                isRecording = true
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isRecording) "Stop" else "Record",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Tap to ${if (isRecording) "stop" else "start"} recording",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try {
                recorder?.release()
                recordingFile?.delete()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProximityTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var proximityValue by remember { mutableStateOf(0f) }
    var maxValue by remember { mutableStateOf(0f) }
    var isNear by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_PROXIMITY)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                event?.let {
                    proximityValue = it.values[0]
                    maxValue = it.sensor.maximumRange
                    isNear = proximityValue < maxValue
                }
            }
            
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, proximitySensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proximity Sensor Test") },
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
                Icons.Default.Sensors,
                contentDescription = "Proximity",
                modifier = Modifier.size(120.dp),
                tint = if (isNear) Color(0xFF10B981) else Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (isNear) "NEAR" else "FAR",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (isNear) Color(0xFF10B981) else Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Distance: ${proximityValue.toInt()} cm",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                "Cover the proximity sensor at the top of your device",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightSensorTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var lightValue by remember { mutableStateOf(0f) }
    var maxValue by remember { mutableStateOf(0f) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                try {
                    event?.let {
                        if (it.values.isNotEmpty()) {
                            lightValue = it.values[0]
                            maxValue = it.sensor?.maximumRange ?: 10000f
                        }
                    }
                } catch (e: Exception) {
                    // Ignore sensor errors
                }
            }
            
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        
        if (lightSensor != null) {
            sensorManager.registerListener(listener, lightSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            try {
                sensorManager?.unregisterListener(listener)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Light Sensor Test") },
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
                Icons.Default.LightMode,
                contentDescription = "Light",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFFA500)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "${lightValue.toInt()} lux",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { (lightValue / maxValue).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFFFFA500)
            )
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                "Cover or expose the light sensor",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Max: ${maxValue.toInt()} lux",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
