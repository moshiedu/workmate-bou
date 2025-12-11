package com.moshitech.workmate.feature.imagestudio.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moshitech.workmate.feature.imagestudio.data.local.ConversionHistoryEntity
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ImageDetailDialog(
    item: ConversionHistoryEntity,
    isFileExists: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onOpenFolder: (() -> Unit)? = null,
    isDark: Boolean,
    customPath: String? = null,
    customStatus: String? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BatchColors.surfaceContainer(isDark)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (customStatus != null) "Preview Details" else "Image Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BatchColors.textPrimary(isDark))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = BatchColors.textSecondary(isDark))
                    }
                }
                
                // Preview (Miniature)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFileExists) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(Uri.parse(item.outputUri)).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(32.dp))
                            Text("File Missing", color = Color.White)
                        }
                    }
                }
                
                // Info Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Name", Uri.parse(item.outputUri).lastPathSegment ?: "Unknown", isDark)
                    val dateStr = if (customStatus != null) "Just Now" else DateUtils.getRelativeTimeSpanString(item.date).toString()
                    DetailRow("Date", dateStr, isDark)
                    DetailRow("Format", item.format, isDark)
                    DetailRow("Size", formatFileSize(item.sizeBytes), isDark)
                    if (item.width > 0) {
                        DetailRow("Resolution", "${item.width} x ${item.height}", isDark)
                    }
                    DetailRow("Path", customPath ?: (Uri.parse(item.outputUri).path ?: "Unknown"), isDark)
                    
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Status", color = BatchColors.textSecondary(isDark), fontSize = 14.sp)
                        if (customStatus != null) {
                             Text(
                                customStatus, 
                                color = BatchColors.primary(isDark), 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                if (isFileExists) "Available" else "Missing / Deleted", 
                                color = if (isFileExists) Color.Green else Color.Red, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Divider(color = BatchColors.outline(isDark))
                
                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isFileExists && onOpenFolder != null) {
                        Button(
                            onClick = onOpenFolder,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BatchColors.surface(isDark))
                        ) {
                            Icon(Icons.Default.FolderOpen, null, tint = BatchColors.primary(isDark))
                            Spacer(Modifier.width(8.dp))
                            Text("Folder", color = BatchColors.textPrimary(isDark))
                        }
                    }
                    
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = BatchColors.textSecondary(isDark), fontSize = 14.sp)
        Text(value, color = BatchColors.textPrimary(isDark), fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false), maxLines = 1)
    }
}

fun formatFileSize(size: Long): String {
    val mb = size / (1024.0 * 1024.0)
    if (mb >= 1.0) return String.format("%.2f MB", mb)
    val kb = size / 1024.0
    return String.format("%.2f KB", kb)
}

// Helper to open folder or show file
fun openFileFolder(context: Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        
        // Strategy 1: DocumentFile (Best for SAF/Tree URIs)
        if (uri.scheme == "content") {
            try {
                val docFile = androidx.documentfile.provider.DocumentFile.fromSingleUri(context, uri)
                val parent = docFile?.parentFile
                if (parent != null) {
                    // Try specific directory intent
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(parent.uri, "vnd.android.document/directory")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        if (intent.resolveActivity(context.packageManager) != null) {
                             context.startActivity(intent)
                             return
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                    
                    // Fallback to generic view on parent for SAF
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(parent.uri)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                         if (intent.resolveActivity(context.packageManager) != null) {
                             context.startActivity(Intent.createChooser(intent, "Open Folder"))
                             return
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            } catch (e: Exception) {
                // Ignore and fall through
            }
        }

        var filePath: String? = null
        // Strategy 2: File System Path (File URI or MediaStore _data)
        if (uri.scheme == "file") {
            filePath = uri.path
        } else if (uri.scheme == "content") {
             // Try to query MediaStore for _data column
             try {
                 context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
                     if (cursor.moveToFirst()) {
                         filePath = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATA))
                     }
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
                 // Fallback: Use path if it looks like a path (rare for content uri but possible)
                 // filePath = uri.path 
             }
        }

        if (filePath != null) {
            val file = File(filePath)
            val parent = file.parentFile
            if (parent != null && parent.exists()) {
                 val intent = Intent(Intent.ACTION_VIEW)
                 intent.setDataAndType(Uri.fromFile(parent), "resource/folder")
                 if (intent.resolveActivity(context.packageManager) != null) {
                     context.startActivity(intent)
                     return
                 }
                 // Try generic file manager
                 intent.setDataAndType(Uri.fromFile(parent), "*/*")
                 context.startActivity(Intent.createChooser(intent, "Open Folder"))
                 return
            }
        }
        
        // If we reach here, we couldn't open the folder properly.
        // Fallback: Try to "view" the file itself, many viewers allow jumping to folder.
        // Or show a toast explaining limitation.
        
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "image/*") // or try generic
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Open File"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Cannot open folder directly.", android.widget.Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error opening location", android.widget.Toast.LENGTH_SHORT).show()
    }
}
