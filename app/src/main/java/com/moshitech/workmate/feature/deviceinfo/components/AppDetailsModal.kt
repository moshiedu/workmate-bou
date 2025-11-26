package com.moshitech.workmate.feature.deviceinfo.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo
import com.moshitech.workmate.feature.deviceinfo.model.PermissionProtectionLevel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@Composable
fun AppDetailsModal(
    app: AppInfo,
    isDark: Boolean,
    textColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showActionsMenu by remember { mutableStateOf(false) }
    var showManifestViewer by remember { mutableStateOf(false) }
    
    // File picker launcher for APK extraction
    val apkFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        uri?.let {
            extractApkToUri(context, app, it)
        }
    }
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = Color(0xFF10B981)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        app.icon?.let { drawable ->
                            Image(
                                bitmap = drawable.toBitmap(56, 56).asImageBitmap(),
                                contentDescription = app.appName,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = app.appName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    
                    IconButton(onClick = { showActionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = subtitleColor
                        )
                    }
                    
                    AppActionsMenu(
                        app = app,
                        expanded = showActionsMenu,
                        onDismiss = { showActionsMenu = false },
                        onShowManifest = { showManifestViewer = true },
                        onExtractApk = {
                            apkFilePicker.launch("${app.appName}.apk")
                        },
                        textColor = textColor
                    )
                }
                
                Divider(color = subtitleColor.copy(alpha = 0.2f))
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Basic Info
                    AppDetailRow("Category", app.category, textColor, subtitleColor)
                    AppDetailRow("Package name", app.packageName, textColor, subtitleColor)
                    AppDetailRow("Version", app.versionName, textColor, subtitleColor)
                    AppDetailRow("Target SDK", app.targetSdk.toString(), textColor, subtitleColor)
                    AppDetailRow("Minimum SDK", "${app.minSdk} (Android ${getAndroidVersion(app.minSdk)})", textColor, subtitleColor)
                    
                    // Installer Info
                    val installerType = when {
                        app.installerPackage == null -> "Unknown"
                        app.installerPackage.contains("vending") -> "Bundle"
                        else -> "Other"
                    }
                    AppDetailRow("Installer type", installerType, textColor, subtitleColor)
                    
                    val installerName = when {
                        app.installerPackage == null -> "Unknown"
                        app.installerPackage.contains("vending") -> "Google Play"
                        else -> app.installerPackage
                    }
                    AppDetailRow("Installer", installerName, textColor, subtitleColor)
                    
                    // Dates
                    val dateFormat = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())
                    AppDetailRow("Installed", dateFormat.format(Date(app.installTime)), textColor, subtitleColor)
                    AppDetailRow("Updated", dateFormat.format(Date(app.updateTime)), textColor, subtitleColor)
                    
                    // Size and UID
                    AppDetailRow("Size", formatFileSize(app.apkSize), textColor, subtitleColor)
                    AppDetailRow("UID", app.uid.toString(), textColor, subtitleColor)
                    
                    // Permissions
                    if (app.permissions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permissions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PermissionLegendItem("Allowed", Icons.Default.Check, Color(0xFF10B981), subtitleColor)
                            PermissionLegendItem("Not allowed", Icons.Default.Close, Color(0xFFEF4444), subtitleColor)
                            PermissionLegendItem("Special access", Icons.Default.Star, Color(0xFFF59E0B), subtitleColor)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Permissions List
                        app.permissions.forEach { permission ->
                            PermissionRow(permission, textColor, subtitleColor)
                        }
                    }
                }
                
                Divider(color = subtitleColor.copy(alpha = 0.2f))
                
                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = subtitleColor
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            openAppSettings(context, app.packageName)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor
                        )
                    ) {
                        Text("Manage")
                    }
                }
            }
        }
    }
    
    // Manifest Viewer Dialog
    if (showManifestViewer) {
        ManifestViewerDialog(
            app = app,
            isDark = isDark,
            textColor = textColor,
            onDismiss = { showManifestViewer = false }
        )
    }
}

@Composable
fun AppDetailRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = subtitleColor,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun PermissionLegendItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor
        )
    }
}

@Composable
fun PermissionRow(
    permission: com.moshitech.workmate.feature.deviceinfo.model.PermissionInfo,
    textColor: Color,
    subtitleColor: Color
) {
    val (icon, iconColor) = when (permission.protectionLevel) {
        PermissionProtectionLevel.ALLOWED -> Icons.Default.Check to Color(0xFF10B981)
        PermissionProtectionLevel.NOT_ALLOWED -> Icons.Default.Close to Color(0xFFEF4444)
        PermissionProtectionLevel.SPECIAL_ACCESS -> Icons.Default.Star to Color(0xFFF59E0B)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = permission.displayName,
            fontSize = 13.sp,
            color = textColor
        )
    }
}

private fun openAppSettings(context: Context, packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = "package:$packageName".toUri()
    context.startActivity(intent)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

private fun getAndroidVersion(sdk: Int): String {
    return when (sdk) {
        21 -> "5.0"
        22 -> "5.1"
        23 -> "6.0"
        24 -> "7.0"
        25 -> "7.1"
        26 -> "8.0"
        27 -> "8.1"
        28 -> "9.0"
        29 -> "10.0"
        30 -> "11.0"
        31 -> "12.0"
        32 -> "12.1"
        33 -> "13.0"
        34 -> "14.0"
        else -> sdk.toString()
    }
}

private fun extractApkToUri(context: Context, app: AppInfo, uri: Uri) {
    try {
        val sourceFile = File(app.sourceDir)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Toast.makeText(context, "APK extracted successfully", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error extracting APK: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
