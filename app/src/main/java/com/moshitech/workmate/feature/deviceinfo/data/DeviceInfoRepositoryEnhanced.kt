package com.moshitech.workmate.feature.deviceinfo.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.Sensor
import android.hardware.SensorManager
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
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.SSLContext
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.media.MediaRecorder
import android.util.Range
import android.util.Size
import android.hardware.camera2.params.StreamConfigurationMap

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
            uptimeMillis = SystemClock.elapsedRealtime(),
            
            // Network Security
            sslVersion = getSSLVersion()
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
            cpuArchitecture = System.getProperty("os.arch") ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
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
            cameras = getCameraInfo(),
            
            // Sensors
            sensors = getSensors(),
            sensorCount = getSensors().size
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
            val hardware = getCpuInfo("Hardware") ?: Build.HARDWARE
            getSoCMarketingName(hardware)
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }

    private fun getSoCMarketingName(hardware: String): String {
        val hw = hardware.lowercase()
        return when {
            hw.contains("sm8650") -> "Snapdragon 8 Gen 3"
            hw.contains("sm8550") -> "Snapdragon 8 Gen 2"
            hw.contains("sm8475") -> "Snapdragon 8+ Gen 1"
            hw.contains("sm8450") -> "Snapdragon 8 Gen 1"
            hw.contains("sm8350") -> "Snapdragon 888"
            hw.contains("sm8250") -> "Snapdragon 865"
            hw.contains("sm8150") -> "Snapdragon 855"
            hw.contains("sm7475") -> "Snapdragon 7+ Gen 2"
            hw.contains("sm7550") -> "Snapdragon 7 Gen 3"
            hw.contains("sm7450") -> "Snapdragon 7 Gen 1"
            hw.contains("sm7325") -> "Snapdragon 778G"
            hw.contains("sm7250") -> "Snapdragon 765G"
            hw.contains("sm7150") -> "Snapdragon 730G"
            hw.contains("sm6375") -> "Snapdragon 695"
            hw.contains("sm6225") -> "Snapdragon 680"
            hw.contains("mt6989") -> "Dimensity 9300"
            hw.contains("mt6985") -> "Dimensity 9200"
            hw.contains("mt6983") -> "Dimensity 9000"
            hw.contains("mt6893") -> "Dimensity 1200"
            hw.contains("mt6891") -> "Dimensity 1100"
            hw.contains("mt6877") -> "Dimensity 900"
            hw.contains("tensor") -> "Google Tensor"
            hw.contains("zuma") -> "Google Tensor G3"
            hw.contains("cheetah") -> "Google Tensor G2"
            hw.contains("whitechapel") -> "Google Tensor"
            else -> hardware
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
            // Try to find "model name" first (common on some kernels)
            var name = getCpuInfo("model name")
            if (name == null) {
                name = getCpuInfo("Processor")
            }
            if (name == null) {
                name = getCpuInfo("Hardware")
            }
            name ?: "Unknown"
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
        return try {
            // Try to get from system property first
            val renderer = getSystemProperty("ro.hardware.egl")
            if (!renderer.isNullOrEmpty()) return renderer

            // Fallback to creating a dummy EGL context
            getGpuRendererFromEgl()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getGpuRendererFromEgl(): String {
        var display: EGLDisplay? = null
        var context: EGLContext? = null
        var surface: EGLSurface? = null

        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display === EGL14.EGL_NO_DISPLAY) return "Unknown"

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) return "Unknown"

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)
            if (numConfigs[0] == 0) return "Unknown"

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )
            surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)

            EGL14.eglMakeCurrent(display, surface, surface, context)

            return GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
        } catch (e: Exception) {
            return "Unknown"
        } finally {
            if (display != null) {
                EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                if (surface != null) EGL14.eglDestroySurface(display, surface)
                if (context != null) EGL14.eglDestroyContext(display, context)
                EGL14.eglTerminate(display)
            }
        }
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
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front camera"
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear camera"
                    else -> "Unknown"
                }
                
                // Calculate megapixels and resolution
                val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val megapixels = if (pixelArraySize != null) {
                    val pixels = pixelArraySize.width.toLong() * pixelArraySize.height.toLong()
                    String.format("%.1f MP", pixels / 1_000_000.0)
                } else {
                    "Unknown"
                }
                
                val resolution = if (pixelArraySize != null) {
                    "${pixelArraySize.width}x${pixelArraySize.height}"
                } else {
                    "Unknown"
                }

                // Sensor size
                val sensorSizeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val sensorSize = if (sensorSizeRect != null) {
                    val diagonal = Math.sqrt((sensorSizeRect.width * sensorSizeRect.width + sensorSizeRect.height * sensorSizeRect.height).toDouble())
                    String.format("1/%.1f\"", 43.27 / diagonal)
                } else {
                    "Unknown"
                }
                
                val sensorSizeMm = if (sensorSizeRect != null) {
                    String.format("%.2f x %.2f mm", sensorSizeRect.width, sensorSizeRect.height)
                } else {
                    "Unknown"
                }

                // Pixel size
                val pixelSize = if (pixelArraySize != null && sensorSizeRect != null) {
                    val pixelSizeMicrons = (sensorSizeRect.width * 1000) / pixelArraySize.width
                    String.format("%.2f µm", pixelSizeMicrons)
                } else {
                    "Unknown"
                }
                
                // Filter color arrangement
                val cfa = characteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)
                val filterArrangement = when (cfa) {
                    CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB -> "RGGB"
                    CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG -> "GRBG"
                    CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG -> "GBRG"
                    CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR -> "BGGR"
                    CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGB -> "RGB"
                    else -> "Unknown"
                }

                // Aperture
                val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                val aperture = if (apertures != null && apertures.isNotEmpty()) {
                    "f/${apertures[0]}"
                } else {
                    "Unknown"
                }

                // Focal length
                val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val focalLength = if (focalLengths != null && focalLengths.isNotEmpty()) {
                    String.format("%.1f mm", focalLengths[0])
                } else {
                    "Unknown"
                }
                
                // 35mm Equivalent Focal Length and Crop Factor
                var focalLength35mm = "Unknown"
                var cropFactor = "Unknown"
                if (focalLengths != null && focalLengths.isNotEmpty() && sensorSizeRect != null) {
                    val sensorDiagonal = Math.sqrt((sensorSizeRect.width * sensorSizeRect.width + sensorSizeRect.height * sensorSizeRect.height).toDouble())
                    val cropFactorVal = 43.27 / sensorDiagonal
                    val eq = focalLengths[0] * cropFactorVal
                    focalLength35mm = String.format("%.0f mm", eq)
                    cropFactor = String.format("%.1fx", cropFactorVal)
                }
                
                // Field of View
                val fieldOfView = if (focalLengths != null && focalLengths.isNotEmpty() && sensorSizeRect != null) {
                    val fov = 2 * Math.atan((sensorSizeRect.width.toDouble() / 2) / focalLengths[0].toDouble()) * (180 / Math.PI)
                    String.format("%.1f° Horizontal", fov)
                } else {
                    "Unknown"
                }
                
                // Shutter Speed Range (Exposure Time)
                val exposureRangeVal = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                val shutterSpeedRange = if (exposureRangeVal != null) {
                    val minSeconds = exposureRangeVal.lower.toDouble() / 1_000_000_000.0
                    val maxSeconds = exposureRangeVal.upper.toDouble() / 1_000_000_000.0
                    String.format("1/%.0f - 1/%.1f s", 1/minSeconds, 1/maxSeconds)
                } else {
                    "Unknown"
                }
                
                // ISO Range
                val isoRangeVal = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val isoRange = if (isoRangeVal != null) {
                    "${isoRangeVal.lower} - ${isoRangeVal.upper}"
                } else {
                    "Unknown"
                }

                // Exposure Range
                val exposureRange = if (exposureRangeVal != null) {
                    val min = exposureRangeVal.lower.toDouble() / 1_000_000_000.0
                    val max = exposureRangeVal.upper.toDouble() / 1_000_000_000.0
                    String.format("%.5fs - %.0fs", min, max)
                } else {
                    "Unknown"
                }
                
                // Boolean capabilities
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                
                val ois = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                val hasOpticalStabilization = ois != null && ois.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON)
                
                val videoStab = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                val hasVideoStabilization = videoStab != null && videoStab.any { it != CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_OFF }
                
                val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                val hasAfLock = afModes != null && afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)
                
                val awbModes = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
                val hasWbLock = awbModes != null && awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_AUTO)

                // Capabilities
                val capabilities = mutableListOf<String>()
                val caps = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                if (caps != null) {
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) capabilities.add("RAW mode")
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) capabilities.add("Manual sensor")
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING)) capabilities.add("Manual post-processing")
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)) capabilities.add("Logical multi-camera")
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT)) capabilities.add("Depth output")
                    if (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE)) capabilities.add("Burst")
                }
                
                // Exposure Modes
                val exposureModes = mutableListOf<String>()
                val aeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
                if (aeModes != null) {
                    if (aeModes.contains(CameraCharacteristics.CONTROL_AE_MODE_OFF)) exposureModes.add("Manual")
                    if (aeModes.contains(CameraCharacteristics.CONTROL_AE_MODE_ON)) exposureModes.add("Auto")
                }
                
                // Autofocus Modes
                val autofocusModes = mutableListOf<String>()
                if (afModes != null) {
                    if (afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_OFF)) autofocusModes.add("Manual")
                    if (afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) autofocusModes.add("Auto")
                    if (afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_MACRO)) autofocusModes.add("Macro")
                    if (afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) autofocusModes.add("Continuous video")
                    if (afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) autofocusModes.add("Continuous picture")
                }
                
                // White Balance Modes
                val whiteBalanceModes = mutableListOf<String>()
                if (awbModes != null) {
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_OFF)) whiteBalanceModes.add("Manual")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_AUTO)) whiteBalanceModes.add("Auto")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT)) whiteBalanceModes.add("Incandescent")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT)) whiteBalanceModes.add("Fluorescent")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT)) whiteBalanceModes.add("Warm Fluorescent")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT)) whiteBalanceModes.add("Daylight")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT)) whiteBalanceModes.add("Cloudy")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT)) whiteBalanceModes.add("Twilight")
                    if (awbModes.contains(CameraCharacteristics.CONTROL_AWB_MODE_SHADE)) whiteBalanceModes.add("Shade")
                }
                
                // Scene Modes
                val sceneModes = mutableListOf<String>()
                val sceneModesList = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
                if (sceneModesList != null) {
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED)) sceneModes.add("Off")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_FACE_PRIORITY)) sceneModes.add("Face priority")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_ACTION)) sceneModes.add("Action")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT)) sceneModes.add("Portrait")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE)) sceneModes.add("Landscape")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT)) sceneModes.add("Night")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT)) sceneModes.add("Night portrait")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE)) sceneModes.add("Theatre")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_BEACH)) sceneModes.add("Beach")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_SNOW)) sceneModes.add("Snow")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET)) sceneModes.add("Sunset")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO)) sceneModes.add("Steady photo")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS)) sceneModes.add("Fireworks")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS)) sceneModes.add("Sports")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_PARTY)) sceneModes.add("Party")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT)) sceneModes.add("Candlelight")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE)) sceneModes.add("Barcode")
                    if (sceneModesList.contains(CameraCharacteristics.CONTROL_SCENE_MODE_HDR)) sceneModes.add("HDR")
                }
                
                // Color Effects
                val colorEffects = mutableListOf<String>()
                val effectsList = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
                if (effectsList != null) {
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_OFF)) colorEffects.add("Off")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_MONO)) colorEffects.add("Mono")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE)) colorEffects.add("Negative")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE)) colorEffects.add("Solarize")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA)) colorEffects.add("Sepia")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE)) colorEffects.add("Posterize")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD)) colorEffects.add("Whiteboard")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD)) colorEffects.add("Blackboard")
                    if (effectsList.contains(CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA)) colorEffects.add("Aqua")
                }
                
                // Face Detection
                val maxFaceCount = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT) ?: 0
                val faceDetectModes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)
                val faceDetectMode = when {
                    faceDetectModes == null -> "None"
                    faceDetectModes.contains(CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_FULL) -> "Full"
                    faceDetectModes.contains(CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE) -> "simple"
                    else -> "Off"
                }
                
                // Camera2 API Level
                val hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val camera2ApiLevel = when (hardwareLevel) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                    else -> "Unknown"
                }

                // Video Modes and Profiles
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val videoModes = mutableListOf<String>()
                val videoProfiles = mutableListOf<String>()
                var maxFrameRate = "Unknown"
                var hasHighSpeedVideo = false
                
                if (map != null) {
                    val sizes = map.getOutputSizes(MediaRecorder::class.java)
                    if (sizes != null) {
                        val commonSizes = sizes.filter { 
                            (it.width == 3840 && it.height == 2160) || // 4K
                            (it.width == 1920 && it.height == 1080) || // 1080p
                            (it.width == 1280 && it.height == 720)     // 720p
                        }.sortedByDescending { it.width * it.height }
                        
                        commonSizes.forEach { size ->
                            val label = when {
                                size.width == 3840 -> "4K"
                                size.width == 1920 -> "1080p"
                                size.width == 1280 -> "720p"
                                else -> "${size.width}x${size.height}"
                            }
                            videoModes.add(label)
                            
                            // Get frame rates for this size
                            try {
                                val fpsRanges = map.getHighSpeedVideoFpsRangesFor(size)
                                if (fpsRanges != null && fpsRanges.isNotEmpty()) {
                                    hasHighSpeedVideo = true
                                    val rates = fpsRanges.map { "${it.upper}" }.distinct().joinToString(", ")
                                    videoProfiles.add("$label @ $rates Hz")
                                    
                                    val maxFps = fpsRanges.maxOfOrNull { it.upper } ?: 0
                                    if (maxFrameRate == "Unknown" || maxFps > maxFrameRate.replace(" Hz", "").toIntOrNull() ?: 0) {
                                        maxFrameRate = "$maxFps Hz"
                                    }
                                }
                            } catch (e: Exception) {
                                // High speed video not supported for this size
                            }
                        }
                    }
                }
                
                // Check for HDR support
                val hasHdr = caps != null && (caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING) ||
                                              caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING))

                cameras.add(
                    CameraInfo(
                        id = cameraId,
                        facing = facing,
                        megapixels = megapixels,
                        resolution = resolution,
                        sensorSize = sensorSizeMm,
                        pixelSize = pixelSize,
                        filterColorArrangement = filterArrangement,
                        aperture = aperture,
                        focalLength = focalLength,
                        focalLength35mm = focalLength35mm,
                        cropFactor = cropFactor,
                        fieldOfView = fieldOfView,
                        shutterSpeedRange = shutterSpeedRange,
                        isoRange = isoRange,
                        exposureRange = exposureRange,
                        hasFlash = hasFlash,
                        hasVideoStabilization = hasVideoStabilization,
                        hasOpticalStabilization = hasOpticalStabilization,
                        hasAfLock = hasAfLock,
                        hasWbLock = hasWbLock,
                        capabilities = capabilities,
                        exposureModes = exposureModes,
                        autofocusModes = autofocusModes,
                        whiteBalanceModes = whiteBalanceModes,
                        sceneModes = sceneModes,
                        colorEffects = colorEffects,
                        maxFaceCount = maxFaceCount,
                        faceDetectMode = faceDetectMode,
                        camera2ApiLevel = camera2ApiLevel,
                        videoModes = videoModes.distinct(),
                        videoResolution = if (videoModes.isNotEmpty()) videoModes.first() else "Unknown",
                        videoProfiles = videoProfiles,
                        maxFrameRate = maxFrameRate,
                        hasHighSpeedVideo = hasHighSpeedVideo,
                        hasHdr = hasHdr
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

    private fun getSSLVersion(): String {
        return try {
            val sslContext = SSLContext.getDefault()
            val engine = sslContext.createSSLEngine()
            val protocols = engine.supportedProtocols
            protocols.joinToString(", ")
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getSensors(): List<SensorInfo> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        
        return sensors.map { sensor ->
            val isWakeUp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sensor.isWakeUpSensor
            } else {
                false
            }
            
            val isDynamic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sensor.isDynamicSensor
            } else {
                false
            }
            
            val reportingMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                when (sensor.reportingMode) {
                    Sensor.REPORTING_MODE_CONTINUOUS -> "Continuous"
                    Sensor.REPORTING_MODE_ON_CHANGE -> "On Change"
                    Sensor.REPORTING_MODE_ONE_SHOT -> "One Shot"
                    Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> "Special Trigger"
                    else -> "Unknown"
                }
            } else {
                "Unknown"
            }
            
            SensorInfo(
                name = sensor.name,
                vendor = sensor.vendor,
                version = sensor.version,
                type = sensor.type,
                typeString = sensor.stringType ?: "Unknown",
                power = sensor.power,
                resolution = sensor.resolution,
                maxRange = sensor.maximumRange,
                isWakeUpSensor = isWakeUp,
                isDynamicSensor = isDynamic,
                reportingMode = reportingMode
            )
        }
    }
}
