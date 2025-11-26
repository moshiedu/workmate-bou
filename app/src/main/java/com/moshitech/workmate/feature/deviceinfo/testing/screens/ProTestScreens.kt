package com.moshitech.workmate.feature.deviceinfo.testing.screens

import android.Manifest
import android.hardware.fingerprint.FingerprintManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasHardware by remember { mutableStateOf(false) }
    var hasEnrolled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ use BiometricManager
                val biometricManager = androidx.biometric.BiometricManager.from(context)
                hasHardware = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) != androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
                hasEnrolled = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6-8 use FingerprintManager
                @Suppress("DEPRECATION")
                val fingerprintManager = context.getSystemService(android.content.Context.FINGERPRINT_SERVICE) as FingerprintManager
                hasHardware = fingerprintManager.isHardwareDetected
                hasEnrolled = fingerprintManager.hasEnrolledFingerprints()
            }
        } catch (e: Exception) {
            hasHardware = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fingerprint Test") },
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
                Icons.Default.Fingerprint,
                contentDescription = "Fingerprint",
                modifier = Modifier.size(120.dp),
                tint = if (hasHardware && hasEnrolled) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (hasHardware) "Hardware Detected" else "No Hardware",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasHardware) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (hasHardware) {
                Text(
                    if (hasEnrolled) "Fingerprints Enrolled" else "No Fingerprints Enrolled",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasEnrolled) Color(0xFF10B981) else Color.Gray
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hardware", fontWeight = FontWeight.Medium)
                        Icon(
                            if (hasHardware) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (hasHardware) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enrolled", fontWeight = FontWeight.Medium)
                        Icon(
                            if (hasEnrolled) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (hasEnrolled) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isCharging by remember { mutableStateOf(false) }
    var chargingType by remember { mutableStateOf("Not Charging") }
    var batteryLevel by remember { mutableStateOf(0) }
    
    DisposableEffect(Unit) {
        val intentFilter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_POWER_CONNECTED)
            addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
            addAction(android.content.Intent.ACTION_BATTERY_CHANGED)
        }
        
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                intent?.let {
                    val status = it.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == android.os.BatteryManager.BATTERY_STATUS_FULL
                    
                    batteryLevel = it.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0)
                    
                    chargingType = when {
                        !isCharging -> "Not Charging"
                        status == android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                        status == android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        else -> "Unknown"
                    }
                }
            }
        }
        
        context.registerReceiver(receiver, intentFilter)
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Charging Test") },
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
                Icons.Default.BatteryChargingFull,
                contentDescription = "Charging",
                modifier = Modifier.size(120.dp),
                tint = if (isCharging) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (isCharging) "CHARGING" else "NOT CHARGING",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (isCharging) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "$batteryLevel%",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status", fontWeight = FontWeight.Medium)
                        Text(chargingType, color = if (isCharging) Color(0xFF10B981) else Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Battery Level", fontWeight = FontWeight.Medium)
                        Text("$batteryLevel%", color = Color(0xFF1890FF))
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Connect charger to test",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadsetTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
            isConnected = audioManager.isWiredHeadsetOn
        } catch (e: Exception) {
            isConnected = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Headset Test") },
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
                Icons.Default.Headset,
                contentDescription = "Headset",
                modifier = Modifier.size(120.dp),
                tint = if (isConnected) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                if (isConnected) "CONNECTED" else "NOT CONNECTED",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isConnected) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ℹ️ Note:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "• Many modern phones don't have 3.5mm headphone jacks",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• USB-C/Lightning headsets may not be detected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Bluetooth headsets are not detected by this test",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                if (isConnected) 
                    "Wired headset detected!" 
                else 
                    "Plug in a 3.5mm wired headset (if supported)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isConnected) Color(0xFF10B981) else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarpieceTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earpiece Test") },
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
                Icons.Default.PhoneInTalk,
                contentDescription = "Earpiece",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Earpiece Test",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Play a test tone through the earpiece",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = {
                    isPlaying = !isPlaying
                    if (isPlaying) {
                        try {
                            val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
                            audioManager.mode = AudioManager.MODE_IN_CALL
                            audioManager.isSpeakerphoneOn = false
                            
                            val toneGen = android.media.ToneGenerator(
                                AudioManager.STREAM_VOICE_CALL,
                                80
                            )
                            toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_0, 1000)
                            
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                audioManager.mode = AudioManager.MODE_NORMAL
                                isPlaying = false
                            }, 1100)
                        } catch (e: Exception) {
                            isPlaying = false
                        }
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Hold phone to your ear to hear the tone",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
