package com.moshitech.workmate.feature.scanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.scanner.domain.model.ScannedDocument
import com.moshitech.workmate.feature.scanner.ui.theme.ScannerColors

/**
 * Post-scan action sheet shown after document is scanned
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScanActionSheet(
    document: ScannedDocument,
    onView: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onSaveToLibrary: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Document Scanned!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = ScannerColors.OnSurface
                    )
                    Text(
                        text = "${document.pageCount} page${if (document.pageCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ScannerColors.OnSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ScannerColors.Success,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Divider(color = ScannerColors.SurfaceVariant)
            
            // Action buttons
            ActionButton(
                icon = Icons.Default.Visibility,
                label = "Preview Document",
                description = "View the scanned document",
                onClick = {
                    onView()
                    onDismiss()
                }
            )
            
            ActionButton(
                icon = Icons.Default.Download,
                label = "Export to Downloads",
                description = "Save to Downloads folder",
                onClick = {
                    onExport()
                    onDismiss()
                }
            )
            
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share Document",
                description = "Share via apps",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )
            
            ActionButton(
                icon = Icons.Default.Save,
                label = "Save to Library",
                description = "Keep in scanner library",
                onClick = {
                    onSaveToLibrary()
                    onDismiss()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ScannerColors.OnSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ScannerColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ScannerColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = ScannerColors.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ScannerColors.OnSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ScannerColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
