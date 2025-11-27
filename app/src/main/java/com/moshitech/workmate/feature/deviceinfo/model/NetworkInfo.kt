package com.moshitech.workmate.feature.deviceinfo.model

data class NetworkInfo(
    val connectionStatus: ConnectionStatus,
    val wifiDetails: WifiDetails?,
    val mobileDetails: MobileDetails?,
    val dhcpDetails: DhcpDetails?,
    val hardwareDetails: NetworkHardwareDetails
)

enum class ConnectionType {
    WIFI, CELLULAR, ETHERNET, NONE
}

data class ConnectionStatus(
    val isConnected: Boolean,
    val type: ConnectionType,
    val signalStrengthPercent: Int, // 0-100
    val signalStrengthDbm: Int,
    val linkSpeedMbps: Int,
    val description: String // e.g., "Wi-Fi", "4G LTE"
)

data class WifiDetails(
    val ssid: String, // Hidden by default in UI
    val bssid: String, // Hidden by default in UI
    val isHiddenSsid: Boolean,
    val linkSpeed: String,
    val signalStrength: String,
    val frequency: String,
    val width: String, // Channel width
    val channel: Int,
    val standard: String, // e.g., Wi-Fi 6 (802.11ax)
    val security: String
)

data class DhcpDetails(
    val server: String,
    val leaseDuration: String,
    val gateway: String,
    val subnetMask: String,
    val dns1: String,
    val dns2: String,
    val ipAddress: String,
    val ipv6: String,
    val publicIp: String // Fetched asynchronously
)

data class NetworkHardwareDetails(
    val supportedBands: String, // e.g., 802.11 a/b/g/n/ac/ax
    val isWifiDirectSupported: Boolean,
    val isWifiAwareSupported: Boolean,
    val isPasspointSupported: Boolean,
    val is5GhzSupported: Boolean,
    val is6GhzSupported: Boolean
)

data class SimInfo(
    val slotIndex: Int,
    val carrierName: String,
    val operatorCode: String,
    val countryIso: String,
    val isRoaming: Boolean,
    val networkType: String,
    val simState: String,
    val isAvailable: Boolean
)

data class MobileDetails(
    val simState: String,
    val carrierName: String,
    val operatorCode: String,
    val countryIso: String,
    val roaming: String,
    val networkType: String, // e.g., LTE, NR
    val isDualSim: Boolean,
    val phoneType: String,
    val isEsim: Boolean,
    val dataSimSlot: Int, // 0=none, 1=SIM1, 2=SIM2
    val sim1Info: SimInfo?,
    val sim2Info: SimInfo?,
    val defaultDataSlot: Int, // 0=none, 1=SIM1, 2=SIM2
    val defaultVoiceSlot: Int,
    val defaultSmsSlot: Int
)
