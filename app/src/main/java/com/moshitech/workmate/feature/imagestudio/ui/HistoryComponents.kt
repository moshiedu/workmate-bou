package com.moshitech.workmate.feature.imagestudio.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.exifinterface.media.ExifInterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
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
import androidx.core.net.toUri

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
    var exifInfo by remember { mutableStateOf<ExifInfo?>(null) }
    var isExifLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    LaunchedEffect(item.outputUri) {
         exifInfo = getExifMetadata(context, item.outputUri.toUri())
         isExifLoaded = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BatchColors.surfaceContainer(isDark)),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp) // Limit height
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Make scrollable
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                // Exif Metadata Section
                Divider(color = BatchColors.outline(isDark))
                Text("Camera Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BatchColors.textPrimary(isDark))
                
                if (!isExifLoaded) {
                     Text("Loading details...", color = BatchColors.textSecondary(isDark), fontSize = 14.sp)
                } else if (exifInfo != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        exifInfo!!.cameraModel?.let { DetailRow("Camera", it, isDark) }
                        exifInfo!!.aperture?.let { DetailRow("Aperture", "f/$it", isDark) }
                        exifInfo!!.shutterSpeed?.let { DetailRow("Shutter", "${it}s", isDark) }
                        exifInfo!!.iso?.let { DetailRow("ISO", it, isDark) }
                        exifInfo!!.focalLength?.let { DetailRow("Focal Len", "${it}mm", isDark) }
                    }
                } else {
                    Text("No metadata available", color = BatchColors.textSecondary(isDark), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 14.sp)
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

// Helper to open folder only
fun openDirectory(context: Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        var intentStarted = false

        // Strategy 1: SAF / DocumentFile
        if (uri.scheme == "content") {
            try {
                // Try to find parent
                val docFile = androidx.documentfile.provider.DocumentFile.fromSingleUri(context, uri)
                val parentUri = docFile?.parentFile?.uri ?: uri // If parent null, try uri itself (though unlikely for single file)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(parentUri, "vnd.android.document/directory")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    intentStarted = true
                    return
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Strategy 2: File Path
        if (!intentStarted) {
             var filePath: String? = null
             if (uri.scheme == "file") {
                 filePath = uri.path
             } else if (uri.scheme == "content") {
                 try {
                     context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
                         if (cursor.moveToFirst()) {
                             filePath = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATA))
                         }
                     }
                 } catch (e: Exception) {}
             }
             
             if (filePath != null) {
                 val file = File(filePath)
                 val parent = file.parentFile ?: file
                 if (parent.exists()) {
                     // Try specific resource/folder
                     val intent = Intent(Intent.ACTION_VIEW)
                     intent.setDataAndType(Uri.fromFile(parent), "resource/folder")
                     if (intent.resolveActivity(context.packageManager) != null) {
                         context.startActivity(intent)
                         return
                     }
                     // Try generic */* with hierarchy
                     val intent2 = Intent(Intent.ACTION_VIEW)
                     intent2.setDataAndType(Uri.fromFile(parent), "*/*")
                     // Hint for some file managers
                     intent2.putExtra("org.openintents.extra.ABSOLUTE_PATH", parent.absolutePath)
                     
                     if (intent2.resolveActivity(context.packageManager) != null) {
                         context.startActivity(Intent.createChooser(intent2, "Open Folder"))
                         return
                     }
                 }
             }
        }
        
        // Final Fallback: Open Downloads or Files App generally
        try {
            val intent = Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)
            if (intent.resolveActivity(context.packageManager) != null) {
                 android.widget.Toast.makeText(context, "Specific folder access restricted. Opening file manager...", android.widget.Toast.LENGTH_LONG).show()
                 context.startActivity(intent)
                 return
            }
        } catch (e: Exception) {}

        // If we reach here, we couldn't open the folder properly.
        android.widget.Toast.makeText(context, "Cannot find file manager to open this location.", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error opening location", android.widget.Toast.LENGTH_SHORT).show()
    }
}

fun openFile(context: Context, uriString: String, mimeType: String = "*/*") {
    try {
        val uri = uriString.toUri()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Open File"))
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show()
    }
}



data class ExifInfo(
    val cameraModel: String? = null,
    val aperture: String? = null,
    val shutterSpeed: String? = null,
    val iso: String? = null,
    val focalLength: String? = null
)

private suspend fun getExifMetadata(context: Context, uri: Uri): ExifInfo? {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: exif.getAttribute(ExifInterface.TAG_MAKE)
                val aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
                val shutter = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY) ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
                val focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                
                if (model != null || aperture != null || shutter != null) {
                     ExifInfo(model, aperture, shutter, iso, focal)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
