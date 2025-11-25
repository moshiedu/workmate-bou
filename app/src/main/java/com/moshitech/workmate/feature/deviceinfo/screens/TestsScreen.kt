package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.data.models.HardwareTest
import com.moshitech.workmate.feature.deviceinfo.data.models.TestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    navController: NavController,
    isDark: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val testExecutor = remember { com.moshitech.workmate.feature.deviceinfo.testing.HardwareTestExecutor(context) }
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    
    var tests by remember {
        mutableStateOf(
            listOf(
                HardwareTest("flashlight", "Flashlight", "flashlight"),
                HardwareTest("vibration", "Vibration", "vibration"),
                HardwareTest("buttons", "Buttons", "buttons"),
                HardwareTest("multitouch", "Multitouch", "multitouch"),
                HardwareTest("display", "Display", "display"),
                HardwareTest("backlight", "Backlight", "backlight"),
                HardwareTest("light_sensor", "Light sensor", "light"),
                HardwareTest("proximity", "Proximity", "proximity"),
                HardwareTest("accelerometer", "Accelerometer", "accelerometer"),
                HardwareTest("charging", "Charging", "charging", isPro = true),
                HardwareTest("speakers", "Speakers", "speakers", isPro = true),
                HardwareTest("headset", "Headset", "headset", isPro = true),
                HardwareTest("earpiece", "Earpiece", "earpiece", isPro = true),
                HardwareTest("microphone", "Microphone", "microphone", isPro = true),
                HardwareTest("fingerprint", "Fingerprint", "fingerprint", isPro = true)
            )
        )
    }
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Tests", fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textColor)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        tests = tests.map { it.copy(status = TestStatus.UNTESTED) }
                    }) {
                        Text("RESET", color = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            items(tests) { test ->
                TestItem(
                    test = test,
                    cardColor = cardColor,
                    textColor = textColor,
                    testExecutor = testExecutor,
                    navController = navController,
                    onStatusChange = { newStatus ->
                        tests = tests.map {
                            if (it.id == test.id) it.copy(status = newStatus) else it
                        }
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TestItem(
    test: HardwareTest,
    cardColor: Color,
    textColor: Color,
    testExecutor: com.moshitech.workmate.feature.deviceinfo.testing.HardwareTestExecutor,
    navController: NavController,
    onStatusChange: (TestStatus) -> Unit
) {
    var isTestingInProgress by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isTestingInProgress) {
                // Navigate to interactive test screen
                val route = when (test.id) {
                    "flashlight" -> "test_flashlight"
                    "vibration" -> "test_vibration"
                    "buttons" -> "test_button"
                    "multitouch" -> "test_multitouch"
                    "display" -> "test_display"
                    "backlight" -> "test_backlight"
                    "light_sensor" -> "test_light_sensor"
                    "proximity" -> "test_proximity"
                    "accelerometer" -> "test_accelerometer"
                    "speakers" -> "test_speaker"
                    "microphone" -> "test_microphone"
                    "charging" -> "test_charging"
                    "headset" -> "test_headset"
                    "earpiece" -> "test_earpiece"
                    "fingerprint" -> "test_fingerprint"
                    else -> null
                }
                route?.let { navController.navigate(it) }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getTestIcon(test.icon),
                    contentDescription = test.name,
                    tint = if (test.isPro) Color(0xFF10B981) else textColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (test.isPro) {
                            Text(
                                "PRO",
                                color = Color(0xFF10B981),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            test.name,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Status button
            Surface(
                shape = CircleShape,
                color = when {
                    isTestingInProgress -> Color(0xFF3B82F6) // Blue when testing
                    test.status == TestStatus.PASSED -> Color(0xFF10B981)
                    test.status == TestStatus.FAILED -> Color(0xFFEF4444)
                    else -> Color(0xFF9CA3AF)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isTestingInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = when (test.status) {
                                TestStatus.PASSED -> Icons.Default.Check
                                TestStatus.FAILED -> Icons.Default.Close
                                TestStatus.UNTESTED -> Icons.Default.HelpOutline
                            },
                            contentDescription = test.status.name,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getTestIcon(iconName: String): ImageVector {
    return when (iconName) {
        "flashlight" -> Icons.Default.FlashlightOn
        "vibration" -> Icons.Default.Vibration
        "buttons" -> Icons.Default.SmartButton
        "multitouch" -> Icons.Default.TouchApp
        "display" -> Icons.Default.PhoneAndroid
        "backlight" -> Icons.Default.Brightness7
        "light" -> Icons.Default.LightMode
        "proximity" -> Icons.Default.Sensors
        "accelerometer" -> Icons.Default.ScreenRotation
        "charging" -> Icons.Default.BatteryChargingFull
        "speakers" -> Icons.Default.VolumeUp
        "headset" -> Icons.Default.Headset
        "earpiece" -> Icons.Default.PhoneInTalk
        "microphone" -> Icons.Default.Mic
        "fingerprint" -> Icons.Default.Fingerprint
        else -> Icons.Default.HelpOutline
    }
}
