package com.moshitech.workmate.feature.deviceinfo.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PermissionGroup(
    val name: String,
    val description: String,
    val icon: Int,
    val apps: List<AppPermissionInfo>,
    val dangerous: Boolean
)

data class AppPermissionInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?,
    val permissions: List<String> = emptyList(),
    val isGranted: Boolean = false
)

data class PermissionStats(
    val totalPermissions: Int,
    val totalApps: Int,
    val mostRequestedPermission: String,
    val appsWithMostPermissions: String
)

enum class GroupingMode {
    BY_PERMISSION,
    BY_APP
}

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _permissionGroups = MutableStateFlow<List<PermissionGroup>>(emptyList())
    val permissionGroups: StateFlow<List<PermissionGroup>> = _permissionGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _groupingMode = MutableStateFlow(GroupingMode.BY_PERMISSION)
    val groupingMode: StateFlow<GroupingMode> = _groupingMode.asStateFlow()

    private val _stats = MutableStateFlow<PermissionStats?>(null)
    val stats: StateFlow<PermissionStats?> = _stats.asStateFlow()

    private var allGroups: List<PermissionGroup> = emptyList()

    init {
        loadPermissions()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterGroups()
    }

    fun toggleGroupingMode() {
        _groupingMode.value = if (_groupingMode.value == GroupingMode.BY_PERMISSION) {
            GroupingMode.BY_APP
        } else {
            GroupingMode.BY_PERMISSION
        }
        loadPermissions()
    }

    fun loadPermissions() {
        viewModelScope.launch {
            _isLoading.value = true
            val groups = withContext(Dispatchers.IO) {
                if (_groupingMode.value == GroupingMode.BY_PERMISSION) {
                    fetchPermissionsByPermission()
                } else {
                    fetchPermissionsByApp()
                }
            }
            allGroups = groups
            _stats.value = calculateStats(groups)
            filterGroups()
            _isLoading.value = false
        }
    }

    private fun filterGroups() {
        val query = _searchQuery.value.lowercase()
        _permissionGroups.value = if (query.isEmpty()) {
            allGroups
        } else {
            allGroups.map { group ->
                group.copy(apps = group.apps.filter { app ->
                    app.appName.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)
                })
            }.filter { it.apps.isNotEmpty() }
        }
    }

    private fun calculateStats(groups: List<PermissionGroup>): PermissionStats {
        val totalApps = groups.flatMap { it.apps }.distinctBy { it.packageName }.size
        val totalPermissions = groups.size
        
        val mostRequested = groups.maxByOrNull { it.apps.size }?.name ?: "N/A"
        
        val appPermCounts = mutableMapOf<String, Int>()
        groups.forEach { group ->
            group.apps.forEach { app ->
                appPermCounts[app.appName] = (appPermCounts[app.appName] ?: 0) + 1
            }
        }
        val appWithMost = appPermCounts.maxByOrNull { it.value }?.key ?: "N/A"
        
        return PermissionStats(
            totalPermissions = totalPermissions,
            totalApps = totalApps,
            mostRequestedPermission = mostRequested,
            appsWithMostPermissions = appWithMost
        )
    }

    private fun fetchPermissionsByPermission(): List<PermissionGroup> {
        val pm = getApplication<Application>().packageManager
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        val permissionMap = mutableMapOf<String, MutableList<AppPermissionInfo>>()

        for (packageInfo in installedPackages) {
            val permissions = packageInfo.requestedPermissions
            
            if (permissions != null) {
                for (permission in permissions) {
                    try {
                        val permInfo = pm.getPermissionInfo(permission, 0)
                        if (permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS != 0) {
                            val group = permInfo.group ?: "Other"
                            
                            if (!permissionMap.containsKey(group)) {
                                permissionMap[group] = mutableListOf()
                            }

                            val appName = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
                            val icon = packageInfo.applicationInfo?.loadIcon(pm)
                            
                            // Check if permission is actually granted using checkPermission
                            val isGranted = pm.checkPermission(permission, packageInfo.packageName) == PackageManager.PERMISSION_GRANTED
                            
                            permissionMap[group]?.add(
                                AppPermissionInfo(
                                    packageName = packageInfo.packageName,
                                    appName = appName,
                                    icon = icon,
                                    isGranted = isGranted
                                )
                            )
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Permission not found, skip
                    }
                }
            }
        }

        return permissionMap.map { (groupKey, apps) ->
            val groupName = groupKey.substringAfterLast(".")
                .replace("_", " ")
                .capitalize()
            
            PermissionGroup(
                name = groupName,
                description = "Apps accessing $groupName",
                icon = 0,
                apps = apps.distinctBy { it.packageName }.sortedBy { it.appName },
                dangerous = true
            )
        }.sortedBy { it.name }
    }

    private fun fetchPermissionsByApp(): List<PermissionGroup> {
        val pm = getApplication<Application>().packageManager
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        return installedPackages.mapNotNull { packageInfo ->
            val permissions = packageInfo.requestedPermissions
            if (permissions != null) {
                val dangerousPerms = permissions.mapNotNull { permission ->
                    try {
                        val permInfo = pm.getPermissionInfo(permission, 0)
                        if (permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS != 0) {
                            permission.substringAfterLast(".").replace("_", " ").capitalize()
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (dangerousPerms.isNotEmpty()) {
                    val appName = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
                    val icon = packageInfo.applicationInfo?.loadIcon(pm)
                    
                    PermissionGroup(
                        name = appName,
                        description = "${dangerousPerms.size} dangerous permissions",
                        icon = 0,
                        apps = listOf(
                            AppPermissionInfo(
                                packageName = packageInfo.packageName,
                                appName = appName,
                                icon = icon,
                                permissions = dangerousPerms
                            )
                        ),
                        dangerous = true
                    )
                } else null
            } else null
        }.sortedBy { it.name }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }
}
