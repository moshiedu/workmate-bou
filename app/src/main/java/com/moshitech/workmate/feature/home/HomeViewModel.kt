package com.moshitech.workmate.feature.home

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val _ramUsagePercent = MutableStateFlow(0)
    val ramUsagePercent: StateFlow<Int> = _ramUsagePercent.asStateFlow()

    init {
        updateRamInfo()
        startPeriodicUpdate()
    }

    private fun updateRamInfo() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMem = memoryInfo.totalMem
        val availMem = memoryInfo.availMem
        val usedMem = totalMem - availMem
        val percent = (usedMem.toDouble() / totalMem.toDouble() * 100).toInt()

        _ramUsagePercent.value = percent
    }
    
    private fun startPeriodicUpdate() {
        viewModelScope.launch {
            while (true) {
                updateRamInfo()
                delay(5000) // Update every 5 seconds
            }
        }
    }
    
    fun refreshRamInfo() {
        updateRamInfo()
    }
}
