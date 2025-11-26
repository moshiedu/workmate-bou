package com.moshitech.workmate.feature.deviceinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.deviceinfo.data.DeviceInfoRepository
import com.moshitech.workmate.feature.deviceinfo.data.DeviceInfoRepositoryEnhanced
import com.moshitech.workmate.feature.deviceinfo.data.models.*
import com.moshitech.workmate.feature.deviceinfo.model.AppFilter
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo
import com.moshitech.workmate.feature.deviceinfo.model.NetworkInfo
import com.moshitech.workmate.feature.deviceinfo.utils.AppsLoader
import com.moshitech.workmate.feature.deviceinfo.utils.NetworkInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DeviceInfoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DeviceInfoRepository(application)
    private val repositoryEnhanced = DeviceInfoRepositoryEnhanced(application)
    private val networkInfoProvider = NetworkInfoProvider(application)
    
    private val _dashboardInfo = MutableStateFlow(DashboardInfo())
    val dashboardInfo: StateFlow<DashboardInfo> = _dashboardInfo.asStateFlow()
    
    private val _hardwareInfo = MutableStateFlow(HardwareInfo())
    val hardwareInfo: StateFlow<HardwareInfo> = _hardwareInfo.asStateFlow()
    
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()
    
    // Enhanced models
    private val _systemInfoEnhanced = MutableStateFlow(SystemInfoEnhanced())
    val systemInfoEnhanced: StateFlow<SystemInfoEnhanced> = _systemInfoEnhanced.asStateFlow()
    
    private val _hardwareInfoEnhanced = MutableStateFlow(HardwareInfoEnhanced())
    val hardwareInfoEnhanced: StateFlow<HardwareInfoEnhanced> = _hardwareInfoEnhanced.asStateFlow()
    
    // Apps
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()
    
    private val _appsLoading = MutableStateFlow(false)
    val appsLoading: StateFlow<Boolean> = _appsLoading.asStateFlow()
    
    // Network
    private val _networkInfo = MutableStateFlow<NetworkInfo?>(null)
    val networkInfo: StateFlow<NetworkInfo?> = _networkInfo.asStateFlow()
    
    init {
        loadHardwareInfo()
        loadSystemInfo()
        loadEnhancedInfo()
        startDashboardUpdates()
        startNetworkUpdates()
    }
    
    private fun loadHardwareInfo() {
        viewModelScope.launch {
            _hardwareInfo.value = repository.getHardwareInfo()
        }
    }
    
    private fun loadSystemInfo() {
        viewModelScope.launch {
            _systemInfo.value = repository.getSystemInfo()
        }
    }
    
    private fun loadEnhancedInfo() {
        viewModelScope.launch {
            _systemInfoEnhanced.value = repositoryEnhanced.getSystemInfoEnhanced()
            _hardwareInfoEnhanced.value = repositoryEnhanced.getHardwareInfoEnhanced()
        }
    }
    
    private fun startDashboardUpdates() {
        viewModelScope.launch {
            while (isActive) {
                _dashboardInfo.value = repository.getDashboardInfo()
                delay(2000) // Update every 2 seconds
            }
        }
    }
    
    fun loadApps(filter: AppFilter = AppFilter.USER) {
        viewModelScope.launch(Dispatchers.IO) {
            _appsLoading.value = true
            try {
                val appsList = AppsLoader.loadApps(getApplication(), filter)
                _apps.value = appsList
            } catch (e: Exception) {
                _apps.value = emptyList()
            } finally {
                _appsLoading.value = false
            }
        }
    }

    private fun startNetworkUpdates() {
        viewModelScope.launch {
            while (isActive) {
                _networkInfo.value = networkInfoProvider.getNetworkInfo()
                delay(2000) // Update every 2 seconds
            }
        }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch {
            _networkInfo.value = networkInfoProvider.getNetworkInfo()
        }
    }

    fun refreshPublicIp() {
        viewModelScope.launch {
            val currentInfo = _networkInfo.value
            if (currentInfo?.dhcpDetails != null) {
                val publicIp = networkInfoProvider.fetchPublicIp()
                _networkInfo.value = currentInfo.copy(
                    dhcpDetails = currentInfo.dhcpDetails.copy(publicIp = publicIp)
                )
            }
        }
    }
}
