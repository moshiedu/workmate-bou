package com.moshitech.workmate.feature.imagestudio.ui

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BatchHistoryScreen(
    viewModel: BatchConverterViewModel,
    history: List<ConversionHistoryEntity>,
    onBack: () -> Unit,
    isDark: Boolean
) {
    // State
    var itemToDelete by remember { mutableStateOf<ConversionHistoryEntity?>(null) }
    var itemForDetail by remember { mutableStateOf<ConversionHistoryEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Grouping
    val groupedHistory = remember(history) {
        history.groupBy { 
            val date = java.util.Date(it.date)
            when {
                DateUtils.isToday(it.date) -> "Today"
                DateUtils.isToday(it.date + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
                else -> android.text.format.DateFormat.format("MMMM dd, yyyy", date).toString()
            }
        }
    }

    // Detail Dialog
    if (itemForDetail != null) {
        val isAvailable = viewModel.isFileAvailable(itemForDetail!!.outputUri)
        ImageDetailDialog(
            item = itemForDetail!!,
            isFileExists = isAvailable,
            onDismiss = { itemForDetail = null },
            onDelete = { itemToDelete = itemForDetail; itemForDetail = null },
            onOpenFolder = { openDirectory(context, itemForDetail!!.outputUri) },
            isDark = isDark
        )
    }

    // Delete Dialog
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

    // Clear All Dialog
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
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dashboard
            HistoryDashboard(history = history, isDark = isDark, viewModel = viewModel)

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history yet", color = BatchColors.textSecondary(isDark))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedHistory.forEach { (dateHeader, items) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BatchColors.surface(isDark)) // Opaque background for sticky header
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = dateHeader,
                                    color = BatchColors.primary(isDark),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        items(items, key = { it.id }) { item ->
                            HistoryItem(
                                item = item,
                                viewModel = viewModel,
                                isDark = isDark,
                                onDelete = { itemToDelete = item },
                                onLongPress = { itemForDetail = item }
                            )
                        }
                        
                        item { Spacer(Modifier.height(8.dp)) }
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
            DashboardItem(label = "Converted", value = totalConversions.toString(), isDark = isDark)
            VerticalDivider(isDark)
            DashboardItem(label = "Total Size", value = viewModel.formatFileSize(totalSize), isDark = isDark)
            VerticalDivider(isDark)
            DashboardItem(label = "Formats", value = uniqueFormats.toString(), isDark = isDark)
        }
    }
}

@Composable
fun VerticalDivider(isDark: Boolean) {
    Divider(
        modifier = Modifier.height(40.dp).width(1.dp),
        color = BatchColors.outline(isDark)
    )
}

@Composable
fun DashboardItem(label: String, value: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BatchColors.primary(isDark))
        Text(text = label, fontSize = 12.sp, color = BatchColors.textSecondary(isDark))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    item: ConversionHistoryEntity,
    viewModel: BatchConverterViewModel,
    isDark: Boolean,
    onDelete: () -> Unit,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    var isAvailable by remember { mutableStateOf(true) }
    
    LaunchedEffect(item.outputUri) {
        isAvailable = viewModel.isFileAvailable(item.outputUri)
    }

    val alpha = if (isAvailable) 1f else 0.6f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { 
                    if (isAvailable) viewModel.openFile(item.outputUri, context) 
                    else onLongPress() // Show detail if missing
                },
                onLongClick = onLongPress
            )
            .background(BatchColors.surface(isDark).copy(alpha = alpha), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BatchColors.surfaceContainer(isDark))
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
                    Icons.Default.Warning, // Changed to Warning for missing
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = java.io.File(android.net.Uri.parse(item.outputUri).path ?: "Unknown").name.takeIf { it.isNotEmpty() } ?: "Image",
                color = if (isAvailable) BatchColors.textPrimary(isDark) else MaterialTheme.colorScheme.error,
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
            }
            if (!isAvailable) {
                Text("File Missing", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }
        }

        // Actions
        // Actions
        Row {
             if (isAvailable) {
                 IconButton(onClick = { openFile(context, item.outputUri) }) {
                     Icon(Icons.Default.Visibility, "View", tint = BatchColors.primary(isDark))
                 }
                 IconButton(onClick = { onLongPress() }) {
                     Icon(Icons.Default.Info, "Details", tint = BatchColors.primary(isDark))
                 }
                 IconButton(onClick = { openDirectory(context, item.outputUri) }) {
                     Icon(Icons.Default.FolderOpen, "Open Folder", tint = BatchColors.primary(isDark))
                 }
             }
             IconButton(onClick = onDelete) {
                 Icon(Icons.Default.Delete, "Delete", tint = BatchColors.textSecondary(isDark))
             }
        }
    }
}

