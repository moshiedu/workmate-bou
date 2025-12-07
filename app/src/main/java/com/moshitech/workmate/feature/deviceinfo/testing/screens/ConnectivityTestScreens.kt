package com.moshitech.workmate.feature.deviceinfo.testing.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = bluetoothManager.adapter
    
    var isEnabled by remember { mutableStateOf(adapter?.isEnabled == true) }
    var scanResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    // Receiver for Bluetooth events
    val receiver = remember {
        object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: android.content.Intent) {
                when (intent.action) {
                    android.bluetooth.BluetoothDevice.ACTION_FOUND -> {
                        val device: android.bluetooth.BluetoothDevice? =
                            intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            try {
                                val name = it.name ?: "Unknown Device"
                                val address = it.address
                                val entry = "$name ($address)"
                                if (!scanResults.contains(entry)) {
                                    scanResults = scanResults + entry
                                }
                            } catch (e: SecurityException) {
                                // Permission might be revoked during scan
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        isScanning = false
                    }
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        isEnabled = state == BluetoothAdapter.STATE_ON
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = android.content.IntentFilter().apply {
            addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                if (isScanning && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    adapter?.cancelDiscovery()
                }
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore unregister errors
            }
        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result.values.all { it }
    }
    
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isEnabled = adapter?.isEnabled == true
    }

    LaunchedEffect(Unit) {
        permissionGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            TestBottomBar(navController, onResult)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bluetooth Status", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                            contentDescription = null,
                            tint = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEnabled) "Enabled" else "Disabled")
                    }
                    if (adapter != null) {
                        Text("Name: ${try { adapter.name } catch(e: SecurityException) { "Unknown" }}")
                        // Suppress lint for testing screen or handle permission properly
                        @SuppressLint("MissingPermission", "HardwareIds")
                        val address = try { adapter.address } catch(e: SecurityException) { "Unknown" }
                        Text("Address: $address")
                    } else {
                        Text("No Bluetooth Adapter found", color = Color.Red)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    if (!isEnabled && adapter != null) {
                        Button(onClick = {
                            val enableBtIntent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBluetoothLauncher.launch(enableBtIntent)
                        }) {
                            Text("Enable Bluetooth")
                        }
                    } else if (isEnabled) {
                         Button(onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings to Disable")
                        }
                    }
                }
            }

            if (!permissionGranted) {
                Button(
                    onClick = { launcher.launch(permissions) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions to Scan")
                }
            } else if (isEnabled) {
                Button(
                    onClick = {
                        if (!isScanning) {
                            scanResults = emptyList()
                            isScanning = true
                            try {
                                adapter?.startDiscovery()
                            } catch (e: SecurityException) {
                                isScanning = false
                            }
                        } else {
                            try {
                                adapter?.cancelDiscovery()
                            } catch (e: SecurityException) {}
                            isScanning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Scanning...")
                    } else {
                        Text("Start Scan")
                    }
                }
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scanResults) { device ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bluetooth, null)
                                Spacer(Modifier.width(12.dp))
                                Text(device)
                            }
                        }
                    }
                    if (scanResults.isEmpty() && !isScanning) {
                        item {
                            Text(
                                "No devices found yet. Tap Scan to start.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    var wifiInfo by remember { mutableStateOf("Loading...") }
    var signalLevel by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while(true) {
            val info = wifiManager.connectionInfo
            val ssid = info.ssid
            val rssi = info.rssi
            val linkSpeed = info.linkSpeed
            val ip = android.text.format.Formatter.formatIpAddress(info.ipAddress)
            
            wifiInfo = """
                SSID: $ssid
                BSSID: ${info.bssid ?: "N/A"}
                RSSI: $rssi dBm
                Link Speed: $linkSpeed Mbps
                IP Address: $ip
                Frequency: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) info.frequency.toString() + " MHz" else "N/A"}
            """.trimIndent()
            
            signalLevel = WifiManager.calculateSignalLevel(rssi, 5)
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wi-Fi Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            TestBottomBar(navController, onResult)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wi-Fi Connection", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (wifiManager.isWifiEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (wifiManager.isWifiEnabled) "Enabled" else "Disabled")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Signal Strength", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height((20 + (index * 10)).dp)
                                    .background(
                                        if (index < signalLevel) Color(0xFF10B981) else Color.LightGray,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(wifiInfo, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    var locationInfo by remember { mutableStateOf("Waiting for location...") }
    var permissionGranted by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result.values.all { it }
    }

    LaunchedEffect(Unit) {
        permissionGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Active Location Updates
    DisposableEffect(permissionGranted) {
        if (permissionGranted) {
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    locationInfo = """
                        Provider: ${location.provider?.uppercase() ?: "UNKNOWN"}
                        Latitude: ${location.latitude}
                        Longitude: ${location.longitude}
                        Accuracy: ${location.accuracy} m
                        Altitude: ${location.altitude} m
                        Speed: ${location.speed} m/s
                        Time: ${java.util.Date(location.time)}
                    """.trimIndent()
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            }

            try {
                // Request updates from both GPS and Network providers
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, listener)
                }
                
                // Also try to get last known location immediately while waiting for updates
                val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val bestLocation = lastGps ?: lastNetwork
                
                if (bestLocation != null && locationInfo == "Waiting for location...") {
                     locationInfo = """
                        Provider: ${bestLocation.provider?.uppercase() ?: "UNKNOWN"} (Last Known)
                        Latitude: ${bestLocation.latitude}
                        Longitude: ${bestLocation.longitude}
                        Accuracy: ${bestLocation.accuracy} m
                        Altitude: ${bestLocation.altitude} m
                        Speed: ${bestLocation.speed} m/s
                        Time: ${java.util.Date(bestLocation.time)}
                    """.trimIndent()
                }
            } catch (e: SecurityException) {
                locationInfo = "Permission denied: ${e.message}"
            }

            onDispose {
                locationManager.removeUpdates(listener)
            }
        } else {
            onDispose { }
        }
    }

    // Refresh status on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Re-check Location status
                // Force recomposition by updating a dummy state or just relying on the fact that
                // we are reading providers in the UI directly if we used state.
                // Since we used variables in the UI, we need to update a state.
                // Actually, the UI reads `locationManager.isProviderEnabled` directly in the composition.
                // To force recomposition, we can toggle a state.
                // Better: Use a state for enabled status.
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // We need to use state for the UI to update
    var isGpsEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }
    var isNetworkEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            TestBottomBar(navController, onResult)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!permissionGranted) {
                Button(
                    onClick = { 
                        launcher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Location Permission")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GPS Status", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isGpsEnabled) Icons.Default.LocationOn else Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = if (isGpsEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("GPS Provider: ${if (isGpsEnabled) "Enabled" else "Disabled"}")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Network Provider: ${if (isNetworkEnabled) "Enabled" else "Disabled"}")
                    
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }) {
                        Text(if (isGpsEnabled) "Open Settings to Disable" else "Open Settings to Enable")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Location Data", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(locationInfo)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTestScreen(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    
    var nfcStatus by remember { mutableStateOf("Checking...") }
    var tagInfo by remember { mutableStateOf<String?>(null) }
    var scanCount by remember { mutableStateOf(0) }

    // Refresh status on resume (e.g. returning from Settings)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Re-check NFC status
                if (nfcAdapter != null) {
                    // Force re-composition/check
                    val isEnabled = nfcAdapter.isEnabled
                    nfcStatus = if (isEnabled) "Ready to Scan" else "NFC is Disabled"
                    
                    // Re-enable reader mode if needed
                    val activity = context as? android.app.Activity ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
                    if (isEnabled && activity != null) {
                        val flags = NfcAdapter.FLAG_READER_NFC_A or
                                NfcAdapter.FLAG_READER_NFC_B or
                                NfcAdapter.FLAG_READER_NFC_F or
                                NfcAdapter.FLAG_READER_NFC_V or
                                NfcAdapter.FLAG_READER_NFC_BARCODE or
                                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
                        
                        // We need to define callback again or move it out. 
                        // For simplicity, we'll rely on the main DisposableEffect to handle ReaderMode
                        // But we trigger a state change to ensure UI updates
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ... (Main DisposableEffect for ReaderMode) ...
    DisposableEffect(nfcAdapter?.isEnabled) { // React to enabled state changes
        val activity = context as? android.app.Activity ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
        
        if (nfcAdapter != null && activity != null && nfcAdapter.isEnabled) {
            val flags = NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

            val callback = NfcAdapter.ReaderCallback { tag ->
                // Play sound manually since we disabled platform sounds to control feedback
                try {
                    val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                    val r = android.media.RingtoneManager.getRingtone(context, notification)
                    r.play()
                } catch (e: Exception) {
                    // Ignore sound error
                }

                val id = tag.id.joinToString(":") { "%02X".format(it) }
                val techList = tag.techList.joinToString("\n") { it.substringAfterLast(".") }
                
                tagInfo = """
                    Tag Detected!
                    ID: $id
                    
                    Technologies:
                    $techList
                """.trimIndent()
                scanCount++
            }

            try {
                nfcAdapter.enableReaderMode(activity, callback, flags, null)
                nfcStatus = "Ready to Scan"
            } catch (e: Exception) {
                nfcStatus = "Error enabling NFC reader: ${e.message}"
            }
        } else {
            if (nfcAdapter != null && !nfcAdapter.isEnabled) {
                 nfcStatus = "NFC is Disabled"
            }
        }

        onDispose {
            activity?.let {
                try {
                    nfcAdapter?.disableReaderMode(it)
                } catch (e: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Test") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            TestBottomBar(navController, onResult)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("NFC Adapter Status", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    if (nfcAdapter == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = Color(0xFFEF4444))
                            Spacer(Modifier.width(8.dp))
                            Text("NFC not supported on this device")
                        }
                    } else {
                        val isEnabled = nfcAdapter.isEnabled // Read property directly for UI
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isEnabled) Icons.Default.Nfc else Icons.Default.Nfc,
                                contentDescription = null,
                                tint = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isEnabled) "Enabled" else "Disabled")
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                            context.startActivity(intent)
                        }) {
                            Text(if (isEnabled) "Open Settings to Disable" else "Open Settings to Enable")
                        }
                    }
                }
            }
            
            if (nfcAdapter != null && nfcAdapter.isEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Scan Instructions", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Spacer(Modifier.height(8.dp))
                        Text("Hold an NFC tag, credit card, or another phone against the back of this device.")
                    }
                }
                
                if (tagInfo != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                                Spacer(Modifier.width(8.dp))
                                Text("Tag Read Successfully ($scanCount)", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(tagInfo ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestBottomBar(
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
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
