package com.moshitech.workmate.feature.deviceinfo.tabs

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryTab(isDark: Boolean) {
    val context = LocalContext.current
    val batteryManager = remember { context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager }
    
    var batteryLevel by remember { mutableStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    var chargingStatus by remember { mutableStateOf("Unknown") }
    var current by remember { mutableStateOf(0) }
    var temperature by remember { mutableStateOf(0f) }
    var voltage by remember { mutableStateOf(0) }
    var technology by remember { mutableStateOf("Unknown") }
    var health by remember { mutableStateOf("Unknown") }
    var capacity by remember { mutableStateOf(0) }
    var chargeCounter by remember { mutableStateOf(0) }
    var designCapacity by remember { mutableStateOf(0) }
    
    var maxInput by remember { mutableStateOf(0f) }
    var chargeCycles by remember { mutableStateOf(0) }
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val accentColor = Color(0xFF10B981)
    
    // Real-time battery monitoring with BroadcastReceiver
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: android.content.Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    // Battery level
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                    batteryLevel = (level * 100) / scale
                    
                    // Charging status and type
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL
                    
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val chargingType = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        else -> ""
                    }
                    
                    chargingStatus = when (status) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> if (chargingType.isNotEmpty()) "Charging $chargingType" else "Charging"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                        BatteryManager.BATTERY_STATUS_FULL -> "Full"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                        else -> "Unknown"
                    }
                    
                    // Temperature and voltage
                    temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                    voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
                    
                    // Health
                    val healthStatus = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    health = when (healthStatus) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        else -> "Unknown"
                    }
                    
                    // Get current and charge counter from BatteryManager
                    current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000
                    chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                    
                    // Calculate max input (charging power in watts)
                    if (isCharging && current != 0 && voltage != 0) {
                        maxInput = (abs(current) * voltage) / 1000000f
                    } else {
                        maxInput = 0f
                    }
                    
                    // Better design capacity calculation
                    // Use energy counter if available, otherwise estimate from charge counter
                    try {
                        val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                        if (energyCounter > 0 && batteryLevel > 0) {
                            // Energy counter is in nWh, convert to mAh
                            designCapacity = ((energyCounter / 1000000) * 100 / batteryLevel / voltage * 1000).toInt()
                        }
                    } catch (e: Exception) {
                        // Fallback to charge counter method
                        if (chargeCounter > 0 && batteryLevel > 0) {
                            designCapacity = (chargeCounter * 100) / batteryLevel
                        }
                    }
                    
                    // Estimate charge cycles (rough estimate based on total charge throughput)
                    // This is an approximation since Android doesn't expose actual cycle count
                    if (designCapacity > 0 && chargeCounter > 0) {
                        // Calculate cycles as total charge / design capacity
                        // Add current charge to get total throughput estimate
                        val totalThroughput = chargeCounter + (designCapacity * batteryLevel / 100)
                        chargeCycles = totalThroughput / designCapacity
                        
                        // Ensure at least 1 cycle if battery has been used
                        if (chargeCycles == 0 && chargeCounter > 0) {
                            chargeCycles = 1
                        }
                    } else {
                        chargeCycles = 0
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    IconButton(onClick = {
                        try {
                            // Try to open battery usage settings first
                            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to general settings if not available
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Battery Settings",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$batteryLevel%",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            chargingStatus,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentColor
                        )
                        
                        // Show current with sign
                        val currentDisplay = if (isCharging) {
                            "+${abs(current)} mA"
                        } else {
                            "-${abs(current)} mA"
                        }
                        Text(
                            "Current $currentDisplay",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        
                        // Show power in watts when charging
                        if (isCharging && current != 0 && voltage != 0) {
                            val powerWatts = (abs(current) * voltage) / 1000000f
                            Text(
                                "Power %.2f W".format(powerWatts),
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                
                Spacer(Modifier.height(16.dp))
                
                BatteryInfoRow("Temperature", "$temperature°C", textColor)
                BatteryInfoRow("Technology", technology, textColor)
                BatteryInfoRow("Health", health, textColor)
                BatteryInfoRow("Voltage", "%.3f V".format(voltage / 1000f), textColor)
                
                if (maxInput > 0) {
                    BatteryInfoRow("Max input", "%.2f W".format(maxInput), textColor)
                }
                
                if (designCapacity > 0) {
                    BatteryInfoRow("Design capacity\n(reported by system)", "$designCapacity mAh", textColor)
                }
                
                if (chargeCounter > 0) {
                    val healthPercent = if (designCapacity > 0) (chargeCounter * 100) / designCapacity else 0
                    BatteryInfoRow(
                        "Capacity\n(estimated)", 
                        "$chargeCounter mAh${if (healthPercent > 0) "\n$healthPercent%" else ""}", 
                        textColor
                    )
                    BatteryInfoRow("Charge counter", "$chargeCounter mAh", textColor)
                }
                
                // Estimate remaining time
                if (current != 0 && chargeCounter > 0) {
                    val remainingHours = if (current < 0) {
                        // Discharging
                        abs(chargeCounter.toFloat() / current)
                    } else {
                        // Charging - estimate time to full
                        if (designCapacity > 0) {
                            (designCapacity - chargeCounter).toFloat() / current
                        } else 0f
                    }
                    
                    if (remainingHours > 0 && remainingHours < 100) {
                        val hours = remainingHours.toInt()
                        val minutes = ((remainingHours - hours) * 60).toInt()
                        BatteryInfoRow("Remaining", "${hours}h ${minutes}m", textColor)
                    }
                }
                
                if (chargeCycles > 0) {
                    BatteryInfoRow("Charge cycles", "$chargeCycles", textColor)
                } else {
                    BatteryInfoRow("Charge cycles", "N/A", textColor)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun BatteryInfoRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
