package com.moshitech.workmate.feature.imagestudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.data.local.ConversionHistoryEntity
import com.moshitech.workmate.feature.imagestudio.viewmodel.BatchConverterViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.FolderOpen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchHistoryScreen(
    viewModel: BatchConverterViewModel,
    history: List<ConversionHistoryEntity>,
    onBack: () -> Unit,
    isDark: Boolean
) {
    // Delete State
    var itemToDelete by remember { mutableStateOf<ConversionHistoryEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Dialogs
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete History Item?", color = BatchColors.textPrimary(isDark)) },
            text = { Text("This will remove the record from your history. The actual file will NOT be deleted from your device.", color = BatchColors.textSecondary(isDark)) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.deleteHistoryItem(itemToDelete!!)
                    itemToDelete = null 
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = BatchColors.textSecondary(isDark))
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark)
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All History?", color = BatchColors.textPrimary(isDark)) },
            text = { Text("This will remove all history records. Your files will NOT be deleted.", color = BatchColors.textSecondary(isDark)) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.clearHistory()
                    showClearAllDialog = false 
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = BatchColors.textSecondary(isDark))
                }
            },
            containerColor = BatchColors.surfaceContainer(isDark)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = BatchColors.textPrimary(isDark)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = BatchColors.textPrimary(isDark))
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.Delete, "Clear All", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent // Handled by parent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sticky Dashboard
            HistoryDashboard(history = history, isDark = isDark, viewModel = viewModel)

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history yet", color = BatchColors.textSecondary(isDark))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        HistoryItem(
                            item = item,
                            viewModel = viewModel,
                            isDark = isDark,
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDashboard(
    history: List<ConversionHistoryEntity>,
    isDark: Boolean,
    viewModel: BatchConverterViewModel
) {
    val totalConversions = history.size
    val totalSize = history.sumOf { it.sizeBytes }
    val uniqueFormats = history.map { it.format }.distinct().size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BatchColors.surface(isDark)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DashboardItem(
                label = "Converted",
                value = totalConversions.toString(),
                isDark = isDark
            )
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = BatchColors.outline(isDark)
            )
            DashboardItem(
                label = "Total Size",
                value = viewModel.formatFileSize(totalSize),
                isDark = isDark
            )
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = BatchColors.outline(isDark)
            )
             DashboardItem(
                label = "Formats",
                value = uniqueFormats.toString(),
                isDark = isDark
            )
        }
    }
}

@Composable
fun DashboardItem(
    label: String,
    value: String,
    isDark: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BatchColors.primary(isDark)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = BatchColors.textSecondary(isDark)
        )
    }
}

@Composable
fun HistoryItem(
    item: ConversionHistoryEntity,
    viewModel: BatchConverterViewModel,
    isDark: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isAvailable by remember { mutableStateOf(true) }
    
    // Use LaunchedEffect with unique key or plain side effect
    LaunchedEffect(item.outputUri) {
        // Run in IO context ideally, but VM handles most
        isAvailable = viewModel.isFileAvailable(item.outputUri)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BatchColors.surface(isDark), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BatchColors.surface(isDark))
        ) {
            if (isAvailable) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(item.outputUri).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Image,
                    null,
                    tint = BatchColors.textSecondary(isDark),
                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = java.io.File(android.net.Uri.parse(item.outputUri).path ?: "Unknown").name.takeIf { it.isNotEmpty() } ?: "Image",
                color = if (isAvailable) BatchColors.textPrimary(isDark) else BatchColors.textSecondary(isDark),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.format} • ${viewModel.formatFileSize(item.sizeBytes)}",
                    color = BatchColors.textSecondary(isDark),
                    fontSize = 12.sp
                )
                if (item.width > 0 && item.height > 0) {
                     Text(
                        text = " • ${item.width}x${item.height}",
                        color = BatchColors.textSecondary(isDark),
                        fontSize = 12.sp
                    )
                }
            }
            if (!isAvailable) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                     Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                     Spacer(Modifier.width(4.dp))
                     Text("File not found", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        }

        // Actions
        Row {
             if (isAvailable) {
                 IconButton(onClick = { viewModel.openFile(item.outputUri, context) }) {
                     Icon(Icons.Default.FolderOpen, "Open", tint = BatchColors.primary(isDark))
                 }
             }
             IconButton(onClick = onDelete) {
                 Icon(Icons.Default.Delete, "Delete", tint = BatchColors.textSecondary(isDark))
             }
        }
    }
}
