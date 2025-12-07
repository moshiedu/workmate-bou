package com.moshitech.workmate.feature.scanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.scanner.domain.model.FlashMode
import com.moshitech.workmate.feature.scanner.domain.model.ScannerConfig
import com.moshitech.workmate.feature.scanner.ui.theme.ScannerColors

/**
 * Scanner settings bottom sheet with standard colors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerSettingsSheet(
    config: ScannerConfig,
    onConfigChange: (ScannerConfig) -> Unit,
    onStartScan: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ScannerColors.Surface,
        contentColor = ScannerColors.OnSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = "Scan Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ScannerColors.OnSurface
            )
            
            // Flash Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Flash",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScannerColors.OnSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlashMode.values().forEach { mode ->
                        FlashModeButton(
                            mode = mode,
                            isSelected = config.flashMode == mode,
                            onClick = { 
                                onConfigChange(config.copy(flashMode = mode))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Divider(color = ScannerColors.SurfaceVariant)
            
            // Auto Capture Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Auto Capture",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ScannerColors.OnSurface
                    )
                    Text(
                        text = "Automatically capture when stable",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScannerColors.OnSurfaceVariant
                    )
                }
                
                Switch(
                    checked = config.autoCapture,
                    onCheckedChange = { 
                        onConfigChange(config.copy(autoCapture = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ScannerColors.OnPrimary,
                        checkedTrackColor = ScannerColors.Primary,
                        uncheckedThumbColor = ScannerColors.OnSurfaceVariant,
                        uncheckedTrackColor = ScannerColors.SurfaceVariant
                    )
                )
            }
            
            Divider(color = ScannerColors.SurfaceVariant)
            
            // Batch Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Batch Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ScannerColors.OnSurface
                    )
                    Text(
                        text = "Scan multiple pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScannerColors.OnSurfaceVariant
                    )
                }
                
                Switch(
                    checked = config.batchMode,
                    onCheckedChange = { 
                        onConfigChange(config.copy(batchMode = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ScannerColors.OnPrimary,
                        checkedTrackColor = ScannerColors.Primary
                    )
                )
            }
            
            Divider(color = ScannerColors.SurfaceVariant)
            
            // HD Quality Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HD Quality",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ScannerColors.OnSurface
                    )
                    Text(
                        text = "Higher quality, larger file size",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScannerColors.OnSurfaceVariant
                    )
                }
                
                Switch(
                    checked = config.hdQuality,
                    onCheckedChange = { 
                        onConfigChange(config.copy(hdQuality = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ScannerColors.OnPrimary,
                        checkedTrackColor = ScannerColors.Primary
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Start Scan Button
            Button(
                onClick = {
                    onDismiss()
                    onStartScan()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScannerColors.Primary,
                    contentColor = ScannerColors.OnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Start Scanning",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FlashModeButton(
    mode: FlashMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconAndLabel = when (mode) {
        FlashMode.OFF -> Pair(Icons.Default.FlashOff, "Off")
        FlashMode.ON -> Pair(Icons.Default.FlashOn, "On")
        FlashMode.AUTO -> Pair(Icons.Default.FlashAuto, "Auto")
        FlashMode.TORCH -> Pair(Icons.Default.Light, "Torch")
    }
    val icon = iconAndLabel.first
    val label = iconAndLabel.second
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) ScannerColors.Primary else Color.Transparent,
            contentColor = if (isSelected) ScannerColors.OnPrimary else ScannerColors.OnSurface
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) ScannerColors.Primary else ScannerColors.SurfaceVariant
            )
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
