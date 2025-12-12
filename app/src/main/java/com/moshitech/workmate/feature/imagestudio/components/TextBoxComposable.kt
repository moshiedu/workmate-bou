package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer

@Composable
fun TextBoxComposable(
    layer: TextLayer,
    isSelected: Boolean,
    isEditing: Boolean,
    onSelect: (String) -> Unit,
    onEdit: (String) -> Unit,
    onTransform: (String, Offset, Float, Float) -> Unit,
    onTextChange: (String, String) -> Unit,
    onDuplicate: (String) -> Unit, // New callback
    onDelete: (String) -> Unit,    // Explicit delete callback for handle
    modifier: Modifier = Modifier
) {
    // Apply transforms via graphicsLayer
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = layer.x
                translationY = layer.y
                scaleX = layer.scale
                scaleY = layer.scale
                rotationZ = layer.rotation
            }
            // General Drag/Pinch/Rotate on the text body
            .pointerInput(layer.id, layer.isLocked, isEditing) {
                if (!layer.isLocked && !isEditing) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        onTransform(layer.id, pan, zoom, rotation)
                    }
                }
            }
            .pointerInput(layer.id, isSelected) {
                detectTapGestures(
                    onTap = {
                        if (!layer.isLocked) {
                            onEdit(layer.id)
                        }
                    }
                )
            }
    ) {
        // Content with Dashed Border
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Min)
                .padding(12.dp) // Space for handles to not overlap too much
                .drawBehind {
                    if (isSelected) {
                        val stroke = 2.dp.toPx() / layer.scale
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                        drawRoundRect(
                            color = Color.White,
                            cornerRadius = CornerRadius(4.dp.toPx()),
                            style = Stroke(width = stroke, pathEffect = pathEffect)
                        )
                    }
                }
                .background(
                    if (layer.showBackground) Color(layer.backgroundColor).copy(alpha = layer.backgroundOpacity) 
                    else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(layer.backgroundPadding.dp)
        ) {
            // ... (Text content logic remains same, just context)
            // Text Display
            val displayText = buildAnnotatedString {
                if (layer.text.isEmpty() && !isEditing) {
                    append("Your Text Here")
                } else if (layer.isAllCaps) {
                    append(layer.text.uppercase())
                } else if (layer.isSmallCaps) {
                    val upperCaseText = layer.text.uppercase()
                    layer.text.forEachIndexed { index, char ->
                        if (char.isLowerCase()) {
                            withStyle(SpanStyle(fontSize = (layer.fontSize * 0.7f).sp)) {
                                append(upperCaseText[index])
                            }
                        } else {
                            append(upperCaseText[index])
                        }
                    }
                } else {
                    append(layer.text)
                }
            }
            
            val textStyle = TextStyle(
                color = Color(layer.color),
                fontSize = layer.fontSize.sp,
                fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (layer.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    layer.isUnderline && layer.isStrikethrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    layer.isUnderline -> TextDecoration.Underline
                    layer.isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                },
                textAlign = when (layer.alignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                    TextAlignment.JUSTIFY -> TextAlign.Justify
                },
                letterSpacing = layer.letterSpacing.sp,
                lineHeight = (layer.fontSize * layer.lineHeight).sp,
                shadow = if (layer.hasShadow) Shadow(
                    color = Color(layer.shadowColor),
                    offset = Offset(layer.shadowOffsetX, layer.shadowOffsetY),
                    blurRadius = layer.shadowBlur
                ) else null // Basic shadow support
            )

            if (isEditing) {
                val focusRequester = remember { FocusRequester() }
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = layer.text,
                    onValueChange = { onTextChange(layer.id, it) },
                    textStyle = textStyle,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 50.dp)
                        .focusRequester(focusRequester),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                )
            } else {
                Text(
                    text = displayText,
                    style = textStyle,
                    modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                        .graphicsLayer { alpha = layer.layerOpacity }
                )
            }
        }

        // Overlay Handles
        if (isSelected && !layer.isLocked) {
            val handleSize = 24.dp
            val handleOffset = 12.dp // Matches padding

            // Top Left: Copy & Delete
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = -handleOffset, y = -handleOffset), // Shift up/left
                horizontalArrangement = Arrangement.spacedBy(24.dp), // Increased gap
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button (Icon Only)
                IconButton(
                    onClick = { onDuplicate(layer.id) },
                    modifier = Modifier.size(handleSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Delete Button (Icon Only)
                IconButton(
                    onClick = { onDelete(layer.id) },
                    modifier = Modifier.size(handleSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Top Right: Rotate
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = handleOffset, y = -handleOffset)
                    .size(handleSize)
                    .background(Color(0xFF007AFF), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Simple rotation: drag right/down rotates clockwise
                            val degrees = (dragAmount.x + dragAmount.y) * 0.2f
                            onTransform(layer.id, Offset.Zero, 1f, degrees)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom Right: Resize
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = handleOffset, y = handleOffset)
                    .size(handleSize)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color(0xFF007AFF), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Simple scale: drag right/down increases size
                            val scaleChange = 1f + (dragAmount.x + dragAmount.y) / 200f
                            onTransform(layer.id, Offset.Zero, scaleChange, 0f)
                        }
                    }
            )
        }
    }
}
