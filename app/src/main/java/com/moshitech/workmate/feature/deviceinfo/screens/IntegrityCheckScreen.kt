package com.moshitech.workmate.feature.deviceinfo.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.moshitech.workmate.feature.deviceinfo.model.SecurityCheck
import com.moshitech.workmate.feature.deviceinfo.model.SecuritySeverity
import com.moshitech.workmate.feature.deviceinfo.viewmodel.IntegrityCheckViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrityCheckScreen(
    navController: NavController,
    isDark: Boolean = true
) {
    val viewModel: IntegrityCheckViewModel = viewModel()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    var showScheduleSheet by remember { mutableStateOf(false) }
    var scheduleRefreshTrigger by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    // Schedule Bottom Sheet
    if (showScheduleSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showScheduleSheet = false
                scheduleRefreshTrigger++
            },
            containerColor = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Scan Schedule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Daily Option
                ScheduleOption(
                    title = "Daily",
                    description = "Scan every 24 hours",
                    icon = Icons.Default.Today,
                    onClick = {
                        com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.scheduleScan(
                            context,
                            com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.ScanFrequency.DAILY
                        )
                        showScheduleSheet = false
                        scheduleRefreshTrigger++
                    },
                    cardColor = backgroundColor,
                    textColor = textColor,
                    isSelected = getCurrentScheduleFrequency(context) == "DAILY"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Weekly Option
                ScheduleOption(
                    title = "Weekly",
                    description = "Scan every 7 days",
                    icon = Icons.Default.DateRange,
                    onClick = {
                        com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.scheduleScan(
                            context,
                            com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.ScanFrequency.WEEKLY
                        )
                        showScheduleSheet = false
                        scheduleRefreshTrigger++
                    },
                    cardColor = backgroundColor,
                    textColor = textColor,
                    isSelected = getCurrentScheduleFrequency(context) == "WEEKLY"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Monthly Option
                ScheduleOption(
                    title = "Monthly",
                    description = "Scan every 30 days",
                    icon = Icons.Default.CalendarMonth,
                    onClick = {
                        com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.scheduleScan(
                            context,
                            com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.ScanFrequency.MONTHLY
                        )
                        showScheduleSheet = false
                        scheduleRefreshTrigger++
                    },
                    cardColor = backgroundColor,
                    textColor = textColor,
                    isSelected = getCurrentScheduleFrequency(context) == "MONTHLY"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Disable Option
                ScheduleOption(
                    title = "Disable",
                    description = "Turn off scheduled scans",
                    icon = Icons.Default.NotificationsOff,
                    onClick = {
                        com.moshitech.workmate.feature.deviceinfo.utils.ScanScheduler.cancelScheduledScans(context)
                        showScheduleSheet = false
                        scheduleRefreshTrigger++
                    },
                    cardColor = backgroundColor,
                    textColor = textColor,
                    isSelected = getCurrentScheduleFrequency(context) == "NONE"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Test Button
                Button(
                    onClick = {
                        val request = androidx.work.OneTimeWorkRequestBuilder<com.moshitech.workmate.feature.deviceinfo.workers.SecurityScanWorker>()
                            .build()
                        androidx.work.WorkManager.getInstance(context).enqueue(request)
                        showScheduleSheet = false
                        android.widget.Toast.makeText(context, "Background scan started...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Run Background Scan Now (Test)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
                actions = {
                    if (!isScanning) {
                        IconButton(onClick = { 
                            showScheduleSheet = true
                            scheduleRefreshTrigger++
                        }) {
                            Icon(Icons.Default.Schedule, "Schedule", tint = textColor)
                        }
                        IconButton(onClick = { viewModel.startScan() }) {
                            Icon(Icons.Default.Refresh, "Rescan", tint = textColor)
                        }
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
            if (isScanning) {
                // Scanning State
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color(0xFF3B82F6),
                        strokeWidth = 4.dp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Scanning Device...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Text(
                    text = "Checking system for security risks",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                // Results State
                scanResult?.let { result ->
                    // Security Score Circle
                    SecurityScoreCircle(
                        score = result.score,
                        textColor = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = when {
                            result.score >= 90 -> "Excellent Security"
                            result.score >= 70 -> "Good Security"
                            result.score >= 50 -> "Fair Security"
                            else -> "Security Issues Detected"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            result.score >= 90 -> "Your device has excellent security posture."
                            result.score >= 70 -> "Your device is reasonably secure with minor issues."
                            result.score >= 50 -> "Your device has some security concerns that should be addressed."
                            else -> "Your device has critical security issues that need immediate attention."
                        },
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Schedule Status Card
                    ScheduleStatusCard(
                        cardColor = cardColor,
                        textColor = textColor,
                        onScheduleClick = { 
                            showScheduleSheet = true
                            scheduleRefreshTrigger++
                        },
                        refreshTrigger = scheduleRefreshTrigger
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Security Checks
                    result.checks.forEach { check ->
                        SecurityCheckCard(
                            check = check,
                            cardColor = cardColor,
                            textColor = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleStatusCard(
    cardColor: Color,
    textColor: Color,
    onScheduleClick: () -> Unit,
    refreshTrigger: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val workManager = androidx.work.WorkManager.getInstance(context)
    
    var scheduleInfo by remember { mutableStateOf("Not scheduled") }
    var scheduleFrequency by remember { mutableStateOf("") }
    
    // Refresh when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        try {
            val workInfos = workManager.getWorkInfosForUniqueWork(
                com.moshitech.workmate.feature.deviceinfo.workers.SecurityScanWorker.WORK_NAME
            ).get()
            
            if (workInfos.isNotEmpty() && workInfos[0].state == androidx.work.WorkInfo.State.ENQUEUED) {
                val nextRunTime = workInfos[0].nextScheduleTimeMillis
                
                if (nextRunTime > 0) {
                    // Format the exact date and time
                    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
                    val nextScanDate = java.util.Date(nextRunTime)
                    scheduleInfo = dateFormat.format(nextScanDate)
                    
                    // Determine frequency based on time difference
                    val hoursUntil = (nextRunTime - System.currentTimeMillis()) / (1000 * 60 * 60)
                    scheduleFrequency = when {
                        hoursUntil < 30 -> "Daily scans enabled"
                        hoursUntil < 200 -> "Weekly scans enabled"
                        else -> "Monthly scans enabled"
                    }
                } else {
                    scheduleInfo = "Scheduled (calculating next run...)"
                    scheduleFrequency = "Active"
                }
            } else {
                scheduleInfo = "Tap to set up automatic scans"
                scheduleFrequency = "Not scheduled"
            }
        } catch (e: Exception) {
            scheduleInfo = "Tap to set up automatic scans"
            scheduleFrequency = "Not scheduled"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onScheduleClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (scheduleFrequency.contains("Not scheduled")) 
                            Color(0xFF6B7280).copy(alpha = 0.1f) 
                        else 
                            Color(0xFF3B82F6).copy(alpha = 0.1f), 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (scheduleFrequency.contains("Not scheduled")) 
                        Icons.Default.NotificationsOff 
                    else 
                        Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (scheduleFrequency.contains("Not scheduled")) 
                        Color(0xFF6B7280) 
                    else 
                        Color(0xFF3B82F6),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Scan Schedule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    if (!scheduleFrequency.contains("Not scheduled")) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scheduleFrequency.split(" ")[0].uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (scheduleFrequency.contains("Not scheduled")) 
                        scheduleInfo 
                    else 
                        "Next: $scheduleInfo",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SecurityScoreCircle(score: Int, textColor: Color) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000)
    )
    
    val scoreColor = when {
        score >= 90 -> Color(0xFF10B981)
        score >= 70 -> Color(0xFF3B82F6)
        score >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    
    Box(
        modifier = Modifier
            .size(140.dp)
            .background(scoreColor.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Text(
                text = "Security Score",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SecurityCheckCard(
    check: SecurityCheck,
    cardColor: Color,
    textColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val severityColor = when (check.severity) {
        SecuritySeverity.CRITICAL -> Color(0xFFEF4444)
        SecuritySeverity.WARNING -> Color(0xFFF59E0B)
        SecuritySeverity.INFO -> Color(0xFF3B82F6)
        SecuritySeverity.PASS -> Color(0xFF10B981)
    }
    
    val statusColor = if (check.passed) Color(0xFF10B981) else severityColor
    val statusIcon = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Cancel
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(statusColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCheckIcon(check.id),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = check.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        if (!check.passed && check.severity == SecuritySeverity.CRITICAL) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRITICAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = check.description,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = textColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Recommendation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = check.recommendation,
                    fontSize = 14.sp,
                    color = textColor
                )
                
                // Fix Steps
                if (check.fixSteps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "How to Fix",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    check.fixSteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = 13.sp,
                                color = severityColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Settings Button
                    if (check.settingsAction != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(check.settingsAction)
                                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to general settings
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = severityColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Settings")
                        }
                    }
                }
                
                if (check.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = check.details,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

private fun getCheckIcon(checkId: String): ImageVector {
    return when (checkId) {
        "root" -> Icons.Default.Lock
        "emulator" -> Icons.Default.Smartphone
        "usb_debug" -> Icons.Default.BugReport
        "dev_options" -> Icons.Default.DeveloperMode
        "screen_lock" -> Icons.Default.LockPerson
        "unknown_sources" -> Icons.Default.Warning
        "encryption" -> Icons.Default.Shield
        "play_protect" -> Icons.Default.VerifiedUser
        else -> Icons.Default.Security
    }
}
