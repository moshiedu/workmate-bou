package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer
import kotlin.math.roundToInt

@Composable
fun StickerBoxComposable(
    layer: StickerLayer,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onTransform: (String, androidx.compose.ui.geometry.Offset, Float, Float) -> Unit,
    onDelete: (String) -> Unit,
    onFlip: ((String) -> Unit)? = null // Optional flip callback
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(layer.x.roundToInt(), layer.y.roundToInt()) }
            .graphicsLayer(
                rotationZ = layer.rotation,
                scaleX = layer.scale * (if (layer.isFlipped) -1f else 1f),
                scaleY = layer.scale
            )
            .pointerInput(layer.id) {
                detectTapGestures(
                    onTap = { onSelect(layer.id) }
                )
            }
            .pointerInput(layer.id) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    if (isSelected || true) { // Allow transform even if not strictly "selected" if user drags it? usually select first.
                         // Better UX: Auto-select on drag start, but for now strict selection
                        if (isSelected) {
                            onTransform(layer.id, pan, zoom, rotation)
                        }
                    }
                }
            }
    ) {
        // Sticker Content
        Box(
            modifier = Modifier
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(if (isSelected) 8.dp else 0.dp) // Padding for border
        ) {
            if (layer.text != null) {
                // Emoji / Text Sticker
                Text(
                    text = layer.text,
                    fontSize = 64.sp // Base size for emoji
                )
            } else if (layer.resId != 0) {
                // Drawable Sticker
                Image(
                    painter = painterResource(id = layer.resId),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Selection Controls (Handles)
        if (isSelected) {
            // Delete Handle (Top Left)
            Box(
                modifier = Modifier
                    .offset(x = (-12).dp, y = (-12).dp)
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopStart)
                    .pointerInput(Unit) {
                        detectTapGestures { onDelete(layer.id) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            
            // Flip Handle (Top Right)
             Box(
                modifier = Modifier
                    .offset(x = 12.dp, y = (-12).dp)
                    .size(24.dp)
                    .background(Color.Blue, CircleShape)
                    .align(Alignment.TopEnd)
                    .pointerInput(Unit) { // Flip logic
                        detectTapGestures { onFlip?.invoke(layer.id) }
                    },
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    Icons.Default.Flip, 
                    contentDescription = "Flip",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Resize Handle (Bottom Right)
            Box(
                modifier = Modifier
                    .offset(x = 12.dp, y = 12.dp)
                    .size(24.dp)
                    .background(Color.Green, CircleShape)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            // Scale Logic only: Dragging Outward (Positive X/Y) increases size
                            val scaleChange = 1f + (dragAmount.x + dragAmount.y) / 200f
                            onTransform(layer.id, androidx.compose.ui.geometry.Offset.Zero, scaleChange, 0f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    Icons.Default.OpenInFull, // Diagonal arrows for Resize
                    contentDescription = "Resize",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
