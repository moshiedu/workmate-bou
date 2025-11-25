package com.moshitech.workmate.feature.deviceinfo.data

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
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
        
        return DashboardInfo(
            cpuFrequency = getCurrentCpuFrequency(),
            ramUsed = memInfo.totalMem - memInfo.availMem,
            ramTotal = memInfo.totalMem,
            storageUsed = storageUsed,
            storageTotal = storageTotal,
            uptime = FormatUtils.formatUptime(SystemClock.elapsedRealtime())
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
}
