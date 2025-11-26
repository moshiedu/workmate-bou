package com.moshitech.workmate.feature.deviceinfo.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object ManifestParser {
    
    /**
     * Reconstructs AndroidManifest.xml content using PackageManager
     * Returns formatted XML-like string
     */
    fun extractManifest(context: Context, packageName: String): String {
        return try {
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_META_DATA or
                    PackageManager.GET_CONFIGURATIONS

            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                context.packageManager.getPackageInfo(packageName, flags)
            }

            buildManifestXml(packageInfo, context.packageManager)
        } catch (e: Exception) {
            buildErrorMessage(e.message ?: "Unknown error", packageName)
        }
    }
    
    private fun buildManifestXml(info: PackageInfo, pm: PackageManager): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
        sb.append("    package=\"${info.packageName}\"\n")
        sb.append("    android:versionCode=\"${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode}\"\n")
        sb.append("    android:versionName=\"${info.versionName}\">\n\n")

        // Uses SDK
        val appInfo = info.applicationInfo
        sb.append("    <uses-sdk\n")
        sb.append("        android:minSdkVersion=\"${appInfo?.minSdkVersion}\"\n")
        sb.append("        android:targetSdkVersion=\"${appInfo?.targetSdkVersion}\" />\n\n")

        // Permissions
        info.requestedPermissions?.let { permissions ->
            permissions.forEachIndexed { index, permission ->
                sb.append("    <uses-permission android:name=\"$permission\" />\n")
            }
            sb.append("\n")
        }

        sb.append("    <application\n")
        sb.append("        android:label=\"${info.applicationInfo?.loadLabel(pm)}\"\n")
        sb.append("        android:icon=\"@mipmap/ic_launcher\">\n\n")

        // Activities
        info.activities?.forEach { activity ->
            sb.append("        <activity\n")
            sb.append("            android:name=\"${activity.name}\"\n")
            if (activity.exported) sb.append("            android:exported=\"true\"\n")
            sb.append("            android:label=\"${activity.loadLabel(pm)}\" />\n")
        }
        if (!info.activities.isNullOrEmpty()) sb.append("\n")

        // Services
        info.services?.forEach { service ->
            sb.append("        <service\n")
            sb.append("            android:name=\"${service.name}\"\n")
            if (service.exported) sb.append("            android:exported=\"true\"\n")
            sb.append("            android:permission=\"${service.permission ?: ""}\" />\n")
        }
        if (!info.services.isNullOrEmpty()) sb.append("\n")

        // Receivers
        info.receivers?.forEach { receiver ->
            sb.append("        <receiver\n")
            sb.append("            android:name=\"${receiver.name}\"\n")
            if (receiver.exported) sb.append("            android:exported=\"true\"\n")
            sb.append("            android:permission=\"${receiver.permission ?: ""}\" />\n")
        }
        if (!info.receivers.isNullOrEmpty()) sb.append("\n")

        // Providers
        info.providers?.forEach { provider ->
            sb.append("        <provider\n")
            sb.append("            android:name=\"${provider.name}\"\n")
            sb.append("            android:authorities=\"${provider.authority}\"\n")
            if (provider.exported) sb.append("            android:exported=\"true\"\n")
            sb.append("            android:grantUriPermissions=\"${provider.grantUriPermissions}\" />\n")
        }

        sb.append("    </application>\n")
        sb.append("</manifest>")
        
        return sb.toString()
    }
    
    private fun buildErrorMessage(error: String, packageName: String): String {
        return """
            Error Reconstructing Manifest
            =============================
            
            Error: $error
            
            Package: $packageName
            
            Note: This viewer reconstructs the manifest from installed package information.
            If the app is not properly installed or system restricted, some details may be missing.
        """.trimIndent()
    }
}
