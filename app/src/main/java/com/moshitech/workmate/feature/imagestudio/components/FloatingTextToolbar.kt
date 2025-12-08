package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer
import kotlin.math.roundToInt

@Composable
fun FloatingTextToolbar(
    layer: TextLayer,
    visible: Boolean,
    onUpdate: (TextLayer) -> Unit,
    onLockToggle: (Boolean) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Vertical toolbar on left side - draggable
    Surface(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2C2C2C),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .width(48.dp)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button (keyboard icon)
            VerticalToolButton(
                icon = Icons.Default.Edit,
                isSelected = false,
                onClick = onEdit
            )
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.width(32.dp))
            
            // Font size
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { /* Size picker */ }
            ) {
                Icon(Icons.Default.TextFields, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("${layer.fontSize.toInt()}", color = Color.White, fontSize = 10.sp)
            }
            
            // Text Color
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(layer.color), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { 
                        showColorPicker = !showColorPicker
                        showBgColorPicker = false
                    }
            )
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.width(32.dp))
            
            // Bold
            VerticalToolButton(
                icon = Icons.Default.FormatBold,
                isSelected = layer.isBold,
                onClick = { onUpdate(layer.copy(isBold = !layer.isBold)) }
            )
            
            // Italic
            VerticalToolButton(
                icon = Icons.Default.FormatItalic,
                isSelected = layer.isItalic,
                onClick = { onUpdate(layer.copy(isItalic = !layer.isItalic)) }
            )
            
            // Underline
            VerticalToolButton(
                icon = Icons.Default.FormatUnderlined,
                isSelected = layer.isUnderline,
                onClick = { onUpdate(layer.copy(isUnderline = !layer.isUnderline)) }
            )
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.width(32.dp))
            
            // Align Left
            VerticalToolButton(
                icon = Icons.Default.FormatAlignLeft,
                isSelected = layer.alignment == TextAlignment.LEFT,
                onClick = { onUpdate(layer.copy(alignment = TextAlignment.LEFT)) }
            )
            
            // Align Center
            VerticalToolButton(
                icon = Icons.Default.FormatAlignCenter,
                isSelected = layer.alignment == TextAlignment.CENTER,
                onClick = { onUpdate(layer.copy(alignment = TextAlignment.CENTER)) }
            )
            
            // Align Right
            VerticalToolButton(
                icon = Icons.Default.FormatAlignRight,
                isSelected = layer.alignment == TextAlignment.RIGHT,
                onClick = { onUpdate(layer.copy(alignment = TextAlignment.RIGHT)) }
            )
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.width(32.dp))
            
            // Background color
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (layer.showBackground) Color(layer.backgroundColor).copy(alpha = layer.backgroundOpacity)
                        else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                    .clickable { 
                        showBgColorPicker = !showBgColorPicker
                        showColorPicker = false
                    }
            )
            
            // Duplicate (white icon)
            IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.ContentCopy, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
            }
        }
    }
    
    // Text Color picker popup
    if (showColorPicker) {
        Surface(
            modifier = Modifier.padding(start = 60.dp, top = 100.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2C2C2C),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Text Color", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = listOf(
                        0xFFFFFFFF, 0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
                        0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF, 0xFFFFA500, 0xFF800080
                    )
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(color), CircleShape)
                                .border(
                                    if (color.toInt() == layer.color) 2.dp else 1.dp,
                                    if (color.toInt() == layer.color) Color(0xFF2196F3) else Color.Gray,
                                    CircleShape
                                )
                                .clickable { 
                                    onUpdate(layer.copy(color = color.toInt()))
                                    showColorPicker = false
                                }
                        )
                    }
                }
            }
        }
    }
    
    // Background Color picker popup
    if (showBgColorPicker) {
        Surface(
            modifier = Modifier.padding(start = 60.dp, top = 300.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2C2C2C),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Background", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Transparent option
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color.Red, CircleShape)
                            .clickable { 
                                onUpdate(layer.copy(showBackground = false))
                                showBgColorPicker = false
                            }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                    
                    val bgColors = listOf(
                        0xFFFFFFFF, 0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
                        0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF, 0xFFFFA500
                    )
                    bgColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(color), CircleShape)
                                .border(
                                    if (color.toInt() == layer.backgroundColor && layer.showBackground) 2.dp else 1.dp,
                                    if (color.toInt() == layer.backgroundColor && layer.showBackground) Color(0xFF2196F3) else Color.Gray,
                                    CircleShape
                                )
                                .clickable { 
                                    onUpdate(layer.copy(
                                        backgroundColor = color.toInt(),
                                        showBackground = true
                                    ))
                                    showBgColorPicker = false
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF2196F3) else Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
