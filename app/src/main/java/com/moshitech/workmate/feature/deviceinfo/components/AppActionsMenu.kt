package com.moshitech.workmate.feature.deviceinfo.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo
import java.io.File

@Composable
fun AppActionsMenu(
    app: AppInfo,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShowManifest: () -> Unit,
    textColor: Color
) {
    val context = LocalContext.current
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // Open App
        DropdownMenuItem(
            text = { Text("Open", color = textColor) },
            onClick = {
                openApp(context, app.packageName)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Launch, contentDescription = null, tint = textColor)
            }
        )
        
        // Open in Market
        DropdownMenuItem(
            text = { Text("Market", color = textColor) },
            onClick = {
                openInMarket(context, app.packageName)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = textColor)
            }
        )
        
        // Show Manifest
        DropdownMenuItem(
            text = { Text("Manifest", color = textColor) },
            onClick = {
                onShowManifest()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = null, tint = textColor)
            }
        )
        
        // Uninstall
        if (!app.isSystemApp || app.isUpdatedSystemApp) {
            DropdownMenuItem(
                text = { Text("Uninstall", color = Color(0xFFEF4444)) },
                onClick = {
                    uninstallApp(context, app.packageName)
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                }
            )
        }
        
        // Extract APK
        DropdownMenuItem(
            text = { Text("Extract apk", color = textColor) },
            onClick = {
                extractApk(context, app)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Archive, contentDescription = null, tint = textColor)
            }
        )
    }
}

private fun openApp(context: Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "App cannot be launched", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening app: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openInMarket(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Cannot open market", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun uninstallApp(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error uninstalling app: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun extractApk(context: Context, app: AppInfo) {
    try {
        val sourceFile = File(app.sourceDir)
        val destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(destDir, "${app.packageName}.apk")
        
        sourceFile.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "APK extracted to Downloads/${app.packageName}.apk", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error extracting APK: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
