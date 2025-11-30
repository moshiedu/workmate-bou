package com.moshitech.workmate.feature.deviceinfo.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.deviceinfo.model.SecurityCheck
import com.moshitech.workmate.feature.deviceinfo.model.SecurityScanResult
import com.moshitech.workmate.feature.deviceinfo.model.SecuritySeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class IntegrityCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<SecurityScanResult?>(null)
    val scanResult: StateFlow<SecurityScanResult?> = _scanResult.asStateFlow()

    private val _scanHistory = MutableStateFlow<List<SecurityScanResult>>(emptyList())
    val scanHistory: StateFlow<List<SecurityScanResult>> = _scanHistory.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1500) // Simulate scanning animation
            
            val checks = withContext(Dispatchers.IO) {
                performSecurityChecks()
            }
            
            val score = calculateSecurityScore(checks)
            val result = SecurityScanResult(
                timestamp = System.currentTimeMillis(),
                score = score,
                checks = checks,
                overallStatus = score >= 70
            )
            
            _scanResult.value = result
            _scanHistory.value = listOf(result) + _scanHistory.value.take(9) // Keep last 10
            _isScanning.value = false
        }
    }

    private fun performSecurityChecks(): List<SecurityCheck> {
        val context = getApplication<Application>()
        return listOf(
            checkRootAccess(),
            checkEmulator(),
            checkUsbDebugging(context),
            checkDeveloperOptions(context),
            checkScreenLock(context),
            checkUnknownSources(context),
            checkEncryption(context),
            checkPlayProtect(context)
        )
    }

    private fun calculateSecurityScore(checks: List<SecurityCheck>): Int {
        val weights = mapOf(
            SecuritySeverity.CRITICAL to 25,
            SecuritySeverity.WARNING to 15,
            SecuritySeverity.INFO to 5,
            SecuritySeverity.PASS to 0
        )
        
        var totalPossible = 0
        var totalAchieved = 0
        
        checks.forEach { check ->
            val weight = weights[check.severity] ?: 0
            totalPossible += weight
            if (check.passed) {
                totalAchieved += weight
            }
        }
        
        return if (totalPossible > 0) {
            ((totalAchieved.toFloat() / totalPossible) * 100).toInt()
        } else {
            100
        }
    }

    private fun checkRootAccess(): SecurityCheck {
        val hasRoot = checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
        return SecurityCheck(
            id = "root",
            title = "Root Access",
            description = if (hasRoot) "Root binaries detected" else "No root access detected",
            severity = SecuritySeverity.CRITICAL,
            passed = !hasRoot,
            recommendation = if (hasRoot) "Rooted devices are vulnerable to malware and security exploits. Consider unrooting your device." else "Your device is not rooted.",
            details = if (hasRoot) "Found su binaries or test-keys in build tags" else ""
        )
    }

    private fun checkEmulator(): SecurityCheck {
        val isEmulator = checkEmulatorEnvironment()
        return SecurityCheck(
            id = "emulator",
            title = "Emulator Detection",
            description = if (isEmulator) "Running on emulator" else "Running on physical device",
            severity = SecuritySeverity.WARNING,
            passed = !isEmulator,
            recommendation = if (isEmulator) "Emulators may not provide full security features." else "Physical device detected.",
            details = if (isEmulator) "Build fingerprint or model indicates emulator" else ""
        )
    }

    private fun checkUsbDebugging(context: Context): SecurityCheck {
        val isEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "usb_debug",
            title = "USB Debugging",
            description = if (isEnabled) "USB Debugging is enabled" else "USB Debugging is disabled",
            severity = SecuritySeverity.WARNING,
            passed = !isEnabled,
            recommendation = if (isEnabled) "Disable USB Debugging when not needed to prevent unauthorized access." else "USB Debugging is safely disabled.",
            details = ""
        )
    }

    private fun checkDeveloperOptions(context: Context): SecurityCheck {
        val isEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "dev_options",
            title = "Developer Options",
            description = if (isEnabled) "Developer options enabled" else "Developer options disabled",
            severity = SecuritySeverity.INFO,
            passed = !isEnabled,
            recommendation = if (isEnabled) "Developer options provide advanced settings that could affect security." else "Developer options are disabled.",
            details = ""
        )
    }

    private fun checkScreenLock(context: Context): SecurityCheck {
        val hasLock = try {
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                km.isDeviceSecure
            } else {
                @Suppress("DEPRECATION")
                km.isKeyguardSecure
            }
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "screen_lock",
            title = "Screen Lock",
            description = if (hasLock) "Screen lock is enabled" else "No screen lock detected",
            severity = SecuritySeverity.CRITICAL,
            passed = hasLock,
            recommendation = if (!hasLock) "Enable a screen lock (PIN, pattern, or biometric) to protect your device." else "Screen lock is active.",
            details = ""
        )
    }

    private fun checkUnknownSources(context: Context): SecurityCheck {
        val isEnabled = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
            }
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "unknown_sources",
            title = "Unknown Sources",
            description = if (isEnabled) "Installation from unknown sources allowed" else "Unknown sources blocked",
            severity = SecuritySeverity.WARNING,
            passed = !isEnabled,
            recommendation = if (isEnabled) "Disable installation from unknown sources to prevent malicious apps." else "Unknown sources are safely blocked.",
            details = ""
        )
    }

    private fun checkEncryption(context: Context): SecurityCheck {
        val isEncrypted = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.storageEncryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                dpm.storageEncryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "encryption",
            title = "Device Encryption",
            description = if (isEncrypted) "Device storage is encrypted" else "Encryption status unknown",
            severity = SecuritySeverity.CRITICAL,
            passed = isEncrypted,
            recommendation = if (!isEncrypted) "Enable device encryption to protect your data." else "Device is encrypted.",
            details = ""
        )
    }

    private fun checkPlayProtect(context: Context): SecurityCheck {
        // Note: Actual Play Protect status requires Google Play Services API
        // This is a placeholder check
        val hasPlayServices = try {
            context.packageManager.getPackageInfo("com.google.android.gms", 0)
            true
        } catch (e: Exception) {
            false
        }
        
        return SecurityCheck(
            id = "play_protect",
            title = "Play Protect",
            description = if (hasPlayServices) "Google Play Services installed" else "Play Services not found",
            severity = SecuritySeverity.INFO,
            passed = hasPlayServices,
            recommendation = if (!hasPlayServices) "Install Google Play Services for additional security features." else "Play Services available.",
            details = "Full Play Protect status requires additional API access"
        )
    }

    // Root detection methods
    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
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

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val input = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            input.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkEmulatorEnvironment(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}
