package com.moshitech.workmate.feature.deviceinfo.testing.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Usb
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
fun UsbTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isUsbConnected by remember { mutableStateOf(false) }
    var usbStatusText by remember { mutableStateOf("Waiting for USB connection...") }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val isUsb = plugged == BatteryManager.BATTERY_PLUGGED_USB
                    val isAc = plugged == BatteryManager.BATTERY_PLUGGED_AC
                    
                    if (isUsb) {
                        isUsbConnected = true
                        usbStatusText = "✅ USB Connected!"
                    } else if (isAc) {
                        isUsbConnected = true
                        usbStatusText = "✅ Connected to AC Charger"
                    } else {
                        isUsbConnected = false
                        usbStatusText = "❌ Please connect a USB cable"
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USB Test") },
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
                imageVector = Icons.Default.Usb,
                contentDescription = "USB",
                modifier = Modifier.size(80.dp),
                tint = if (isUsbConnected) Color(0xFF10B981) else Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "USB Port Test",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Connect a USB cable to your device",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                usbStatusText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isUsbConnected) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}
