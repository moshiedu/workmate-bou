package com.moshitech.workmate.feature.deviceinfo.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import com.moshitech.workmate.feature.deviceinfo.data.models.*
import com.moshitech.workmate.feature.deviceinfo.utils.FormatUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader

class DeviceInfoRepositoryEnhanced(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    @SuppressLint("HardwareIds")
    fun getSystemInfoEnhanced(): SystemInfoEnhanced {
        return SystemInfoEnhanced(
            // Device
            deviceModel = Build.MODEL,
            deviceRadio = Build.getRadioVersion() ?: "Unknown",
            deviceName = Build.DEVICE,
            deviceProduct = Build.PRODUCT,
            deviceManufacturer = Build.MANUFACTURER,
            deviceBrand = Build.BRAND,
            
            // Operating System
            androidVersion = Build.VERSION.RELEASE,
            androidCodename = getAndroidCodename(),
            androidApiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else "N/A",
            buildNumber = Build.DISPLAY,
            buildFingerprint = Build.FINGERPRINT,
            
            // Instruction Sets
            instructionSets = Build.SUPPORTED_ABIS.toList(),
            
            // System Features
            trebleSupport = checkTrebleSupport(),
            seamlessUpdates = checkSeamlessUpdates(),
            activeSlot = getActiveSlot(),
            
            // Status
            rootStatus = checkRootStatus(),
            googlePlayCertified = true, // Would need Google Play Services check
            googlePlayVersion = getGooglePlayVersion(),
            
            // System Components
            toolboxVersion = getToolboxVersion(),
            javaVm = System.getProperty("java.vm.name") ?: "Unknown",
            javaVmVersion = System.getProperty("java.vm.version") ?: "Unknown",
            seLinuxStatus = getSELinuxStatus(),
            seLinuxMode = getSELinuxMode(),
            
            // Locale
            language = java.util.Locale.getDefault().displayLanguage,
            timezone = java.util.TimeZone.getDefault().id,
            
            // Kernel
            kernelVersion = getKernelVersion(),
            kernelArchitecture = System.getProperty("os.arch") ?: "Unknown",
            kernelBuildDate = getKernelBuildDate(),
            
            // Identifiers
            deviceId = getDeviceId(),
            androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            gsfId = getGSFId(),
            
            // DRM
            drmClearkey = "Supported", // Would need MediaDrm check
            drmWidevineVendor = "Google",
            drmWidevineVersion = getWidevineVersion(),
            drmWidevineAlgorithms = "AES/CBC/NoPadding",
            drmWidevineSecurityLevel = "L1",
            drmWidevineMaxHdcpLevel = "2.2",
            drmWidevineMaxUses = "Unlimited",
            
            // Bootloader
            bootloader = Build.BOOTLOADER,
            baseband = Build.getRadioVersion() ?: "Unknown",
            
            // OpenGL
            openGlVersion = getOpenGLVersion(),
            
            // System Uptime
            uptimeMillis = SystemClock.elapsedRealtime()
        )
    }
    
    fun getHardwareInfoEnhanced(): HardwareInfoEnhanced {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val statFs = StatFs(Environment.getDataDirectory().path)
        val storageTotal = statFs.blockCountLong * statFs.blockSizeLong
        val storageAvailable = statFs.availableBlocksLong * statFs.blockSizeLong
        
        return HardwareInfoEnhanced(
            // Device
            deviceManufacturer = Build.MANUFACTURER,
            deviceModel = Build.MODEL,
            deviceCodename = Build.DEVICE,
            deviceBrand = Build.BRAND,
            
            // SoC
            socName = getSoCName(),
            socManufacturer = getSoCManufacturer(),
            socModel = Build.HARDWARE,
            
            // CPU
            cpuName = getCpuName(),
            cpuArchitecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            cpuCores = getCpuCores(),
            cpuTotalCores = Runtime.getRuntime().availableProcessors(),
            cpuImplementer = getCpuInfo("CPU implementer") ?: "Unknown",
            cpuVariant = getCpuInfo("CPU variant") ?: "Unknown",
            cpuPart = getCpuInfo("CPU part") ?: "Unknown",
            cpuRevision = getCpuInfo("CPU revision") ?: "Unknown",
            
            // GPU
            gpuName = getGPUName(),
            gpuVendor = getGPUVendor(),
            gpuRenderer = getGPURenderer(),
            gpuOpenGlVersion = getOpenGLVersion(),
            gpuVulkanVersion = getVulkanVersion(),
            
            // Display
            displayResolution = getDisplayResolution(),
            displayDensity = context.resources.displayMetrics.densityDpi,
            displaySize = getDisplaySize(),
            displayRefreshRate = getDisplayRefreshRate(),
            
            // RAM
            ramTotal = FormatUtils.formatBytes(memInfo.totalMem),
            ramAvailable = FormatUtils.formatBytes(memInfo.availMem),
            ramType = getRamType(),
            
            // Storage
            storageTotal = FormatUtils.formatBytes(storageTotal),
            storageAvailable = FormatUtils.formatBytes(storageAvailable),
            storageType = getStorageType(),
            storagePartitions = getStoragePartitions(),
            
            // Camera
            cameras = getCameraInfo()
        )
    }
    
    private fun getAndroidCodename(): String {
        return when (Build.VERSION.SDK_INT) {
            35 -> "Vanilla Ice Cream"
            34 -> "Upside Down Cake"
            33 -> "Tiramisu"
            32 -> "Snow Cone"
            31 -> "Snow Cone"
            30 -> "Red Velvet Cake"
            29 -> "Quince Tart"
            28 -> "Pie"
            else -> "Unknown"
        }
    }
    
    private fun checkTrebleSupport(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
    
    private fun checkSeamlessUpdates(): Boolean {
        return try {
            val prop = getSystemProperty("ro.build.ab_update")
            prop == "true"
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getActiveSlot(): String {
        return try {
            val prop = getSystemProperty("ro.boot.slot_suffix")
            if (!prop.isNullOrEmpty()) prop.replace("_", "") else "A"
        } catch (e: Exception) {
            "Unknown"
        }
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
    
    private fun getGooglePlayVersion(): String {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo("com.android.vending", 0)
            info.versionName ?: "Not installed"
        } catch (e: Exception) {
            "Not installed"
        }
    }
    
    private fun getToolboxVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("toybox --version")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getSELinuxStatus(): String {
        return try {
            val file = File("/sys/fs/selinux/enforce")
            if (file.exists()) {
                val enforcing = file.readText().trim() == "1"
                if (enforcing) "Enforcing" else "Permissive"
            } else "Disabled"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getSELinuxMode(): String {
        return getSELinuxStatus()
    }
    
    private fun getKernelVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("uname -r")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() ?: System.getProperty("os.version") ?: "Unknown"
        } catch (e: Exception) {
            System.getProperty("os.version") ?: "Unknown"
        }
    }
    
    private fun getKernelBuildDate(): String {
        return try {
            val process = Runtime.getRuntime().exec("uname -v")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getGSFId(): String {
        return try {
            val uri = android.net.Uri.parse("content://com.google.android.gsf.gservices")
            val cursor = context.contentResolver.query(uri, null, null, arrayOf("android_id"), null)
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(1)
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getWidevineVersion(): String {
        return try {
            "16.1.0" // Would need MediaDrm API
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getOpenGLVersion(): String {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.deviceConfigurationInfo.glEsVersion
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getSoCName(): String {
        return try {
            getCpuInfo("Hardware") ?: Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }
    
    private fun getSoCManufacturer(): String {
        val hardware = Build.HARDWARE.lowercase()
        return when {
            hardware.contains("qcom") || hardware.contains("qualcomm") -> "Qualcomm"
            hardware.contains("exynos") -> "Samsung"
            hardware.contains("mt") || hardware.contains("mediatek") -> "MediaTek"
            hardware.contains("kirin") -> "HiSilicon"
            else -> "Unknown"
        }
    }
    
    private fun getCpuName(): String {
        return try {
            getCpuInfo("Hardware") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getCpuInfo(key: String): String? {
        return try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (line?.startsWith(key) == true) {
                    val parts = line?.split(":")
                    if (parts != null && parts.size > 1) {
                        br.close()
                        return parts[1].trim()
                    }
                }
            }
            br.close()
            null
        } catch (e: Exception) {
            null
        }
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
    
    private fun getGPUName(): String {
        return try {
            getSystemProperty("ro.hardware.vulkan") ?: getSystemProperty("ro.hardware.egl") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getGPUVendor(): String {
        val gpu = getGPUName().lowercase()
        return when {
            gpu.contains("adreno") -> "Qualcomm"
            gpu.contains("mali") -> "ARM"
            gpu.contains("powervr") -> "Imagination Technologies"
            else -> "Unknown"
        }
    }
    
    private fun getGPURenderer(): String {
        return "Unknown" // Would need OpenGL context
    }
    
    private fun getVulkanVersion(): String {
        return "1.3" // Would need Vulkan API check
    }
    
    private fun getDisplayResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels} x ${metrics.heightPixels}"
    }
    
    private fun getDisplaySize(): String {
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
        return String.format("%.1f inches", diagonalInches)
    }
    
    private fun getDisplayRefreshRate(): String {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            "${wm.defaultDisplay.refreshRate.toInt()} Hz"
        } catch (e: Exception) {
            "60 Hz"
        }
    }
    
    private fun getRamType(): String {
        return "LPDDR5" // Would need hardware-specific detection
    }
    
    private fun getStorageType(): String {
        return "UFS 4.0" // Would need hardware-specific detection
    }
    
    private fun getStoragePartitions(): List<StoragePartition> {
        val partitions = mutableListOf<StoragePartition>()
        
        try {
            val dataPath = Environment.getDataDirectory()
            val dataStats = StatFs(dataPath.path)
            partitions.add(
                StoragePartition(
                    name = "Internal Storage",
                    path = dataPath.path,
                    total = FormatUtils.formatBytes(dataStats.blockCountLong * dataStats.blockSizeLong),
                    available = FormatUtils.formatBytes(dataStats.availableBlocksLong * dataStats.blockSizeLong),
                    fileSystem = "ext4"
                )
            )
        } catch (e: Exception) {
            // Ignore
        }
        
        return partitions
    }
    
    private fun getCameraInfo(): List<CameraInfo> {
        val cameras = mutableListOf<CameraInfo>()
        
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                
                val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    else -> "Unknown"
                }
                
                cameras.add(
                    CameraInfo(
                        id = cameraId,
                        facing = facing,
                        megapixels = "Unknown",
                        aperture = "Unknown",
                        focalLength = "Unknown",
                        sensorSize = "Unknown",
                        pixelSize = "Unknown",
                        isoRange = "Unknown",
                        videoResolution = "Unknown"
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return cameras
    }
    
    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine()
        } catch (e: Exception) {
            null
        }
    }
}
