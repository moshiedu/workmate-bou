package com.moshitech.workmate.feature.deviceinfo.screens

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrityCheckScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val context = LocalContext.current
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    var isChecking by remember { mutableStateOf(true) }
    var rootDetected by remember { mutableStateOf(false) }
    var emulatorDetected by remember { mutableStateOf(false) }
    var debugDetected by remember { mutableStateOf(false) }
    var overallStatus by remember { mutableStateOf(true) } // True = Good, False = Bad

    LaunchedEffect(Unit) {
        // Simulate scanning delay for effect
        delay(1500)
        
        rootDetected = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
        emulatorDetected = checkEmulator()
        debugDetected = checkDebug(context)
        
        overallStatus = !rootDetected && !emulatorDetected
        isChecking = false
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Integrity Check", fontWeight = FontWeight.Bold, color = textColor)
                        Text("Pro Feature", fontSize = 12.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Status Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        if (isChecking) Color(0xFF3B82F6).copy(alpha = 0.1f)
                        else if (overallStatus) Color(0xFF10B981).copy(alpha = 0.1f)
                        else Color(0xFFEF4444).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color(0xFF3B82F6),
                        strokeWidth = 4.dp
                    )
                } else {
                    Icon(
                        imageVector = if (overallStatus) Icons.Default.VerifiedUser else Icons.Default.GppBad,
                        contentDescription = null,
                        tint = if (overallStatus) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isChecking) "Scanning Device..." 
                       else if (overallStatus) "Device Integrity Verified" 
                       else "Integrity Issues Detected",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isChecking) "Checking system for root access, emulator environment, and security risks."
                       else if (overallStatus) "Your device appears to be secure and unmodified."
                       else "Your device shows signs of modification or is running in an insecure environment.",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Results List
            if (!isChecking) {
                IntegrityResultItem(
                    title = "Root Access",
                    description = if (rootDetected) "Root binaries found (su)" else "No root access detected",
                    passed = !rootDetected,
                    icon = Icons.Default.Lock,
                    cardColor = cardColor,
                    textColor = textColor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                IntegrityResultItem(
                    title = "Emulator Check",
                    description = if (emulatorDetected) "Running on an emulator" else "Running on physical device",
                    passed = !emulatorDetected,
                    icon = Icons.Default.Smartphone,
                    cardColor = cardColor,
                    textColor = textColor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                IntegrityResultItem(
                    title = "USB Debugging",
                    description = if (debugDetected) "USB Debugging is enabled" else "USB Debugging is disabled",
                    passed = !debugDetected, // Debugging isn't necessarily a fail, but a warning
                    isWarning = debugDetected,
                    icon = Icons.Default.BugReport,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
fun IntegrityResultItem(
    title: String,
    description: String,
    passed: Boolean,
    isWarning: Boolean = false,
    icon: ImageVector,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (passed) Color(0xFF10B981).copy(alpha = 0.1f)
                        else if (isWarning) Color(0xFFF59E0B).copy(alpha = 0.1f)
                        else Color(0xFFEF4444).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (passed) Color(0xFF10B981) 
                           else if (isWarning) Color(0xFFF59E0B) 
                           else Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle 
                              else if (isWarning) Icons.Default.Warning 
                              else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (passed) Color(0xFF10B981) 
                       else if (isWarning) Color(0xFFF59E0B) 
                       else Color(0xFFEF4444)
            )
        }
    }
}

// --- Checks ---

private fun checkRootMethod1(): Boolean {
    val buildTags = Build.TAGS
    return buildTags != null && buildTags.contains("test-keys")
}

private fun checkRootMethod2(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )
    return paths.any { File(it).exists() }
}

private fun checkRootMethod3(): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val input = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
        input.readLine() != null
    } catch (t: Throwable) {
        false
    } finally {
        process?.destroy()
    }
}

private fun checkEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT)
}

private fun checkDebug(context: Context): Boolean {
    return try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    } catch (e: Exception) {
        false
    }
}
