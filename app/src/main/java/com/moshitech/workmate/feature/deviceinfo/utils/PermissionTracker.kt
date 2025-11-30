package com.moshitech.workmate.feature.deviceinfo.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PermissionTracker(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    data class AppPermissionState(
        val packageName: String,
        val grantedPermissions: Set<String>
    )

    fun checkForPermissionChanges(): List<String> {
        val currentPermissions = getCurrentPermissions()
        val previousPermissions = getStoredPermissions()
        
        val changes = mutableListOf<String>()
        
        // Compare current with previous
        currentPermissions.forEach { (packageName, currentGranted) ->
            val previousGranted = previousPermissions[packageName] ?: emptySet()
            
            // Find newly granted permissions
            val newPermissions = currentGranted - previousGranted
            
            if (newPermissions.isNotEmpty()) {
                val appName = getAppName(packageName)
                newPermissions.forEach { permission ->
                    val permName = permission.substringAfterLast(".")
                    changes.add("$appName granted $permName")
                }
            }
        }
        
        // Save current state for next time
        savePermissions(currentPermissions)
        
        return changes
    }

    private fun getCurrentPermissions(): Map<String, Set<String>> {
        val pm = context.packageManager
        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val permissionMap = mutableMapOf<String, Set<String>>()

        for (packageInfo in installedPackages) {
            val granted = mutableSetOf<String>()
            val permissions = packageInfo.requestedPermissions
            val flags = packageInfo.requestedPermissionsFlags

            if (permissions != null) {
                for (i in permissions.indices) {
                    if ((flags?.get(i)?.and(PackageInfo.REQUESTED_PERMISSION_GRANTED)) != 0) {
                        // Only track dangerous permissions for noise reduction
                        try {
                            val permInfo = pm.getPermissionInfo(permissions[i], 0)
                            if (permInfo.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_DANGEROUS != 0) {
                                granted.add(permissions[i])
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
            }
            if (granted.isNotEmpty()) {
                permissionMap[packageInfo.packageName] = granted
            }
        }
        return permissionMap
    }

    private fun getStoredPermissions(): Map<String, Set<String>> {
        val json = prefs.getString(KEY_PERMISSIONS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Set<String>>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun savePermissions(permissions: Map<String, Set<String>>) {
        val json = gson.toJson(permissions)
        prefs.edit().putString(KEY_PERMISSIONS, json).apply()
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    companion object {
        private const val PREFS_NAME = "permission_tracker_prefs"
        private const val KEY_PERMISSIONS = "stored_permissions"
    }
}
