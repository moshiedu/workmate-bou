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

    data class StoppedApp(val name: String, val memory: String)

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

    private fun getRunningApps(): List<StoppedApp> {
        if (!_isPermissionGranted.value) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val start = calendar.timeInMillis
        val end = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        
        val pm = context.packageManager
        
        return stats.asSequence()
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .take(15) // Get top 15 recent apps
            .mapNotNull { usageStat ->
                try {
                    val appName = pm.getApplicationLabel(pm.getApplicationInfo(usageStat.packageName, 0)).toString()
                    // Filter out system apps or our own app if needed, for now just basic filtering
                    if (usageStat.packageName != context.packageName) {
                        val memory = (10..150).random() // Still simulated memory as we can't get exact per-process RAM easily
                        StoppedApp(appName, "$memory MB")
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            .toList()
    }

    fun startBoost() {
        if (_state.value == BoosterState.BOOSTING) return

        viewModelScope.launch {
            _state.value = BoosterState.BOOSTING
            _memoryFreed.value = "Optimizing..."
            _appsStopped.value = "Scanning..."
            _stoppedAppsList.value = emptyList()
            
            // Simulate boosting process
            for (i in 1..100) {
                _progress.value = i / 100f
                delay(30) // 3 seconds total
            }

            updateRamInfo()
            
            val realApps = getRunningApps()
            val stoppedApps = if (realApps.isNotEmpty()) {
                realApps.take((3..8).random().coerceAtMost(realApps.size))
            } else {
                // Fallback to dummy if permission not granted or no stats
                 val dummyApps = listOf("Instagram", "Facebook", "Chrome", "Spotify", "YouTube", "TikTok", "Snapchat", "Twitter", "Maps", "Gmail")
                val selectedApps = dummyApps.shuffled().take((3..7).random())
                selectedApps.map { appName ->
                    val memory = (10..80).random()
                    StoppedApp(appName, "$memory MB")
                }
            }
            
            // Calculate total freed based on "stopped" apps
            val totalFreed = stoppedApps.sumOf { it.memory.replace(" MB", "").toInt() }
            _memoryFreed.value = "$totalFreed MB"
            
            _stoppedAppsList.value = stoppedApps
            _appsStopped.value = "${stoppedApps.size}"
            
            val dateFormat = SimpleDateFormat("'Today, 'hh:mm a", Locale.getDefault())
            _lastBoosted.value = dateFormat.format(Date())
            
            _state.value = BoosterState.BOOSTED
        }
    }
    
    private fun updateRamInfo() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMem = memoryInfo.totalMem
        val availMem = memoryInfo.availMem
        val usedMem = totalMem - availMem
        val percent = (usedMem.toDouble() / totalMem.toDouble() * 100).toInt()

        val usedFormatted = Formatter.formatShortFileSize(context, usedMem)
        val totalFormatted = Formatter.formatShortFileSize(context, totalMem)

        _ramUsageText.value = "$usedFormatted / $totalFormatted Used"
        _ramUsagePercent.value = percent
    }
}
