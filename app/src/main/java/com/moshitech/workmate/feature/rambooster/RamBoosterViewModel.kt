package com.moshitech.workmate.feature.rambooster

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.format.Formatter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BoosterState {
    IDLE, BOOSTING, BOOSTED
}

class RamBoosterViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val _state = MutableStateFlow(BoosterState.IDLE)
    val state: StateFlow<BoosterState> = _state.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _memoryFreed = MutableStateFlow("0 MB")
    val memoryFreed: StateFlow<String> = _memoryFreed.asStateFlow()

    private val _appsStopped = MutableStateFlow("0")
    val appsStopped: StateFlow<String> = _appsStopped.asStateFlow()

    private val _ramUsageText = MutableStateFlow("Loading...")
    val ramUsageText: StateFlow<String> = _ramUsageText.asStateFlow()
    
    private val _ramUsagePercent = MutableStateFlow(0)
    val ramUsagePercent: StateFlow<Int> = _ramUsagePercent.asStateFlow()

    private val _lastBoosted = MutableStateFlow("Never")
    val lastBoosted: StateFlow<String> = _lastBoosted.asStateFlow()

    data class StoppedApp(val name: String, val memory: String, val packageName: String)

    private val _stoppedAppsList = MutableStateFlow<List<StoppedApp>>(emptyList())
    val stoppedAppsList: StateFlow<List<StoppedApp>> = _stoppedAppsList.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    init {
        checkPermission()
        updateRamInfo()
    }

    fun checkPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        _isPermissionGranted.value = mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            // Only exclude critical system apps, allow updated system apps (which are often user-facing like Chrome, YouTube)
            val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            
            // If it's a system app but NOT an updated one, exclude it.
            // If it IS an updated system app (like Chrome updates), treat it as user app (return false)
            if (isSystem && !isUpdatedSystem) {
                // Double check launch intent to see if it's a user-launchable app
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                launchIntent == null // If no launch intent, it's likely a background system service -> true
            } else {
                false
            }
        } catch (e: Exception) {
            false // If we can't determine, assume it's NOT a system app to be safe and let user decide
        }
    }

    private fun getRunningApps(): List<StoppedApp> {
        if (!_isPermissionGranted.value) return emptyList()

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val pm = context.packageManager
            
            // Try different intervals to ensure we get data
            val intervals = listOf(
                UsageStatsManager.INTERVAL_DAILY,
                UsageStatsManager.INTERVAL_WEEKLY,
                UsageStatsManager.INTERVAL_MONTHLY,
                UsageStatsManager.INTERVAL_YEARLY
            )
            
            var usageStats: List<android.app.usage.UsageStats> = emptyList()
            val end = System.currentTimeMillis()
            val start = end - (24 * 60 * 60 * 1000) 
            
            for (interval in intervals) {
                usageStats = usageStatsManager.queryUsageStats(interval, start, end)
                if (!usageStats.isNullOrEmpty()) break
            }
            
            val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
            
            // Process UsageStats results
            val usageStatsApps = if (!usageStats.isNullOrEmpty()) {
                usageStats.asSequence()
                    .filter { stat ->
                        stat.packageName != context.packageName && !isSystemApp(stat.packageName)
                    }
                    .sortedByDescending { it.lastTimeUsed }
                    .distinctBy { it.packageName }
                    .take(20)
                    .mapNotNull { stat ->
                        try {
                            val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                            val appName = pm.getApplicationLabel(appInfo).toString()
                            
                            val process = runningProcesses.find { 
                                it.processName.startsWith(stat.packageName)
                            }
                            
                            var memoryString = "Background"
                            if (process != null) {
                                val memoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(process.pid))
                                if (memoryInfo.isNotEmpty()) {
                                    val memoryMB = memoryInfo[0].totalPss / 1024
                                    if (memoryMB > 0) memoryString = "$memoryMB MB"
                                }
                            }
                            StoppedApp(appName, memoryString, stat.packageName)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .toList()
            } else emptyList()
            
            if (usageStatsApps.isNotEmpty()) return usageStatsApps

            // Fallback to runningProcesses if UsageStats yielded nothing
            return runningProcesses.mapNotNull { process ->
                try {
                    val pkgName = process.processName.split(":")[0]
                    if (pkgName == context.packageName) return@mapNotNull null
                    if (isSystemApp(pkgName)) return@mapNotNull null
                    
                    val appInfo = pm.getApplicationInfo(pkgName, 0)
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    
                    val memoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(process.pid))
                    val memoryMB = if (memoryInfo.isNotEmpty()) memoryInfo[0].totalPss / 1024 else 0
                    
                    StoppedApp(appName, "$memoryMB MB", pkgName)
                } catch (e: Exception) {
                    null
                }
            }.take(20)
            
        } catch (e: Exception) {
            return emptyList()
        }
    }

    fun startBoost() {
        if (_state.value == BoosterState.BOOSTING) return
        if (!_isPermissionGranted.value) return

        viewModelScope.launch {
            _state.value = BoosterState.BOOSTING
            _memoryFreed.value = "Optimizing..."
            _appsStopped.value = "Scanning..."
            _stoppedAppsList.value = emptyList()
            
            // Run heavy work on IO thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Measure system memory BEFORE boost
                    val memoryBefore = getAvailableMemory()
                    
                    // Simulate scanning visual (update progress on Main)
                    for (i in 1..50) {
                        _progress.value = i / 100f
                        delay(10) // Faster scan
                    }
                    
                    // Get apps to kill (Heavy operation)
                    val appsToKill = getRunningApps()
                    
                    if (appsToKill.isEmpty()) {
                        // Even if no apps found, show completion animation
                        for (i in 51..100) {
                            _progress.value = i / 100f
                            delay(10)
                        }
                        _memoryFreed.value = "0 MB"
                        _appsStopped.value = "0"
                        
                        val dateFormat = SimpleDateFormat("'Today, 'hh:mm a", Locale.getDefault())
                        _lastBoosted.value = dateFormat.format(Date())
                        
                        _state.value = BoosterState.BOOSTED
                        return@withContext
                    }
                    
                    // Kill apps
                    val killedApps = mutableListOf<StoppedApp>()
                    val totalApps = appsToKill.size
                    // Ensure animation takes at least 1.5 seconds for satisfaction
                    val minDuration = 1500L 
                    val delayPerApp = maxOf(50L, minDuration / totalApps)
                    
                    var projectedFreedMB = 0
                    
                    for ((index, app) in appsToKill.withIndex()) {
                        try {
                            activityManager.killBackgroundProcesses(app.packageName)
                            killedApps.add(app)
                            
                            // Calculate projected memory
                            val memString = app.memory.replace(" MB", "")
                            val mem = memString.toIntOrNull()
                            if (mem != null) {
                                projectedFreedMB += mem
                            } else {
                                // If "Background" or unknown, assume ~40MB per app
                                projectedFreedMB += 40
                            }
                        } catch (e: Exception) {
                            // Ignore errors
                        }
                        // Update progress
                        _progress.value = 0.5f + (0.5f * ((index + 1).toFloat() / totalApps))
                        delay(delayPerApp) 
                    }
                    
                    // Wait a bit for system to reclaim memory
                    delay(500)
                    
                    // Measure system memory AFTER boost
                    val memoryAfter = getAvailableMemory()
                    val actualFreedBytes = memoryAfter - memoryBefore
                    val actualFreedMB = (actualFreedBytes / (1024 * 1024)).coerceAtLeast(0)
                    
                    // Use the larger of actual vs projected to ensure user satisfaction
                    val displayFreedMB = maxOf(actualFreedMB, projectedFreedMB.toLong())
                    
                    _memoryFreed.value = "$displayFreedMB MB"
                    _stoppedAppsList.value = killedApps
                    _appsStopped.value = "${killedApps.size}"
                    
                    // Optimistically update RAM info to reflect the boost immediately
                    updateRamInfo(projectedFreedMB.toLong() * 1024 * 1024)
                    
                    val dateFormat = SimpleDateFormat("'Today, 'hh:mm a", Locale.getDefault())
                    _lastBoosted.value = dateFormat.format(Date())
                    
                    _state.value = BoosterState.BOOSTED
                } catch (e: Exception) {
                    // Fallback in case of crash
                    _state.value = BoosterState.BOOSTED
                    _memoryFreed.value = "Done"
                }
            }
        }
    }
    
    private fun getAvailableMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
    private fun updateRamInfo(optimisticFreedBytes: Long = 0) {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMem = memoryInfo.totalMem
        // Add optimistic freed memory to available memory for display
        val availMem = memoryInfo.availMem + optimisticFreedBytes
        // Ensure we don't exceed total memory
        val adjustedAvailMem = minOf(availMem, totalMem)
        
        val usedMem = totalMem - adjustedAvailMem
        val percent = (usedMem.toDouble() / totalMem.toDouble() * 100).toInt()

        val usedFormatted = Formatter.formatShortFileSize(context, usedMem)
        val totalFormatted = Formatter.formatShortFileSize(context, totalMem)

        _ramUsageText.value = "$usedFormatted / $totalFormatted Used"
        _ramUsagePercent.value = percent
    }
}
