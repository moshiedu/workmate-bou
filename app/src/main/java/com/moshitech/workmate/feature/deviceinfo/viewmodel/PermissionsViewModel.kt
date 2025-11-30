package com.moshitech.workmate.feature.deviceinfo.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
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
    val icon: Int, // Resource ID or generic icon type
    val apps: List<AppPermissionInfo>,
    val dangerous: Boolean
)

data class AppPermissionInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _permissionGroups = MutableStateFlow<List<PermissionGroup>>(emptyList())
    val permissionGroups: StateFlow<List<PermissionGroup>> = _permissionGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPermissions()
    }

    fun loadPermissions() {
        viewModelScope.launch {
            _isLoading.value = true
            val groups = withContext(Dispatchers.IO) {
                fetchPermissions()
            }
            _permissionGroups.value = groups
            _isLoading.value = false
        }
    }

    private fun fetchPermissions(): List<PermissionGroup> {
        val pm = getApplication<Application>().packageManager
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        val permissionMap = mutableMapOf<String, MutableList<AppPermissionInfo>>()
        val permissionDetails = mutableMapOf<String, PermissionInfo>()

        for (packageInfo in installedPackages) {
            val permissions = packageInfo.requestedPermissions
            if (permissions != null) {
                for (permission in permissions) {
                    try {
                        val permInfo = pm.getPermissionInfo(permission, 0)
                        // Only show dangerous permissions (runtime permissions)
                        if (permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS != 0) {
                            val group = permInfo.group ?: "Other"
                            
                            if (!permissionMap.containsKey(group)) {
                                permissionMap[group] = mutableListOf()
                            }
                            
                            // Cache permission details for the group name/desc
                            if (!permissionDetails.containsKey(group)) {
                                // Try to get group info
                                try {
                                    if (group != "Other") {
                                        // Some groups might not resolve, handle gracefully
                                        // Actually, we group by the permission group name usually
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }

                            val appName = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
                            val icon = packageInfo.applicationInfo?.loadIcon(pm)
                            
                            permissionMap[group]?.add(
                                AppPermissionInfo(
                                    packageName = packageInfo.packageName,
                                    appName = appName,
                                    icon = icon
                                )
                            )
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Permission not found, skip
                    }
                }
            }
        }

        // Convert map to list of PermissionGroup
        return permissionMap.map { (groupKey, apps) ->
            val groupName = groupKey.substringAfterLast(".")
                .replace("_", " ")
                .capitalize()
            
            PermissionGroup(
                name = groupName,
                description = "Apps accessing $groupName",
                icon = 0, // We'll handle icons in UI based on name
                apps = apps.sortedBy { it.appName },
                dangerous = true
            )
        }.sortedBy { it.name }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }
}
