package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.utils.FormatUtils

@Composable
fun DashboardTabEnhanced(
    navController: androidx.navigation.NavController,
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val dashboardInfo by viewModel.dashboardInfo.collectAsState()
    val hardwareInfo by viewModel.hardwareInfoEnhanced.collectAsState()
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    // Dialog states
    var showCpuDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showRamDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Section (Full Width)
        item(span = StaggeredGridItemSpan.FullLine) {
            DashboardHeader(
                deviceName = hardwareInfo.deviceModel,
                socName = hardwareInfo.socName,
                textColor = textColor,
                subtitleColor = subtitleColor
            )
        }

        // RAM Card
        item {
            val ramPercent = if (dashboardInfo.ramTotal > 0) {
                (dashboardInfo.ramUsed.toFloat() / dashboardInfo.ramTotal * 100)
            } else 0f
            
            CompactProgressCard(
                title = "RAM",
                icon = Icons.Default.Memory,
                value = FormatUtils.formatBytes(dashboardInfo.ramUsed),
                total = FormatUtils.formatBytes(dashboardInfo.ramTotal),
                percentage = ramPercent,
                color = Color(0xFF5C6BC0), // Indigo
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showRamDialog = true }
            )
        }

        // Storage Card
        item {
            val storagePercent = if (dashboardInfo.storageTotal > 0) {
                (dashboardInfo.storageUsed.toFloat() / dashboardInfo.storageTotal * 100)
            } else 0f
            
            CompactProgressCard(
                title = "Storage",
                icon = Icons.Default.Storage,
                value = FormatUtils.formatBytes(dashboardInfo.storageUsed),
                total = FormatUtils.formatBytes(dashboardInfo.storageTotal),
                percentage = storagePercent,
                color = Color(0xFFEF5350), // Red
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showStorageDialog = true }
            )
        }

        // Battery Card
        item {
            CompactInfoCard(
                title = "Battery",
                icon = Icons.Default.BatteryStd,
                mainValue = "${dashboardInfo.batteryLevel}%",
                subValue = dashboardInfo.batteryStatus,
                accentColor = Color(0xFF66BB6A), // Green
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showBatteryDialog = true }
            )
        }

        // CPU Card
        item {
            CompactInfoCard(
                title = "CPU",
                icon = Icons.Default.DeveloperBoard,
                mainValue = dashboardInfo.cpuFrequency,
                subValue = dashboardInfo.cpuTemperature,
                accentColor = Color(0xFFFFA726), // Orange
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showCpuDialog = true }
            )
        }

        // Network Card
        item {
            CompactInfoCard(
                title = "Network",
                icon = Icons.Default.Wifi,
                mainValue = dashboardInfo.networkType,
                subValue = dashboardInfo.signalStrength,
                accentColor = Color(0xFF29B6F6), // Light Blue
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showNetworkDialog = true }
            )
        }

        // Data Usage Card
        item {
            var showDataDialog by remember { mutableStateOf(false) }
            val bootDate = remember {
                val bootTime = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()
                java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(bootTime))
            }
            
            CompactInfoCard(
                title = "Data Usage",
                icon = Icons.Default.DataUsage,
                mainValue = "${FormatUtils.formatBytes(dashboardInfo.networkSpeedDownload + dashboardInfo.networkSpeedUpload)}/s",
                subValue = "Total: ${FormatUtils.formatBytes(dashboardInfo.dataReceived + dashboardInfo.dataSent)}",
                accentColor = Color(0xFFEC407A), // Pink
                cardColor = cardColor,
                textColor = textColor,
                onClick = { showDataDialog = true }
            )
            
            if (showDataDialog) {
                DashboardDialog(
                    title = "Data Usage Details",
                    onDismiss = { showDataDialog = false },
                    cardColor = cardColor,
                    textColor = textColor
                ) {
                    DashboardItemRow("Total Received", FormatUtils.formatBytes(dashboardInfo.dataReceived), textColor, subtitleColor)
                    DashboardItemRow("Total Sent", FormatUtils.formatBytes(dashboardInfo.dataSent), textColor, subtitleColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = subtitleColor.copy(alpha = 0.2f))
                    DashboardItemRow("Download Speed", "${FormatUtils.formatBytes(dashboardInfo.networkSpeedDownload)}/s", textColor, subtitleColor)
                    DashboardItemRow("Upload Speed", "${FormatUtils.formatBytes(dashboardInfo.networkSpeedUpload)}/s", textColor, subtitleColor)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Stats tracked since last boot: $bootDate",
                        fontSize = 12.sp,
                        color = subtitleColor,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        // Hardware Tests Button (Full Width)
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate("tests") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Tests", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = { navController.navigate("benchmarks") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF) // Deep Purple
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Benchmarks", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                
                Button(
                    onClick = { navController.navigate("widgets") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981) // Green
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Widgets", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
    
    // Dialogs (Reusing existing DetailDialog logic from DashboardTab.kt if possible, or redefining here)
    // For simplicity, I'll redefine a simple version here or we can make DetailDialog public in DashboardTab
    // Since I can't easily change visibility of existing file without another edit, I'll copy the DetailDialog logic for now.
    
    if (showRamDialog) {
        DashboardDialog(
            title = "RAM Details",
            onDismiss = { showRamDialog = false },
            cardColor = cardColor,
            textColor = textColor
        ) {
            DashboardItemRow("Total", FormatUtils.formatBytes(dashboardInfo.ramTotal), textColor, subtitleColor)
            DashboardItemRow("Used", FormatUtils.formatBytes(dashboardInfo.ramUsed), textColor, subtitleColor)
            DashboardItemRow("Available", FormatUtils.formatBytes(dashboardInfo.ramTotal - dashboardInfo.ramUsed), textColor, subtitleColor)
            DashboardItemRow("Type", hardwareInfo.ramType, textColor, subtitleColor)
        }
    }
    
    if (showStorageDialog) {
        DashboardDialog(
            title = "Storage Details",
            onDismiss = { showStorageDialog = false },
            cardColor = cardColor,
            textColor = textColor
        ) {
            DashboardItemRow("Total", FormatUtils.formatBytes(dashboardInfo.storageTotal), textColor, subtitleColor)
            DashboardItemRow("Used", FormatUtils.formatBytes(dashboardInfo.storageUsed), textColor, subtitleColor)
            DashboardItemRow("Available", FormatUtils.formatBytes(dashboardInfo.storageTotal - dashboardInfo.storageUsed), textColor, subtitleColor)
            DashboardItemRow("Type", hardwareInfo.storageType, textColor, subtitleColor)
        }
    }
    
    if (showBatteryDialog) {
        DashboardDialog(
            title = "Battery Details",
            onDismiss = { showBatteryDialog = false },
            cardColor = cardColor,
            textColor = textColor
        ) {
            DashboardItemRow("Level", "${dashboardInfo.batteryLevel}%", textColor, subtitleColor)
            DashboardItemRow("Temperature", FormatUtils.formatTemperature(dashboardInfo.batteryTemperature), textColor, subtitleColor)
            DashboardItemRow("Status", dashboardInfo.batteryStatus, textColor, subtitleColor)
            DashboardItemRow("Health", "Good", textColor, subtitleColor)
        }
    }
    
    if (showCpuDialog) {
        DashboardDialog(
            title = "CPU Details",
            onDismiss = { showCpuDialog = false },
            cardColor = cardColor,
            textColor = textColor
        ) {
             dashboardInfo.cpuCores.forEachIndexed { index, core ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Core $index", color = subtitleColor)
                    Text(
                        FormatUtils.formatFrequency(core.currentFrequency),
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    if (showNetworkDialog) {
        DashboardDialog(
            title = "Network Details",
            onDismiss = { showNetworkDialog = false },
            cardColor = cardColor,
            textColor = textColor
        ) {
            DashboardItemRow("Type", dashboardInfo.networkType, textColor, subtitleColor)
            DashboardItemRow("Signal", dashboardInfo.signalStrength, textColor, subtitleColor)
            DashboardItemRow("Status", "Connected", textColor, subtitleColor)
        }
    }
}

@Composable
fun DashboardHeader(
    deviceName: String,
    socName: String,
    textColor: Color,
    subtitleColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "$deviceName • $socName",
            fontSize = 16.sp,
            color = subtitleColor
        )
    }
}

@Composable
fun CompactInfoCard(
    title: String,
    icon: ImageVector,
    mainValue: String,
    subValue: String,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat style
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = mainValue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subValue,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun CompactProgressCard(
    title: String,
    icon: ImageVector,
    value: String,
    total: String,
    percentage: Float,
    color: Color,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f),
                    maxLines = 1
                )
                Text(
                    text = total,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DashboardDialog(
    title: String,
    onDismiss: () -> Unit,
    cardColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DashboardItemRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = subtitleColor)
        Text(value, color = textColor, fontWeight = FontWeight.Medium)
    }
}
