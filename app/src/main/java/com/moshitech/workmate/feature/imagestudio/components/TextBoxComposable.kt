package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
            .pointerInput(layer.id, layer.isLocked, isEditing) {
                if (!layer.isLocked && !isEditing) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        onTransform(layer.id, pan, zoom, rotation)
                    }
                }
            }
            .pointerInput(layer.id) {
                detectTapGestures(
                    onTap = {
                        if (!layer.isLocked) {
                            onSelect(layer.id) // Just select, toggles toolbar
                        }
                    }
                )
            }
    ) {
        // Content Box with styling
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max) // Allow auto-sizing width based on content
                .height(IntrinsicSize.Min)
                .background(
                    if (layer.showBackground) Color(layer.backgroundColor).copy(alpha = layer.backgroundOpacity) 
                    else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(layer.backgroundPadding.dp)
                // Selection Border & Visual Handles (Visual Only)
                .drawBehind {
                    if (isSelected && !isEditing) {
                        val strokeWidth = 2.dp.toPx() / layer.scale
                        // Dashed Border
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        drawRoundRect(
                            color = Color(0xFF2196F3), // Blue selection color
                            cornerRadius = CornerRadius(4.dp.toPx()),
                            style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                        )
                        
                        // Corner Handles (Visual cues for "Resize")
                        val handleRadius = 5.dp.toPx() / layer.scale
                        // Top Left
                        drawCircle(Color.White, radius = handleRadius, center = Offset(0f, 0f))
                        drawCircle(Color(0xFF2196F3), radius = handleRadius, center = Offset(0f, 0f), style = Stroke(strokeWidth))
                        // Top Right
                        drawCircle(Color.White, radius = handleRadius, center = Offset(size.width, 0f))
                        drawCircle(Color(0xFF2196F3), radius = handleRadius, center = Offset(size.width, 0f), style = Stroke(strokeWidth))
                        // Bottom Left
                        drawCircle(Color.White, radius = handleRadius, center = Offset(0f, size.height))
                        drawCircle(Color(0xFF2196F3), radius = handleRadius, center = Offset(0f, size.height), style = Stroke(strokeWidth))
                        // Bottom Right
                        drawCircle(Color.White, radius = handleRadius, center = Offset(size.width, size.height))
                        drawCircle(Color(0xFF2196F3), radius = handleRadius, center = Offset(size.width, size.height), style = Stroke(strokeWidth))
                    }
                }
        ) {
            val textStyle = TextStyle(
                color = Color(layer.color),
                fontSize = layer.fontSize.sp,
                fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (layer.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (layer.isUnderline) TextDecoration.Underline else TextDecoration.None,
                textAlign = when (layer.alignment) {
                    TextAlignment.LEFT -> TextAlign.Left
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.Right
                },
                letterSpacing = layer.letterSpacing.sp,
                lineHeight = (layer.fontSize * layer.lineHeight).sp,
                shadow = if (layer.hasShadow) Shadow(
                    color = Color(layer.shadowColor),
                    offset = Offset(2f, 2f),
                    blurRadius = layer.shadowBlur
                ) else null
            )

            if (isEditing) {
                // Inline editing with visible cursor
                Box {
                    if (layer.text.isEmpty()) {
                        Text(
                            text = "Type here...",
                            style = textStyle.copy(color = Color.Gray),
                            modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                        )
                    }
                    BasicTextField(
                        value = layer.text,
                        onValueChange = { onTextChange(layer.id, it) },
                        textStyle = textStyle,
                        modifier = Modifier.defaultMinSize(minWidth = 50.dp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                    )
                }
            } else {
                // Display text
                Text(
                    text = if (layer.text.isEmpty()) "Double tap to edit" else layer.text,
                    style = textStyle,
                    modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                )
            }
            
            // Locked Icon Overlay
            if (layer.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(2.dp)
                )
            }
        }
    }
}
