package com.moshitech.workmate.feature.imagestudio.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BatchGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, null, tint = Color(0xFF007AFF)) },
        title = { Text("Conversion Guide", color = Color(0xFF0F172A)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formats
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Auto Format", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                    Text(
                        "Preserves original file type (PNG→PNG). Use to batch resize mixed files without changing their extension.",
                        color = Color(0xFF334155), fontSize = 13.sp
                    )
                }
                
                // Smart Resize
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Size", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                    Text(
                        "Sets a maximum file size (e.g. 500KB). If quality reduction isn't enough (e.g. for PNGs), the app will automatically reduce resolution until it fits.",
                        color = Color(0xFF334155), fontSize = 13.sp
                    )
                }
                
                 // HEIF
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("HEIF (.heic)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                        Text(
                            "High Efficiency Image Format. Saves space with better quality than JPEG. Supported on Android 9+.",
                            color = Color(0xFF334155), fontSize = 13.sp
                        )
                    }
                }
                
                // Quality vs Dimensions
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Upscaling", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                    Text(
                        "Setting dimensions larger than the original will not create detail; it may result in a blurry or pixelated image.",
                        color = Color(0xFF334155), fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = Color(0xFF007AFF))
            }
        }
    )
}
