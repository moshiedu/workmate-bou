package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
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
    
    // Use ViewModel
    val viewModel: com.moshitech.workmate.feature.deviceinfo.viewmodel.TestsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val tests by viewModel.uiState.collectAsState()
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    
    // Handle test results from interactive screens
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val testResultState = savedStateHandle?.getStateFlow<Pair<String, Boolean>?>("test_result", null)?.collectAsState()
    val testResult = testResultState?.value

    LaunchedEffect(testResult) {
        testResult?.let { (id, passed) ->
            viewModel.updateTestStatus(id, passed)
            // Clear the result to avoid re-processing
            savedStateHandle?.remove<Pair<String, Boolean>>("test_result")
        }
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
                        viewModel.resetAllTests()
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            items(tests) { test ->
                TestItem(
                    test = test,
                    cardColor = cardColor,
                    textColor = textColor,
                    testExecutor = testExecutor,
                    navController = navController,
                    onStatusChange = { /* Handled by ViewModel via navigation result */ }
                )
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val route = when (test.id) {
                        "backlight" -> "test_backlight"
                        "display" -> "test_display"
                        "multitouch" -> "test_multitouch"
                        "buttons" -> "test_button"
                        "speakers" -> "test_speaker"
                        "microphone" -> "test_microphone"
                        "proximity" -> "test_proximity"
                        "light" -> "test_light_sensor"
                        "light_sensor" -> "test_light_sensor"
                        "accelerometer" -> "test_accelerometer"
                        "gyroscope" -> "test_gyroscope"
                        "magnetometer" -> "test_magnetometer"
                        "flashlight" -> "test_flashlight"
                        "vibration" -> "test_vibration"
                        "charging" -> "test_charging"
                        "headset" -> "test_headset"
                        "earpiece" -> "test_earpiece"
                        "fingerprint" -> "test_fingerprint"
                        "bluetooth" -> "test_bluetooth"
                        "wifi" -> "test_wifi"
                        "gps" -> "test_gps"
                        "nfc" -> "test_nfc"
                        "usb" -> "test_otg"
                        else -> null
                    }
                    route?.let { navController.navigate(it) }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getTestIcon(test.id),
                    contentDescription = test.name,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = test.name,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    if (test.isPro) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0xFFFF9800), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            
            StatusButton(test.status)
        }
    }
}

@Composable
fun StatusButton(status: TestStatus) {
    Surface(
        shape = CircleShape,
        color = when (status) {
            TestStatus.PASSED -> Color(0xFF10B981)
            TestStatus.FAILED -> Color(0xFFEF4444)
            TestStatus.UNTESTED -> Color(0xFF9CA3AF)
        },
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (status) {
                    TestStatus.PASSED -> Icons.Default.Check
                    TestStatus.FAILED -> Icons.Default.Close
                    TestStatus.UNTESTED -> Icons.Default.HelpOutline
                },
                contentDescription = status.name,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
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
        "bluetooth" -> Icons.Default.Bluetooth
        "wifi" -> Icons.Default.Wifi
        "gps" -> Icons.Default.LocationOn
        "nfc" -> Icons.Default.Nfc
        "usb" -> Icons.Default.Usb
        else -> Icons.Default.HelpOutline
    }
}
