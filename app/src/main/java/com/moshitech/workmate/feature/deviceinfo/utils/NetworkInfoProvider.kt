package com.moshitech.workmate.feature.deviceinfo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.moshitech.workmate.feature.deviceinfo.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.Collections

class NetworkInfoProvider(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun getNetworkInfo(): NetworkInfo {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        val connectionType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> ConnectionType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }

        val isConnected = connectionType != ConnectionType.NONE

        return NetworkInfo(
            connectionStatus = getConnectionStatus(connectionType, capabilities),
            wifiDetails = if (connectionType == ConnectionType.WIFI) getWifiDetails() else null,
            mobileDetails = getMobileDetails(),
            dhcpDetails = if (isConnected) getDhcpDetails() else null,
            hardwareDetails = getHardwareDetails()
        )
    }

    private fun getConnectionStatus(type: ConnectionType, capabilities: NetworkCapabilities?): ConnectionStatus {
        val signalStrengthDbm: Int
        val signalStrengthPercent: Int
        val linkSpeed: Int

        if (type == ConnectionType.WIFI) {
            val wifiInfo = wifiManager.connectionInfo
            signalStrengthDbm = wifiInfo.rssi
            signalStrengthPercent = WifiManager.calculateSignalLevel(wifiInfo.rssi, 100)
            linkSpeed = wifiInfo.linkSpeed
        } else {
            // Placeholder for cellular signal strength (requires simpler logic for now)
            signalStrengthDbm = -1
            signalStrengthPercent = 0
            linkSpeed = 0
        }

        return ConnectionStatus(
            isConnected = type != ConnectionType.NONE,
            type = type,
            signalStrengthPercent = signalStrengthPercent,
            signalStrengthDbm = signalStrengthDbm,
            linkSpeedMbps = linkSpeed,
            description = when (type) {
                ConnectionType.WIFI -> "Wi-Fi"
                ConnectionType.CELLULAR -> "Cellular"
                ConnectionType.ETHERNET -> "Ethernet"
                else -> "Disconnected"
            }
        )
    }

    private fun getWifiDetails(): WifiDetails {
        val info = wifiManager.connectionInfo
        val dhcp = wifiManager.dhcpInfo
        
        // Frequency to Channel conversion (approximate)
        val frequency = info.frequency
        val channel = if (frequency >= 2412 && frequency <= 2484) {
            (frequency - 2412) / 5 + 1
        } else if (frequency >= 5170 && frequency <= 5825) {
            (frequency - 5170) / 5 + 34
        } else {
            0
        }

        // Standard detection (simplified)
        val standard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (info.wifiStandard) {
                android.net.wifi.ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)"
                android.net.wifi.ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)"
                android.net.wifi.ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)"
                else -> "802.11 a/b/g"
            }
        } else {
            "802.11 a/b/g/n/ac"
        }

        return WifiDetails(
            ssid = info.ssid.removeSurrounding("\""),
            bssid = info.bssid ?: "Unavailable",
            isHiddenSsid = info.hiddenSSID,
            linkSpeed = "${info.linkSpeed} Mbps",
            signalStrength = "${info.rssi} dBm",
            frequency = "$frequency MHz",
            width = "20 MHz", // Difficult to get exact width without higher API or scan results
            channel = channel,
            standard = standard,
            security = "WPA/WPA2" // Placeholder, requires ScanResult to be accurate
        )
    }

    private fun getDhcpDetails(): DhcpDetails {
        val dhcp = wifiManager.dhcpInfo
        
        return DhcpDetails(
            server = intToIp(dhcp.serverAddress),
            leaseDuration = "${dhcp.leaseDuration / 3600} hours",
            gateway = intToIp(dhcp.gateway),
            subnetMask = intToIp(dhcp.netmask),
            dns1 = intToIp(dhcp.dns1),
            dns2 = intToIp(dhcp.dns2),
            ipAddress = intToIp(dhcp.ipAddress),
            ipv6 = getIpv6Address(),
            publicIp = "Tap to show" // Placeholder
        )
    }

    private fun getMobileDetails(): MobileDetails {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return MobileDetails(
                simState = "Permission Required",
                carrierName = "-",
                operatorCode = "-",
                countryIso = "-",
                roaming = "-",
                networkType = "-",
                isDualSim = false,
                phoneType = "-",
                isEsim = false,
                dataSimSlot = 0,
                sim1Info = null,
                sim2Info = null,
                defaultDataSlot = 0,
                defaultVoiceSlot = 0,
                defaultSmsSlot = 0
            )
        }

        val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (telephonyManager.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                else -> "Unknown"
            }
        } else {
            "Unknown"
        }

        val isDualSim = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) telephonyManager.phoneCount > 1 else false
        
        // Get SIM info using SubscriptionManager
        val (sim1Info, sim2Info) = getSimInfos()
        val (defaultDataSlot, defaultVoiceSlot, defaultSmsSlot) = getDefaultSlots()

        return MobileDetails(
            simState = getSimStateString(telephonyManager.simState),
            carrierName = telephonyManager.networkOperatorName ?: "Unknown",
            operatorCode = telephonyManager.networkOperator ?: "-",
            countryIso = telephonyManager.networkCountryIso?.uppercase() ?: "-",
            roaming = if (telephonyManager.isNetworkRoaming) "Roaming" else "Not Roaming",
            networkType = networkType,
            isDualSim = isDualSim,
            phoneType = when (telephonyManager.phoneType) {
                TelephonyManager.PHONE_TYPE_GSM -> "GSM"
                TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
                else -> "None"
            },
            isEsim = sim1Info?.let { it.isAvailable && it.carrierName.contains("eSIM", ignoreCase = true) } ?: false,
            dataSimSlot = defaultDataSlot,
            sim1Info = sim1Info,
            sim2Info = sim2Info,
            defaultDataSlot = defaultDataSlot,
            defaultVoiceSlot = defaultVoiceSlot,
            defaultSmsSlot = defaultSmsSlot
        )
    }

    private fun getSimInfos(): Pair<SimInfo?, SimInfo?> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return Pair(null, null)
        }

        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
                ?: return Pair(null, null)

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return Pair(null, null)
            }

            val subscriptions = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
            
            var sim1: SimInfo? = null
            var sim2: SimInfo? = null

            for (sub in subscriptions) {
                val slotIndex = sub.simSlotIndex
                if (slotIndex == 0 || slotIndex == 1) {
                    val simInfo = SimInfo(
                        slotIndex = slotIndex + 1, // Convert to 1-based
                        carrierName = sub.carrierName?.toString() ?: "Unknown",
                        operatorCode = sub.mcc.toString() + sub.mnc.toString(),
                        countryIso = sub.countryIso?.uppercase() ?: "-",
                        isRoaming = false, // Would need TelephonyManager per subscription
                        networkType = getNetworkTypeForSubscription(sub.subscriptionId),
                        simState = "Ready",
                        isAvailable = true
                    )
                    
                    if (slotIndex == 0) sim1 = simInfo
                    else if (slotIndex == 1) sim2 = simInfo
                }
            }

            // If SIM slot exists but no subscription, mark as not available
            val phoneCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) telephonyManager.phoneCount else 1
            if (sim1 == null && phoneCount >= 1) {
                sim1 = SimInfo(1, "Not Available", "-", "-", false, "-", "Absent", false)
            }
            if (sim2 == null && phoneCount >= 2) {
                sim2 = SimInfo(2, "Not Available", "-", "-", false, "-", "Absent", false)
            }

            return Pair(sim1, sim2)
        } catch (e: Exception) {
            return Pair(null, null)
        }
    }

    private fun getNetworkTypeForSubscription(subscriptionId: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return "Unknown"
        
        try {
            val tm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                telephonyManager.createForSubscriptionId(subscriptionId)
            } else {
                telephonyManager
            }
            
            return when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            return "Unknown"
        }
    }

    private fun getDefaultSlots(): Triple<Int, Int, Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return Triple(0, 0, 0)
        }

        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
                ?: return Triple(0, 0, 0)

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return Triple(0, 0, 0)
            }

            val dataSubId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.telephony.SubscriptionManager.getDefaultDataSubscriptionId()
            } else {
                -1
            }

            val voiceSubId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.telephony.SubscriptionManager.getDefaultVoiceSubscriptionId()
            } else {
                -1
            }

            val smsSubId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.telephony.SubscriptionManager.getDefaultSmsSubscriptionId()
            } else {
                -1
            }

            val subscriptions = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
            
            val dataSlot = subscriptions.find { it.subscriptionId == dataSubId }?.simSlotIndex?.plus(1) ?: 0
            val voiceSlot = subscriptions.find { it.subscriptionId == voiceSubId }?.simSlotIndex?.plus(1) ?: 0
            val smsSlot = subscriptions.find { it.subscriptionId == smsSubId }?.simSlotIndex?.plus(1) ?: 0

            return Triple(dataSlot, voiceSlot, smsSlot)
        } catch (e: Exception) {
            return Triple(0, 0, 0)
        }
    }

    private fun getHardwareDetails(): NetworkHardwareDetails {
        return NetworkHardwareDetails(
            supportedBands = "802.11 a/b/g/n/ac/ax",
            isWifiDirectSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT),
            isWifiAwareSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
            } else false,
            isPasspointSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_PASSPOINT),
            is5GhzSupported = wifiManager.is5GHzBandSupported,
            is6GhzSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiManager.is6GHzBandSupported
            } else false
        )
    }

    suspend fun fetchPublicIp(): String = withContext(Dispatchers.IO) {
        try {
            URL("https://api.ipify.org").readText()
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    private fun intToIp(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun getIpv6Address(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in Collections.list(interfaces)) {
                for (addr in Collections.list(intf.inetAddresses)) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet6Address) {
                        return addr.hostAddress?.substringBefore("%") ?: ""
                    }
                }
            }
        } catch (e: Exception) { }
        return "Unavailable"
    }

    private fun getSimStateString(state: Int): String {
        return when (state) {
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "Absent"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            else -> "Unknown"
        }
    }
}
