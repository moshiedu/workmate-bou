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

    LaunchedEffect(Unit) {
        viewModel.startScan()
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
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
