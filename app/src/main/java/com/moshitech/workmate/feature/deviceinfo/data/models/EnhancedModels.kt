package com.moshitech.workmate.feature.deviceinfo.data.models

data class BatteryInfoEnhanced(
    // Status
    val level: Int = 0,
    val status: String = "Unknown",
    val health: String = "Unknown",
    val plugged: String = "Unknown",
    val present: Boolean = false,
    
    // Specifications
    val technology: String = "Unknown",
    val capacity: Int = 0,
    val designCapacity: Int = 0,
    val cycleCount: Int = 0,
    
    // Measurements
    val voltage: Int = 0,
    val current: Int = 0,
    val temperature: Float = 0f,
    val power: Float = 0f,
    
    // Charging
    val chargingSpeed: String = "Unknown",
    val chargeTimeRemaining: String = "Unknown",
    val fastChargingSupport: Boolean = false,
    val wirelessChargingSupport: Boolean = false,
    
    // Health
    val healthPercentage: Int = 100,
    val manufactureDate: String = "Unknown",
    val firstUseDate: String = "Unknown"
)

data class NetworkInfoEnhanced(
    // WiFi
    val wifiEnabled: Boolean = false,
    val wifiConnected: Boolean = false,
    val wifiSsid: String = "Not connected",
    val wifiBssid: String = "Unknown",
    val wifiIpv4: String = "Unknown",
    val wifiIpv6: String = "Unknown",
    val wifiLinkSpeed: String = "Unknown",
    val wifiFrequency: String = "Unknown",
    val wifiSignalStrength: Int = 0,
    val wifiStandard: String = "Unknown",
    
    // Mobile Data
    val mobileDataEnabled: Boolean = false,
    val mobileDataConnected: Boolean = false,
    val mobileOperator: String = "Unknown",
    val mobileNetworkType: String = "Unknown",
    val mobileSignalStrength: Int = 0,
    val mobileIpv4: String = "Unknown",
    val mobileIpv6: String = "Unknown",
    
    // Dual SIM
    val simCount: Int = 0,
    val sim1Operator: String = "Unknown",
    val sim2Operator: String = "Unknown",
    
    // Bluetooth
    val bluetoothEnabled: Boolean = false,
    val bluetoothVersion: String = "Unknown",
    val bluetoothPairedDevices: Int = 0,
    
    // Public IP
    val publicIp: String = "Unknown",
    
    // VPN
    val vpnActive: Boolean = false
)

data class SensorInfoEnhanced(
    val name: String,
    val type: String,
    val vendor: String,
    val version: Int,
    val power: Float,
    val maxRange: Float,
    val resolution: Float,
    val minDelay: Int,
    val maxDelay: Int,
    val currentValue: String = "N/A"
)
