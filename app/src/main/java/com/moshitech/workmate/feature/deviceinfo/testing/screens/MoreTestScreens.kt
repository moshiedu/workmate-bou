package com.moshitech.workmate.feature.deviceinfo.testing.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultitouchTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    var touchPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var maxTouches by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multitouch Test") },
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
        ) {
            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1890FF).copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Text(
                    "Touch the screen with multiple fingers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current touches: ${touchPoints.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1890FF)
                )
                Text(
                    "Max touches detected: $maxTouches",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            // Touch canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        forEachGesture {
                            awaitPointerEventScope {
                                do {
                                    val event = awaitPointerEvent()
                                    touchPoints = event.changes.map { it.position }
                                    if (touchPoints.size > maxTouches) {
                                        maxTouches = touchPoints.size
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    touchPoints.forEach { point ->
                        drawCircle(
                            color = Color(0xFF1890FF),
                            radius = 50f,
                            center = point,
                            alpha = 0.5f
                        )
                        drawCircle(
                            color = Color(0xFF1890FF),
                            radius = 30f,
                            center = point
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var volumeUpPressed by remember { mutableStateOf(false) }
    var volumeDownPressed by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Get system services
    val vibrator = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
    }
    
    val audioManager = remember {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    }
    
    fun vibrateAndShowVolume() {
        // Vibrate
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
        
        // Show volume level
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "Volume Level: $currentVolume / $maxVolume",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Intercept keys using Window Callback
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val originalCallback = activity?.window?.callback
        
        if (activity != null && originalCallback != null) {
            activity.window.callback = object : android.view.Window.Callback by originalCallback {
                override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
                    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                        when (event.keyCode) {
                            android.view.KeyEvent.KEYCODE_VOLUME_UP -> {
                                volumeUpPressed = true
                                vibrateAndShowVolume()
                                return true // Consume event
                            }
                            android.view.KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                volumeDownPressed = true
                                vibrateAndShowVolume()
                                return true // Consume event
                            }
                        }
                    }
                    return originalCallback.dispatchKeyEvent(event)
                }
            }
        }
        
        onDispose {
            if (activity != null && originalCallback != null) {
                activity.window.callback = originalCallback
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Button Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Press the hardware buttons",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Volume Up
                ButtonIndicator(
                    label = "Volume Up",
                    icon = Icons.Default.VolumeUp,
                    isPressed = volumeUpPressed
                )
                
                // Volume Down
                ButtonIndicator(
                    label = "Volume Down",
                    icon = Icons.Default.VolumeDown,
                    isPressed = volumeDownPressed
                )
                
                Spacer(Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ Limitation:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF856404)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Hardware button detection is very limited on Android. Volume buttons can only be detected when the app is in focus, and the Power button cannot be detected at all due to system restrictions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonIndicator(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPressed: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) Color(0xFF10B981) else Color(0xFFF3F4F6)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isPressed) Color.White else Color.Gray
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    label,
                    color = if (isPressed) Color.White else Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
            if (isPressed) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Pressed",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    var volume by remember { mutableStateOf(0.5f) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speaker Test") },
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
                Icons.Default.VolumeUp,
                contentDescription = "Speaker",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Volume: ${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1890FF)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Slider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isPlaying = !isPlaying
                    if (isPlaying) {
                        // Play test tone
                        try {
                            val toneGen = android.media.ToneGenerator(
                                android.media.AudioManager.STREAM_MUSIC,
                                (volume * 100).toInt()
                            )
                            toneGen.startTone(android.media.ToneGenerator.TONE_DTMF_0, 1000)
                        } catch (e: Exception) {
                            // Ignore
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
        }
    }
}
