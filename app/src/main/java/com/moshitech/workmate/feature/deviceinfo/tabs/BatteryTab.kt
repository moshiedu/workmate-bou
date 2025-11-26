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
import java.io.File
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
    var designCapacity by remember { mutableStateOf(getBatteryProfileCapacity(context)) }
    
    var maxInput by remember { mutableStateOf(0f) }
    
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val accentColor = Color(0xFF10B981)
    
    // Periodic update for battery properties that might not trigger broadcasts
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Update every second
            try {
                // Update current reading
                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                current = if (currentNow != 0) {
                    currentNow / 1000
                } else {
                    // Try average
                    val avg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                    if (avg != 0) avg / 1000 else readKernelCurrent()
                }
            } catch (e: Exception) {
                // Try kernel fallback
                val kernelCurrent = readKernelCurrent()
                if (kernelCurrent != 0) current = kernelCurrent
            }
        }
    }
    
    
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
                    // Try CURRENT_NOW first, fallback to CURRENT_AVERAGE, then Sysfs
                    current = try {
                        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                        if (currentNow != 0) {
                            currentNow / 1000
                        } else {
                            // Fallback to CURRENT_AVERAGE
                            val avg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                            if (avg != 0) avg / 1000 else readKernelCurrent()
                        }
                    } catch (e: Exception) {
                        readKernelCurrent()
                    }
                    
                    chargeCounter = try {
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                    } catch (e: Exception) {
                        0
                    }
                    
                    // Calculate max input (charging power in watts)
                    if (isCharging && current != 0 && voltage != 0) {
                        maxInput = (abs(current) * voltage) / 1000000f
                    } else {
                        maxInput = 0f
                    }
                    
                    // Better design capacity calculation
                    // If PowerProfile failed (designCapacity is 0), try estimation methods
                    if (designCapacity == 0) {
                        try {
                            val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                            if (energyCounter > 0 && batteryLevel > 0 && voltage > 0) {
                                // Energy counter is in nWh, convert to mAh
                                designCapacity = ((energyCounter / 1000000) * 100 / batteryLevel / voltage * 1000).toInt()
                            } else if (chargeCounter > 0 && batteryLevel > 0) {
                                // Fallback to charge counter method
                                designCapacity = (chargeCounter * 100) / batteryLevel
                            }
                        } catch (e: Exception) {
                            // Fallback to charge counter method
                            if (chargeCounter > 0 && batteryLevel > 0) {
                                designCapacity = (chargeCounter * 100) / batteryLevel
                            }
                        }
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
                        
                        // Show current with sign and 3-digit formatting
                        val currentDisplay = if (isCharging) {
                            "+${abs(current)} mA"
                        } else {
                            "-${abs(current)} mA"
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Current ",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                            Text(
                                currentDisplay,
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Show power in watts when charging
                        if (isCharging && current != 0 && voltage != 0) {
                            val powerWatts = (abs(current) * voltage) / 1000000f
                            Text(
                                "Power %.3f W".format(powerWatts),
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
                    BatteryInfoRow("Max input", "%.3f W".format(maxInput), textColor)
                }
                
                // Always show design capacity
                if (designCapacity > 0) {
                    BatteryInfoRow("Design capacity\n(reported by system)", "%,d mAh".format(designCapacity), textColor)
                } else {
                    BatteryInfoRow("Design capacity\n(reported by system)", "N/A", textColor)
                }
                
                if (chargeCounter > 0) {
                    // Calculate Estimated Full Capacity (Health)
                    // Formula: (Current Charge / Battery Level) * 100
                    val estimatedFullCapacity = if (batteryLevel > 0) (chargeCounter.toLong() * 100) / batteryLevel else 0
                    
                    val healthPercent = if (designCapacity > 0 && estimatedFullCapacity > 0) 
                        (estimatedFullCapacity * 100) / designCapacity 
                    else 0
                    
                    val capacityText = if (healthPercent > 0) {
                        "%,d mAh\n$healthPercent%%".format(estimatedFullCapacity)
                    } else {
                        "%,d mAh".format(estimatedFullCapacity)
                    }
                    BatteryInfoRow(
                        "Capacity\n(estimated full)", 
                        capacityText, 
                        textColor
                    )
                    BatteryInfoRow("Charge counter", "%,d mAh".format(chargeCounter), textColor)
                }
                
                // Estimate remaining time
                // Fallback for chargeCounter if 0
                val effectiveChargeCounter = if (chargeCounter > 0) {
                    chargeCounter
                } else if (designCapacity > 0) {
                    (designCapacity * batteryLevel) / 100
                } else {
                    0
                }

                if (current != 0 && effectiveChargeCounter > 0) {
                    val remainingHours = if (current < 0) {
                        // Discharging
                        abs(effectiveChargeCounter.toFloat() / current)
                    } else {
                        // Charging - estimate time to full
                        if (designCapacity > 0) {
                            (designCapacity - effectiveChargeCounter).toFloat() / current
                        } else 0f
                    }
                    
                    if (remainingHours > 0 && remainingHours < 100) {
                        val totalSeconds = (remainingHours * 3600).toLong()
                        val days = totalSeconds / 86400
                        val hours = (totalSeconds % 86400) / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        
                        val timeString = buildString {
                            if (days > 0) append("${days}d ")
                            if (hours > 0) append("${hours}h ")
                            if (minutes > 0) append("${minutes}m ")
                            append("${seconds}s")
                        }
                        
                        BatteryInfoRow("Remaining", timeString, textColor)
                    }
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

private fun getBatteryProfileCapacity(context: Context): Int {
    return try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
        val capacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
        capacity.toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

private fun readKernelCurrent(): Int {
    val paths = listOf(
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/bms/current_now",
        "/sys/class/power_supply/main/current_now",
        "/sys/class/power_supply/usb/current_now"
    )
    for (path in paths) {
        try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                val value = file.readText().trim().toInt()
                if (value != 0) {
                    // Kernel values are usually in microamperes (uA)
                    // Some devices might report in mA, but uA is standard
                    // Heuristic: if value > 10000, assume uA and divide by 1000
                    // If value is small (e.g. 500), assume mA
                    return if (abs(value) > 10000) value / 1000 else value
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
    return 0
}
