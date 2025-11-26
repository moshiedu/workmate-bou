package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.utils.FormatUtils

@Composable
fun DashboardTab(
    navController: androidx.navigation.NavController,
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val dashboardInfo by viewModel.dashboardInfo.collectAsState()
    val hardwareInfo by viewModel.hardwareInfoEnhanced.collectAsState()
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    var showCpuDialog by remember { mutableStateOf(false) }
    var showSocDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var showRamDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CPU Status Card
        InteractiveInfoCard(
            title = "CPU Status",
            items = listOf(
                "Frequency" to dashboardInfo.cpuFrequency,
                "Temperature" to dashboardInfo.cpuTemperature
            ),
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showCpuDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // SOC Card
        InteractiveInfoCard(
            title = "SOC",
            items = listOf(
                "Name" to hardwareInfo.socName,
                "Temperature" to dashboardInfo.cpuTemperature
            ),
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showSocDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // Battery Card
        InteractiveInfoCard(
            title = "Battery",
            items = listOf(
                "Level" to "${dashboardInfo.batteryLevel}%",
                "Temperature" to FormatUtils.formatTemperature(dashboardInfo.batteryTemperature),
                "Status" to dashboardInfo.batteryStatus
            ),
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showBatteryDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // Network Card
        InteractiveInfoCard(
            title = "Network",
            items = listOf(
                "Type" to dashboardInfo.networkType,
                "Signal" to dashboardInfo.signalStrength
            ),
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showNetworkDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // RAM Card
        val ramPercent = if (dashboardInfo.ramTotal > 0) {
            (dashboardInfo.ramUsed.toFloat() / dashboardInfo.ramTotal * 100)
        } else 0f
        
        InteractiveProgressCard(
            title = "RAM",
            used = FormatUtils.formatBytes(dashboardInfo.ramUsed),
            total = FormatUtils.formatBytes(dashboardInfo.ramTotal),
            percentage = ramPercent,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showRamDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // Storage Card
        val storagePercent = if (dashboardInfo.storageTotal > 0) {
            (dashboardInfo.storageUsed.toFloat() / dashboardInfo.storageTotal * 100)
        } else 0f
        
        InteractiveProgressCard(
            title = "Storage",
            used = FormatUtils.formatBytes(dashboardInfo.storageUsed),
            total = FormatUtils.formatBytes(dashboardInfo.storageTotal),
            percentage = storagePercent,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            onCardClick = { showStorageDialog = true },
            onMenuClick = { /* Show menu options */ }
        )
        
        // Tests Button
        Button(
            onClick = { 
                navController.navigate("tests")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Tests", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hardware Tests", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    
    // CPU Detail Dialog
    if (showCpuDialog) {
        DetailDialog(
            title = "CPU Details",
            onDismiss = { showCpuDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Core Temperatures:", fontWeight = FontWeight.Bold, color = textColor)
                hardwareInfo.cpuCores.forEachIndexed { index, core ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
    }
    
    // SOC Detail Dialog
    if (showSocDialog) {
        DetailDialog(
            title = "SOC Details",
            onDismiss = { showSocDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardDetailRow("Name", hardwareInfo.socName, textColor, subtitleColor)
                DashboardDetailRow("Manufacturer", hardwareInfo.socManufacturer, textColor, subtitleColor)
                DashboardDetailRow("Model", hardwareInfo.socModel, textColor, subtitleColor)
                DashboardDetailRow("Temperature", dashboardInfo.cpuTemperature, textColor, subtitleColor)
            }
        }
    }
    
    // Battery Detail Dialog
    if (showBatteryDialog) {
        DetailDialog(
            title = "Battery Details",
            onDismiss = { showBatteryDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardDetailRow("Level", "${dashboardInfo.batteryLevel}%", textColor, subtitleColor)
                DashboardDetailRow("Temperature", FormatUtils.formatTemperature(dashboardInfo.batteryTemperature), textColor, subtitleColor)
                DashboardDetailRow("Status", dashboardInfo.batteryStatus, textColor, subtitleColor)
                DashboardDetailRow("Health", "Good", textColor, subtitleColor)
            }
        }
    }
    
    // Network Detail Dialog
    if (showNetworkDialog) {
        DetailDialog(
            title = "Network Details",
            onDismiss = { showNetworkDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardDetailRow("Type", dashboardInfo.networkType, textColor, subtitleColor)
                DashboardDetailRow("Signal Strength", dashboardInfo.signalStrength, textColor, subtitleColor)
                DashboardDetailRow("Status", "Connected", textColor, subtitleColor)
            }
        }
    }
    
    // RAM Detail Dialog
    if (showRamDialog) {
        DetailDialog(
            title = "RAM Details",
            onDismiss = { showRamDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardDetailRow("Total", FormatUtils.formatBytes(dashboardInfo.ramTotal), textColor, subtitleColor)
                DashboardDetailRow("Used", FormatUtils.formatBytes(dashboardInfo.ramUsed), textColor, subtitleColor)
                DashboardDetailRow("Available", FormatUtils.formatBytes(dashboardInfo.ramTotal - dashboardInfo.ramUsed), textColor, subtitleColor)
                DashboardDetailRow("Type", hardwareInfo.ramType, textColor, subtitleColor)
            }
        }
    }
    
    // Storage Detail Dialog
    if (showStorageDialog) {
        DetailDialog(
            title = "Storage Details",
            onDismiss = { showStorageDialog = false },
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardDetailRow("Total", FormatUtils.formatBytes(dashboardInfo.storageTotal), textColor, subtitleColor)
                DashboardDetailRow("Used", FormatUtils.formatBytes(dashboardInfo.storageUsed), textColor, subtitleColor)
                DashboardDetailRow("Available", FormatUtils.formatBytes(dashboardInfo.storageTotal - dashboardInfo.storageUsed), textColor, subtitleColor)
                DashboardDetailRow("Type", hardwareInfo.storageType, textColor, subtitleColor)
            }
        }
    }
}

@Composable
fun InteractiveInfoCard(
    title: String,
    items: List<Pair<String, String>>,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onCardClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = subtitleColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = subtitleColor)
                    Text(value, color = textColor, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun InteractiveProgressCard(
    title: String,
    used: String,
    total: String,
    percentage: Float,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onCardClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1890FF)
                    )
                    IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = subtitleColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF1890FF),
                trackColor = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Used: $used", color = subtitleColor, style = MaterialTheme.typography.bodySmall)
                Text("Total: $total", color = subtitleColor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DetailDialog(
    title: String,
    onDismiss: () -> Unit,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color(0xFF1890FF))
                }
            }
        }
    }
}

@Composable
private fun DashboardDetailRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = subtitleColor)
        Text(value, color = textColor, fontWeight = FontWeight.Medium)
    }
}
