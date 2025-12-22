package com.moshitech.workmate.feature.imagestudio.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.moshitech.workmate.feature.imagestudio.data.Layer
import com.moshitech.workmate.feature.imagestudio.data.LayerType
import kotlinx.coroutines.launch

/**
 * Layer Panel - Photoshop-like layer management UI
 * Shows all layers with visibility toggle, z-order controls, smooth drag-and-drop reordering
 */
@Composable
fun LayerPanel(
    layers: List<Layer>,
    selectedLayerId: String?,
    onLayerSelected: (String) -> Unit,
    onVisibilityToggle: (String) -> Unit,
    onLayerReorder: (String, Int) -> Unit,
    onLayerRename: (String, String) -> Unit,
    onLayerDelete: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort layers by zIndex (highest first = front to back) - use derived state
    val sortedLayers = remember(layers) { 
        layers.sortedByDescending { it.zIndex }
    }
    
    // Drag state
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    val dragOffsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Item height constant
    val itemHeight = 80.dp
    
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Layers",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.width(8.dp))
                
                // Layer count badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF3B82F6), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${layers.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close Layer Panel",
                    tint = Color.White
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Front/Back indicator labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "FRONT",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                "Long press to reorder",
                color = Color.Gray,
                fontSize = 10.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Layer List with smooth drag-and-drop
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(
                items = sortedLayers,
                key = { _, layer -> layer.id }
            ) { index, layer ->
                val isDragging = draggedItemId == layer.id
                val currentDraggingIndex = draggingIndex
                
                // Calculate animated offset for this item
                val offsetY = remember { Animatable(0f) }
                
                LaunchedEffect(currentDraggingIndex, dragOffsetY.value) {
                    if (currentDraggingIndex != null && !isDragging) {
                        // Calculate if this item should move
                        val draggedY = dragOffsetY.value
                        val itemHeightPx = with(density) { itemHeight.toPx() }
                        val draggedPosition = currentDraggingIndex + (draggedY / itemHeightPx)
                        
                        val shouldMoveDown = index < currentDraggingIndex && draggedPosition <= index
                        val shouldMoveUp = index > currentDraggingIndex && draggedPosition >= index
                        
                        val targetOffset = when {
                            shouldMoveDown -> itemHeightPx + with(density) { 8.dp.toPx() }
                            shouldMoveUp -> -(itemHeightPx + with(density) { 8.dp.toPx() })
                            else -> 0f
                        }
                        
                        offsetY.animateTo(
                            targetValue = targetOffset,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    } else {
                        offsetY.snapTo(0f)
                    }
                }
                
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 8.dp else 0.dp,
                    label = "elevation"
                )
                
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = if (isDragging) dragOffsetY.value else offsetY.value
                        }
                        .zIndex(if (isDragging) 1f else 0f)
                ) {
                    LayerItem(
                        layer = layer,
                        position = index + 1,
                        totalLayers = sortedLayers.size,
                        isSelected = layer.id == selectedLayerId,
                        isDragging = isDragging,
                        elevation = elevation,
                        onSelect = { onLayerSelected(layer.id) },
                        onVisibilityToggle = { onVisibilityToggle(layer.id) },
                        onMoveUp = { onLayerReorder(layer.id, layer.zIndex + 1) },
                        onMoveDown = { onLayerReorder(layer.id, layer.zIndex - 1) },
                        onRename = { newName -> onLayerRename(layer.id, newName) },
                        onDelete = { onLayerDelete(layer.id) },
                        onDragStart = {
                            draggingIndex = index
                            draggedItemId = layer.id
                            scope.launch {
                                dragOffsetY.snapTo(0f)
                            }
                        },
                        onDrag = { delta ->
                            scope.launch {
                                val itemHeightPx = with(density) { itemHeight.toPx() }
                                val spacingPx = with(density) { 8.dp.toPx() }
                                val newOffset = (dragOffsetY.value + delta).coerceIn(
                                    -(index * (itemHeightPx + spacingPx)),
                                    ((sortedLayers.size - 1 - index) * (itemHeightPx + spacingPx))
                                )
                                dragOffsetY.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val currentIndex = draggingIndex ?: return@launch
                                val itemHeightPx = with(density) { itemHeight.toPx() }
                                val positions = (dragOffsetY.value / itemHeightPx).toInt()
                                val newIndex = (currentIndex + positions).coerceIn(0, sortedLayers.size - 1)
                                
                                // Reset drag state FIRST so the list can recompose with new order
                                draggingIndex = null
                                draggedItemId = null
                                
                                // Apply the reorder if position changed
                                if (newIndex != currentIndex && newIndex in sortedLayers.indices) {
                                    val targetLayer = sortedLayers[newIndex]
                                    onLayerReorder(layer.id, targetLayer.zIndex)
                                }
                                
                                // Then animate offset back to 0
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Back indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                Icons.Default.Layers,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "BACK",
                color = Color(0xFF6B7280),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Individual Layer Item in the panel with drag support
 */
@Composable
fun LayerItem(
    layer: Layer,
    position: Int,
    totalLayers: Int,
    isSelected: Boolean,
    isDragging: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    onSelect: () -> Unit,
    onVisibilityToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.25f)
                else Color(0xFF2A2A2A),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF404040),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Drag Handle + Visibility + Thumbnail + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Drag handle icon
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = if (isDragging) Color(0xFF3B82F6) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            
            // Position indicator badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (position == 1) Color(0xFF10B981) // Green for front
                        else if (position == totalLayers) Color(0xFF6B7280) // Gray for back
                        else Color(0xFF3B82F6), // Blue for middle
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$position",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Visibility toggle
            IconButton(
                onClick = onVisibilityToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (layer.isVisible) Icons.Default.Visibility 
                    else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle visibility",
                    tint = if (layer.isVisible) Color.White else Color.Gray
                )
            }
            
            // Layer thumbnail (icon based on type)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF3A3A3A), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (layer.type) {
                        LayerType.TEXT -> Icons.Default.TextFields
                        LayerType.STICKER -> Icons.Default.EmojiEmotions
                        else -> Icons.Default.Layers
                    },
                    contentDescription = null,
                    tint = when (layer.type) {
                        LayerType.TEXT -> Color(0xFF60A5FA)
                        LayerType.STICKER -> Color(0xFFFBBF24)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Layer Name & Content Preview
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showRenameDialog = true }
            ) {
                Text(
                    layer.layerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show text content for text layers
                if (layer is Layer.Text) {
                    Text(
                        text = "\"${layer.textLayer.text}\"",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Right: Z-order controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Move up (to front)
            IconButton(
                onClick = onMoveUp,
                modifier = Modifier.size(32.dp),
                enabled = position > 1
            ) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Move forward",
                    tint = if (position > 1) Color(0xFF10B981) else Color(0xFF4B5563),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Move down (to back)
            IconButton(
                onClick = onMoveDown,
                modifier = Modifier.size(32.dp),
                enabled = position < totalLayers
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = "Move backward",
                    tint = if (position < totalLayers) Color(0xFFF59E0B) else Color(0xFF4B5563),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete layer",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    
    // Rename dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(layer.layerName) }
        
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Layer") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Layer Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
