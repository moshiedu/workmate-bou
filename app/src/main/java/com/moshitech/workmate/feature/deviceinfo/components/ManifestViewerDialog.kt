package com.moshitech.workmate.feature.deviceinfo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ManifestViewerDialog(
    app: AppInfo,
    isDark: Boolean,
    textColor: Color,
    onDismiss: () -> Unit
) {
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    var manifestContent by remember { mutableStateOf("Loading manifest...") }
    
    LaunchedEffect(app) {
        manifestContent = try {
            extractManifest(app.sourceDir)
        } catch (e: Exception) {
            "Error loading manifest: ${e.message}\n\nNote: Manifest extraction requires additional tools or libraries."
        }
    }
    
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
                Text(
                    text = "AndroidManifest.xml",
                    fontSize = 18.sp,
                    color = textColor,
                    modifier = Modifier.padding(16.dp)
                )
                
                Divider(color = subtitleColor.copy(alpha = 0.2f))
                
                // Content
                Text(
                    text = manifestContent,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
                
                Divider(color = subtitleColor.copy(alpha = 0.2f))
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}

private fun extractManifest(apkPath: String): String {
    return try {
        // Try using aapt2 command if available
        val process = Runtime.getRuntime().exec(arrayOf("aapt2", "dump", "xmltree", apkPath, "AndroidManifest.xml"))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = reader.readText()
        process.waitFor()
        
        if (output.isNotEmpty()) {
            output
        } else {
            "Manifest extraction not available.\n\nTo view the manifest, you would need:\n" +
                    "1. Install aapt2 tool\n" +
                    "2. Or use a library like apk-parser\n" +
                    "3. Or manually extract and decompile the APK\n\n" +
                    "APK Path: $apkPath"
        }
    } catch (e: Exception) {
        "Manifest extraction not available.\n\n" +
                "Error: ${e.message}\n\n" +
                "APK Path: $apkPath\n\n" +
                "Note: This feature requires additional setup or libraries to extract and parse the binary XML manifest."
    }
}
