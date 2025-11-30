package com.moshitech.workmate.feature.applock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    navController: NavController,
    viewModel: AppLockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("App Lock", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (state) {
                AppLockState.DASHBOARD -> DashboardView(viewModel, isDark, textColor)
                else -> PinView(viewModel, isDark, textColor)
            }
        }
    }
}

@Composable
fun DashboardView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val installedApps by viewModel.installedApps.collectAsState()
    val lockedApps by viewModel.lockedApps.collectAsState()
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Service Toggle Card
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("App Lock Service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                    Text(if (isServiceEnabled) "Active" else "Inactive", style = MaterialTheme.typography.bodySmall, color = if (isServiceEnabled) Color(0xFF10B981) else Color.Gray)
                }
                Switch(checked = isServiceEnabled, onCheckedChange = { viewModel.toggleService(it) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Accessibility Service Setup Guide
        val context = androidx.compose.ui.platform.LocalContext.current
        val isAccessibilityEnabled = remember {
            mutableStateOf(isAccessibilityServiceEnabled(context))
        }
        
        // Auto-refresh when returning to this screen
        androidx.compose.runtime.DisposableEffect(Unit) {
            val listener = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
                }
            }
            val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
            lifecycle?.addObserver(listener)
            
            onDispose {
                lifecycle?.removeObserver(listener)
            }
        }
        
        if (!isAccessibilityEnabled.value && lockedApps.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "To lock apps, you need to enable the Accessibility Service:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF78350F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "1. Tap the button below\n2. Find 'Workmate App Lock'\n3. Toggle it ON\n4. Accept the permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Accessibility Settings")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Select Apps to Lock", style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(installedApps) { app ->
                AppItem(app, lockedApps.contains(app.packageName), isDark, textColor) {
                    viewModel.toggleAppLock(app.packageName)
                }
            }
        }
    }
}

// Helper function to check if accessibility service is enabled
private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val accessibilityManager = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    val enabledServices = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(context.packageName) == true
}

@Composable
fun AppItem(app: AppInfo, isLocked: Boolean, isDark: Boolean, textColor: Color, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(app.name, modifier = Modifier.weight(1f), color = textColor, style = MaterialTheme.typography.bodyLarge)
        Icon(
            if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = null,
            tint = if (isLocked) Color(0xFFEF4444) else Color.Gray
        )
    }
}

@Composable
fun PinView(viewModel: AppLockViewModel, isDark: Boolean, textColor: Color) {
    val pin by viewModel.pin.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.headlineSmall, color = textColor)
        if (error != null) {
            Text(error!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Pin Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < pin.length) textColor else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Number Pad
        val numbers = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        numbers.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(80.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(if (isDark) Color(0xFF1E293B) else Color.White, CircleShape)
                                .clickable {
                                    if (digit == "⌫") viewModel.onDeleteDigit() else viewModel.onPinDigit(digit)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(digit, style = MaterialTheme.typography.headlineMedium, color = textColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
