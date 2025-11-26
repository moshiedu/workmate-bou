package com.moshitech.workmate.feature.deviceinfo.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isUpdatedSystemApp: Boolean,
    val category: String,
    val targetSdk: Int,
    val minSdk: Int,
    val installerPackage: String?,
    val installTime: Long,
    val updateTime: Long,
    val apkSize: Long,
    val uid: Int,
    val permissions: List<PermissionInfo>,
    val sourceDir: String
)

data class PermissionInfo(
    val name: String,
    val displayName: String,
    val isGranted: Boolean,
    val protectionLevel: PermissionProtectionLevel
)

enum class PermissionProtectionLevel {
    ALLOWED,        // ✓
    NOT_ALLOWED,    // ✗
    SPECIAL_ACCESS  // ★
}

enum class AppFilter {
    USER,
    SYSTEM,
    ALL
}
