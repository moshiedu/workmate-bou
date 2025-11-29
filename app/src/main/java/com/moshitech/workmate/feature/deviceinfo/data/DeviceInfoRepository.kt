package com.moshitech.workmate.feature.deviceinfo.data

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.moshitech.workmate.feature.deviceinfo.data.models.CpuCore
import com.moshitech.workmate.feature.deviceinfo.data.models.DashboardInfo
import com.moshitech.workmate.feature.deviceinfo.data.models.HardwareInfo
import com.moshitech.workmate.feature.deviceinfo.data.models.SystemInfo
import com.moshitech.workmate.feature.deviceinfo.utils.FormatUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader

class DeviceInfoRepository(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    fun getDashboardInfo(): DashboardInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val statFs = StatFs(Environment.getDataDirectory().path)
        val storageTotal = statFs.blockCountLong * statFs.blockSizeLong
        val storageAvailable = statFs.availableBlocksLong * statFs.blockSizeLong
        val storageUsed = storageTotal - storageAvailable

        // Battery Info
        val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.let {
            val level = it.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) {
                (level / scale.toFloat() * 100).toInt()
            } else 0
        } ?: 0
        
        val status = batteryIntent?.let {
            when (it.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)) {
                android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }
        } ?: "Unknown"
        
        val temperature = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10f) ?: 0f
        
        return DashboardInfo(
            cpuFrequency = getCurrentCpuFrequency(),
            ramUsed = memInfo.totalMem - memInfo.availMem,
            ramTotal = memInfo.totalMem,
            storageUsed = storageUsed,
            storageTotal = storageTotal,
            uptime = FormatUtils.formatUptime(SystemClock.elapsedRealtime()),
            batteryLevel = level,
            batteryStatus = status,
            batteryTemperature = temperature,
            cpuCores = getCpuCores(),
            dataSent = android.net.TrafficStats.getTotalTxBytes(),
            dataReceived = android.net.TrafficStats.getTotalRxBytes()
        )
    }
    
    fun getHardwareInfo(): HardwareInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val statFs = StatFs(Environment.getDataDirectory().path)
        val storageTotal = statFs.blockCountLong * statFs.blockSizeLong
        
        return HardwareInfo(
            deviceManufacturer = Build.MANUFACTURER,
            deviceModel = Build.MODEL,
            deviceCodename = Build.DEVICE,
            deviceBrand = Build.BRAND,
            cpuName = getCpuName(),
            cpuArchitecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuMaxFrequency = getMaxCpuFrequency(),
            displayResolution = getDisplayResolution(),
            displayDensity = context.resources.displayMetrics.densityDpi,
            ramTotal = FormatUtils.formatBytes(memInfo.totalMem),
            storageTotal = FormatUtils.formatBytes(storageTotal)
        )
    }
    
    fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            androidVersion = Build.VERSION.RELEASE,
            androidApiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else "N/A",
            kernelVersion = getKernelVersion(),
            buildNumber = Build.DISPLAY,
            buildFingerprint = Build.FINGERPRINT,
            bootloader = Build.BOOTLOADER,
            baseband = Build.getRadioVersion() ?: "Unknown",
            javaVm = System.getProperty("java.vm.name") ?: "Unknown",
            uptimeMillis = SystemClock.elapsedRealtime(),
            rootStatus = checkRootStatus()
        )
    }
    
    private fun getCpuName(): String {
        return try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (line?.startsWith("Hardware") == true) {
                    val parts = line?.split(":")
                    if (parts != null && parts.size > 1) {
                        br.close()
                        return parts[1].trim()
                    }
                }
            }
            br.close()
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getCurrentCpuFrequency(): String {
        return try {
            val br = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"))
            val freq = br.readLine()?.toLongOrNull() ?: 0L
            br.close()
            FormatUtils.formatFrequency(freq)
        } catch (e: Exception) {
            "--"
        }
    }
    
    private fun getMaxCpuFrequency(): String {
        return try {
            val br = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"))
            val freq = br.readLine()?.toLongOrNull() ?: 0L
            br.close()
            FormatUtils.formatFrequency(freq)
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getKernelVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("uname -r")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val version = reader.readLine() ?: "Unknown"
            reader.close()
            version
        } catch (e: Exception) {
            System.getProperty("os.version") ?: "Unknown"
        }
    }
    
    private fun getDisplayResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels} x ${metrics.heightPixels}"
    }
    
    private fun checkRootStatus(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun getCpuCores(): List<CpuCore> {
        val cores = mutableListOf<CpuCore>()
        val coreCount = Runtime.getRuntime().availableProcessors()
        
        for (i in 0 until coreCount) {
            val maxFreq = readCpuFreq(i, "cpuinfo_max_freq")
            val minFreq = readCpuFreq(i, "cpuinfo_min_freq")
            val curFreq = readCpuFreq(i, "scaling_cur_freq")
            
            cores.add(
                CpuCore(
                    name = "Core $i",
                    minFrequency = minFreq,
                    maxFrequency = maxFreq,
                    currentFrequency = curFreq
                )
            )
        }
        
        return cores
    }
    
    private fun readCpuFreq(core: Int, file: String): Long {
        return try {
            val path = "/sys/devices/system/cpu/cpu$core/cpufreq/$file"
            File(path).readText().trim().toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
