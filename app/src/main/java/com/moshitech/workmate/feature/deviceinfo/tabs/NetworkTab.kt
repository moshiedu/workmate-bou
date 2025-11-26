package com.moshitech.workmate.feature.deviceinfo.tabs

import android.Manifest
import android.content.pm.PackageManager
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
                    contentDescription = "Settings",
                    tint = subtitleColor,
                    modifier = Modifier.size(20.dp)
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
    NetworkCard(title = "Wi-Fi", cardColor = cardColor, textColor = accentColor) {
        val context = LocalContext.current
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
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
            showAction = wifi.bssid == "Unavailable" || !hasPermission, // Simplified check
            onShow = onShowSsid,
            textColor = textColor,
            subtitleColor = subtitleColor,
            accentColor = accentColor
        )
        DetailRow(label = "Link speed", value = wifi.linkSpeed, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Signal strength", value = wifi.signalStrength, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Frequency", value = wifi.frequency, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Width", value = wifi.width, textColor = textColor, subtitleColor = subtitleColor) // Simplified
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
    NetworkCard(title = "DHCP", cardColor = cardColor, textColor = accentColor) {
        DetailRow(label = "DHCP Server", value = dhcp.server, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DHCP lease duration", value = dhcp.leaseDuration, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Gateway", value = dhcp.gateway, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Subnet mask", value = dhcp.subnetMask, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DNS1", value = dhcp.dns1, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "DNS2", value = dhcp.dns2, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "IP address", value = dhcp.ipAddress, textColor = textColor, subtitleColor = subtitleColor)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("IPv6", color = subtitleColor, fontSize = 12.sp)
        Text(dhcp.ipv6, color = textColor, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        ShowableDetailRow(
            label = "Public IP",
            value = dhcp.publicIp,
            showAction = dhcp.publicIp == "Tap to show",
            onShow = onShowPublicIp,
            textColor = textColor,
            subtitleColor = subtitleColor,
            accentColor = accentColor
        )
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
    NetworkCard(title = "Mobile", cardColor = cardColor, textColor = accentColor) {
        val context = LocalContext.current
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        DetailRow(label = "Dual SIM", value = if (mobile.isDualSim) "Yes" else "No", textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "Phone type", value = mobile.phoneType, textColor = textColor, subtitleColor = subtitleColor)
        DetailRow(label = "eSIM", value = if (mobile.isEsim) "Yes" else "No", textColor = textColor, subtitleColor = subtitleColor)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Connection", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        DetailRow(label = "Status", value = mobile.simState, textColor = textColor, subtitleColor = subtitleColor)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("SIM 1", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!hasPermission) {
             ShowableDetailRow(
                label = "Network type",
                value = "SHOW",
                showAction = true,
                onShow = onShowDetails,
                textColor = textColor,
                subtitleColor = subtitleColor,
                accentColor = accentColor
            )
        } else {
            DetailRow(label = "State", value = mobile.simState, textColor = textColor, subtitleColor = subtitleColor)
            DetailRow(label = "Carrier", value = mobile.carrierName, textColor = textColor, subtitleColor = subtitleColor)
            DetailRow(label = "Operator code", value = mobile.operatorCode, textColor = textColor, subtitleColor = subtitleColor)
            DetailRow(label = "Country", value = mobile.countryIso, textColor = textColor, subtitleColor = subtitleColor)
            DetailRow(label = "Roaming", value = mobile.roaming, textColor = textColor, subtitleColor = subtitleColor)
            DetailRow(label = "Network type", value = mobile.networkType, textColor = textColor, subtitleColor = subtitleColor)
        }
    }
}

@Composable
fun NetworkCard(
    title: String,
    cardColor: Color,
    textColor: Color,
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
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
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
