package com.moshitech.workmate.feature.deviceinfo.tabs

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.model.*

@Composable
fun NetworkTab(
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val networkInfo by viewModel.networkInfo.collectAsState()
    val context = LocalContext.current
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = Color(0xFF10B981)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshNetworkInfo()
        }
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshNetworkInfo()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Overview
        item {
            networkInfo?.connectionStatus?.let { status ->
                ConnectionOverviewCard(
                    status = status,
                    cardColor = cardColor,
                    textColor = textColor,
                    accentColor = accentColor,
                    subtitleColor = subtitleColor
                )
            }
        }

        // Wi-Fi Section
        item {
            networkInfo?.wifiDetails?.let { wifi ->
                WifiSection(
                    wifi = wifi,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    accentColor = accentColor,
                    onShowSsid = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )
            }
        }

        // DHCP Section
        item {
            networkInfo?.dhcpDetails?.let { dhcp ->
                DhcpSection(
                    dhcp = dhcp,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    accentColor = accentColor,
                    onShowPublicIp = {
                        viewModel.refreshPublicIp()
                    }
                )
            }
        }

        // Hardware Section
        item {
            networkInfo?.hardwareDetails?.let { hardware ->
                HardwareSection(
                    hardware = hardware,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                )
            }
        }

        // Mobile Section
        item {
            networkInfo?.mobileDetails?.let { mobile ->
                MobileSection(
                    mobile = mobile,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    accentColor = accentColor,
                    onShowDetails = {
                        phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                    }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ConnectionOverviewCard(
    status: ConnectionStatus,
    cardColor: Color,
    textColor: Color,
    accentColor: Color,
    subtitleColor: Color
) {
    val context = LocalContext.current
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Connection Settings",
                    tint = subtitleColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (status.type == ConnectionType.WIFI) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    tint = if (status.isConnected) accentColor else subtitleColor,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = status.description,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status.isConnected) accentColor else subtitleColor
                    )
                    if (status.isConnected) {
                        Text(
                            text = "${status.linkSpeedMbps} Mbps",
                            fontSize = 14.sp,
                            color = accentColor
                        )
                        Text(
                            text = "${status.signalStrengthPercent}%  ${status.signalStrengthDbm} dBm",
                            fontSize = 14.sp,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WifiSection(
    wifi: WifiDetails,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    onShowSsid: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    
    NetworkCard(
        title = "Wi-Fi",
        cardColor = cardColor,
        textColor = accentColor,
        onSettingsClick = {
            try {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open WiFi settings", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        DetailRow(label = "Status", value = "Connected", textColor = textColor, subtitleColor = subtitleColor)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Network", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        ShowableDetailRow(
            label = "Network",
            value = wifi.ssid,
            showAction = wifi.ssid == "<unknown ssid>" || !hasPermission,
            onShow = onShowSsid,
            textColor = textColor,
            subtitleColor = subtitleColor,
            accentColor = accentColor
        )
        ShowableDetailRow(
            label = "BSSID",
            value = wifi.bssid,
            showAction = wifi.bssid == "Unavailable" || !hasPermission,
            onShow = onShowSsid,
            textColor = textColor,
            subtitleColor = subtitleColor,
            accentColor = accentColor
        )
        DetailRow(label = "Link speed", value = wifi.linkSpeed, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Signal strength", value = wifi.signalStrength, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Frequency", value = wifi.frequency, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Width", value = wifi.width, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Channel", value = wifi.channel.toString(), textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Standard", value = wifi.standard, textColor = textColor, subtitleColor = subtitleColor)
    }
}

@Composable
fun DhcpSection(
    dhcp: DhcpDetails,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    onShowPublicIp: () -> Unit
) {
    val context = LocalContext.current
    
    NetworkCard(title = "DHCP", cardColor = cardColor, textColor = accentColor, showSettings = false) {
        DetailRow(label = "DHCP Server", value = dhcp.server, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DHCP lease duration", value = dhcp.leaseDuration, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Gateway", value = dhcp.gateway, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Subnet mask", value = dhcp.subnetMask, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DNS1", value = dhcp.dns1, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DNS2", value = dhcp.dns2, textColor = textColor, subtitleColor = subtitleColor)
        CopyableDetailRow(label = "IP address", value = dhcp.ipAddress, textColor = textColor, subtitleColor = subtitleColor, accentColor = accentColor)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("IPv6", color = subtitleColor, fontSize = 12.sp)
        Text(dhcp.ipv6, color = textColor, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (dhcp.publicIp == "Tap to show") {
            ShowableDetailRow(
                label = "Public IP",
                value = dhcp.publicIp,
                showAction = true,
                onShow = onShowPublicIp,
                textColor = textColor,
                subtitleColor = subtitleColor,
                accentColor = accentColor
            )
        } else {
            CopyableDetailRow(
                label = "Public IP",
                value = dhcp.publicIp,
                textColor = textColor,
                subtitleColor = subtitleColor,
                accentColor = accentColor
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                val dhcpInfo = buildString {
                    appendLine("DHCP Information")
                    appendLine("=".repeat(30))
                    appendLine("DHCP Server: ${dhcp.server}")
                    appendLine("Lease Duration: ${dhcp.leaseDuration}")
                    appendLine("Gateway: ${dhcp.gateway}")
                    appendLine("Subnet Mask: ${dhcp.subnetMask}")
                    appendLine("DNS1: ${dhcp.dns1}")
                    appendLine("DNS2: ${dhcp.dns2}")
                    appendLine("IP Address: ${dhcp.ipAddress}")
                    appendLine("IPv6: ${dhcp.ipv6}")
                    if (dhcp.publicIp != "Tap to show") {
                        appendLine("Public IP: ${dhcp.publicIp}")
                    }
                }
                copyToClipboard(context, dhcpInfo, "DHCP Info")
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy All DHCP Info")
        }
    }
}

@Composable
fun HardwareSection(
    hardware: NetworkHardwareDetails,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    NetworkCard(title = "Hardware", cardColor = cardColor, textColor = Color(0xFF10B981)) {
        FeatureRow(label = hardware.supportedBands, isSupported = true, textColor = textColor)
        FeatureRow(label = "Wi-Fi Direct support", isSupported = hardware.isWifiDirectSupported, textColor = textColor)
        FeatureRow(label = "Wi-Fi Aware support", isSupported = hardware.isWifiAwareSupported, textColor = textColor)
        FeatureRow(label = "Wi-Fi Passpoint support", isSupported = hardware.isPasspointSupported, textColor = textColor)
        FeatureRow(label = "5GHz band support", isSupported = hardware.is5GhzSupported, textColor = textColor)
        FeatureRow(label = "6GHz band support", isSupported = hardware.is6GhzSupported, textColor = textColor)
    }
}

@Composable
fun MobileSection(
    mobile: MobileDetails,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    onShowDetails: () -> Unit
) {
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    NetworkCard(
        title = "Mobile",
        cardColor = cardColor,
        textColor = accentColor,
        onSettingsClick = {
            try {
                context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open mobile settings", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        DetailRow(label = "Dual SIM", value = if (mobile.isDualSim) "Yes" else "No", textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Phone type", value = mobile.phoneType, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "eSIM", value = if (mobile.isEsim) "Yes" else "No", textColor = textColor, subtitleColor = subtitleColor)
        
        if (!hasPermission) {
            Spacer(modifier = Modifier.height(8.dp))
            ShowableDetailRow(
                label = "Network info",
                value = "SHOW",
                showAction = true,
                onShow = onShowDetails,
                textColor = textColor,
                subtitleColor = subtitleColor,
                accentColor = accentColor
            )
        } else {
            // Default slots section
            Spacer(modifier = Modifier.height(12.dp))
            Text("Defaults", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            DetailRow(
                label = "Data",
                value = when (mobile.defaultDataSlot) {
                    1 -> "SIM 1"
                    2 -> "SIM 2"
                    else -> "None"
                },
                textColor = textColor,
                subtitleColor = subtitleColor
            )
            DetailRow(
                label = "Voice",
                value = when (mobile.defaultVoiceSlot) {
                    1 -> "SIM 1"
                    2 -> "SIM 2"
                    else -> "None"
                },
                textColor = textColor,
                subtitleColor = subtitleColor
            )
            DetailRow(
                label = "SMS",
                value = when (mobile.defaultSmsSlot) {
                    1 -> "SIM 1"
                    2 -> "SIM 2"
                    else -> "None"
                },
                textColor = textColor,
                subtitleColor = subtitleColor
            )
            
            // SIM 1 Section
            mobile.sim1Info?.let { sim1 ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SIM 1", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (sim1.isAvailable) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "SIM 1 Settings",
                            tint = accentColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                                    } catch (e: Exception) {
                                        Toast
                                            .makeText(context, "Cannot open SIM settings", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sim1.isAvailable) {
                    DetailRow(label = "State", value = sim1.simState, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Carrier", value = sim1.carrierName, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Operator code", value = sim1.operatorCode, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Country", value = sim1.countryIso, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Network type", value = sim1.networkType, textColor = textColor, subtitleColor = subtitleColor)
                } else {
                    Text("Not Available", color = subtitleColor, fontSize = 14.sp)
                }
            }
            
            // SIM 2 Section
            mobile.sim2Info?.let { sim2 ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SIM 2", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (sim2.isAvailable) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "SIM 2 Settings",
                            tint = accentColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                                    } catch (e: Exception) {
                                        Toast
                                            .makeText(context, "Cannot open SIM settings", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sim2.isAvailable) {
                    DetailRow(label = "State", value = sim2.simState, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Carrier", value = sim2.carrierName, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Operator code", value = sim2.operatorCode, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Country", value = sim2.countryIso, textColor = textColor, subtitleColor = subtitleColor)
                    DetailRow(label = "Network type", value = sim2.networkType, textColor = textColor, subtitleColor = subtitleColor)
                } else {
                    Text("Not Available", color = subtitleColor, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun NetworkCard(
    title: String,
    cardColor: Color,
    textColor: Color,
    showSettings: Boolean = true,
    onSettingsClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (showSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = textColor.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(20.dp)
                            .then(
                                if (onSettingsClick != null) {
                                    Modifier.clickable { onSettingsClick() }
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = subtitleColor
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ShowableDetailRow(
    label: String,
    value: String,
    showAction: Boolean,
    onShow: () -> Unit,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = subtitleColor
        )
        if (showAction) {
            Text(
                text = "SHOW",
                fontSize = 14.sp,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onShow() }
            )
        } else {
            Text(
                text = value,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeatureRow(
    label: String,
    isSupported: Boolean,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isSupported) Color(0xFF10B981) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
fun CopyableDetailRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = subtitleColor
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        copyToClipboard(context, value, label)
                    }
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
