package com.moshitech.workmate.feature.deviceinfo.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.moshitech.workmate.feature.deviceinfo.model.AppFilter
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo
import com.moshitech.workmate.feature.deviceinfo.model.PermissionProtectionLevel
import java.io.File

object AppsLoader {
    
    fun loadApps(context: Context, filter: AppFilter): List<AppInfo> {
        val pm = context.packageManager
        val packages = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdated = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                // Filter based on type
                when (filter) {
                    AppFilter.USER -> if (isSystem && !isUpdated) return@mapNotNull null
                    AppFilter.SYSTEM -> if (!isSystem) return@mapNotNull null
                    AppFilter.ALL -> {}
                }
                
                createAppInfo(pm, pkg, appInfo, isSystem, isUpdated)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }
    
    private fun createAppInfo(
        pm: PackageManager,
        pkg: PackageInfo,
        appInfo: ApplicationInfo,
        isSystem: Boolean,
        isUpdated: Boolean
    ): AppInfo {
        val category = getCategoryName(appInfo)
        val installer = getInstallerName(pm, pkg.packageName)
        val apkSize = try {
            File(appInfo.sourceDir).length()
        } catch (e: Exception) {
            0L
        }
        
        val permissions = pkg.requestedPermissions?.mapIndexed { index, permName ->
            val isGranted = pkg.requestedPermissionsFlags?.get(index)?.let {
                (it and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } ?: false
            
            val protectionLevel = getPermissionProtectionLevel(pm, permName, isGranted)
            val displayName = getPermissionDisplayName(permName)
            
            com.moshitech.workmate.feature.deviceinfo.model.PermissionInfo(
                name = permName,
                displayName = displayName,
                isGranted = isGranted,
                protectionLevel = protectionLevel
            )
        } ?: emptyList()
        
        return AppInfo(
            packageName = pkg.packageName,
            appName = pm.getApplicationLabel(appInfo).toString(),
            versionName = pkg.versionName ?: "Unknown",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkg.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkg.versionCode.toLong()
            },
            icon = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null },
            isSystemApp = isSystem,
            isUpdatedSystemApp = isUpdated,
            category = category,
            targetSdk = appInfo.targetSdkVersion,
            minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appInfo.minSdkVersion
            } else {
                0
            },
            installerPackage = installer,
            installTime = pkg.firstInstallTime,
            updateTime = pkg.lastUpdateTime,
            apkSize = apkSize,
            uid = appInfo.uid,
            permissions = permissions,
            sourceDir = appInfo.sourceDir
        )
    }
    
    private fun getCategoryName(appInfo: ApplicationInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> "Game"
                ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                ApplicationInfo.CATEGORY_VIDEO -> "Video"
                ApplicationInfo.CATEGORY_IMAGE -> "Image"
                ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                ApplicationInfo.CATEGORY_NEWS -> "News"
                ApplicationInfo.CATEGORY_MAPS -> "Maps"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                else -> "Undefined"
            }
        } else {
            "Undefined"
        }
    }
    
    private fun getInstallerName(pm: PackageManager, packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getPermissionProtectionLevel(
        pm: PackageManager,
        permName: String,
        isGranted: Boolean
    ): PermissionProtectionLevel {
        return try {
            val permInfo = @Suppress("DEPRECATION")
            pm.getPermissionInfo(permName, 0)
            
            when {
                permInfo.protection == PermissionInfo.PROTECTION_DANGEROUS ||
                permInfo.protection == PermissionInfo.PROTECTION_SIGNATURE -> {
                    if (isGranted) PermissionProtectionLevel.SPECIAL_ACCESS
                    else PermissionProtectionLevel.NOT_ALLOWED
                }
                isGranted -> PermissionProtectionLevel.ALLOWED
                else -> PermissionProtectionLevel.NOT_ALLOWED
            }
        } catch (e: Exception) {
            if (isGranted) PermissionProtectionLevel.ALLOWED
            else PermissionProtectionLevel.NOT_ALLOWED
        }
    }
    
    private fun getPermissionDisplayName(permName: String): String {
        return permName.substringAfterLast('.').replace('_', ' ')
            .split(' ').joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }
}
