package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateCentroidSize
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
import kotlin.math.roundToInt // Added for coordinate mapping
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.PI

@Composable
fun StickerBoxComposable(
    layer: StickerLayer,
    isSelected: Boolean,
    bitmapScale: Float,
    bitmapOffset: Offset,
    onSelect: (String) -> Unit,
    onTransform: (String, Offset, Float, Float) -> Unit,
    onTransformEnd: (String) -> Unit,
    onDelete: (String) -> Unit,
    onFlip: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewConfiguration = androidx.compose.ui.platform.LocalViewConfiguration.current

    Box(
        modifier = modifier
            // Position in screen coordinates using bitmap coords
            .offset {
                IntOffset(
                    (layer.x * bitmapScale + bitmapOffset.x).roundToInt(),
                    (layer.y * bitmapScale + bitmapOffset.y).roundToInt()
                )
            }
            // Let content determine size naturally (based on scaled sticker)
            // Do NOT use fixed .size() here!
            .graphicsLayer {
                // Visual transforms only
                rotationZ = layer.rotation
                scaleX = layer.scale * (if (layer.isFlipped) -1f else 1f)
                scaleY = layer.scale
                alpha = layer.opacity

                // Optional: elevation-based shadow (no offset control)
                if (layer.hasShadow) {
                    shadowElevation = layer.shadowBlur
                    shape = androidx.compose.ui.graphics.RectangleShape
                }
            }
            .pointerInput(layer.id, layer.isLocked || !isSelected) {
                if (layer.isLocked || !isSelected) return@pointerInput

                // Same robust gesture detection as TextBox
                awaitEachGesture {
                    var zoom = 1f
                    var pan = Offset.Zero
                    var rotation = 0f
                    var pastTouchSlop = false

                    awaitFirstDown(requireUnconsumed = false)

                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) break

                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()

                        if (!pastTouchSlop) {
                            zoom *= zoomChange
                            rotation += rotationChange
                            pan += panChange

                            val centroidSize = event.calculateCentroidSize(useCurrent = false)
                            val zoomMotion = abs(1 - zoom) * centroidSize
                            val rotationMotion = abs(rotation * Math.PI.toFloat() * centroidSize / 180f)
                            val panMotion = pan.getDistance()

                            if (zoomMotion > viewConfiguration.touchSlop ||
                                rotationMotion > viewConfiguration.touchSlop ||
                                panMotion > viewConfiguration.touchSlop
                            ) {
                                pastTouchSlop = true
                            }
                        }

                        if (pastTouchSlop) {
                            // Correct pan for current rotation (prevents drift)
                            val rad = Math.toRadians(layer.rotation.toDouble())
                            val cos = Math.cos(rad)
                            val sin = Math.sin(rad)
                            val correctedPan = Offset(
                                (panChange.x * cos - panChange.y * sin).toFloat(),
                                (panChange.x * sin + panChange.y * cos).toFloat()
                            )

                            onTransform(layer.id, correctedPan, zoomChange, rotationChange)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }

                        if (!event.changes.any { it.pressed }) break
                    }

                    if (pastTouchSlop) {
                        onTransformEnd(layer.id)
                    }
                }
            }
            .pointerInput(layer.id) {
                detectTapGestures { onSelect(layer.id) }
            }
    ) {
        // Sticker Visual Content
        Box(
            modifier = Modifier
                // Center content so rotation pivot is correct
                .size(100.dp) // Base size for the sticker content itself
                .padding(8.dp) // Breathing room for handles
                // Custom shadow with offset support
                .then(
                    if (layer.hasShadow) {
                        Modifier
                            .offset(
                                x = with(density) { layer.shadowOffsetX.toDp() },
                                y = with(density) { layer.shadowOffsetY.toDp() }
                            )
                            .shadow(
                                elevation = layer.shadowBlur.dp,
                                shape = androidx.compose.ui.graphics.RectangleShape,
                                ambientColor = Color(layer.shadowColor),
                                spotColor = Color(layer.shadowColor)
                            )
                            .offset(
                                x = with(density) { -layer.shadowOffsetX.toDp() },
                                y = with(density) { -layer.shadowOffsetY.toDp() }
                            )
                    } else Modifier
                )
                // Selection dashed border (visual only)
                .then(
                    if (isSelected) {
                        Modifier.drawBehind {
                            drawRect(
                                color = Color.White,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        }
                    } else Modifier
                )
                // User-defined border
                .then(
                    if (layer.hasBorder) {
                        Modifier.drawBehind {
                            drawRect(
                                color = Color(layer.borderColor),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = layer.borderWidth)
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (layer.text != null) {
                Text(
                    text = layer.text,
                    fontSize = 80.sp, // Adjust base size as needed
                    color = if (layer.hasTint) Color.Unspecified else Color.Black,
                    modifier = Modifier
                        .then(
                            if (layer.hasTint) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    drawRect(
                                        Color(layer.tintColor).copy(alpha = layer.tintStrength),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop
                                    )
                                }
                            } else Modifier
                        )
                )
            } else if (layer.resId != 0) {
                Image(
                    painter = painterResource(id = layer.resId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .matchParentSize()
                        .then(
                            if (layer.hasTint) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    drawRect(
                                        Color(layer.tintColor).copy(alpha = layer.tintStrength),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop
                                    )
                                }
                            } else Modifier
                        )
                )
            }
        }

        // Selection Handles
        if (isSelected && !layer.isLocked) {
            val handleSize = 32.dp
            val handleOffset = 16.dp

            // Delete (Top-Left)
            IconButton(
                onClick = { onDelete(layer.id) },
                modifier = Modifier
                    .offset(x = -handleOffset, y = -handleOffset)
                    .size(handleSize)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }

            // Flip (Top-Right)
            if (onFlip != null) {
                IconButton(
                    onClick = { onFlip(layer.id) },
                    modifier = Modifier
                        .offset(x = handleOffset, y = -handleOffset)
                        .size(handleSize)
                        .background(Color(0xFF2196F3), CircleShape)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Flip, null, tint = Color.White)
                }
            }

            // Resize/Rotate Handle (Bottom-Right)
            Box(
                modifier = Modifier
                    .offset(x = handleOffset, y = handleOffset)
                    .size(handleSize)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color(0xFF2196F3), CircleShape)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onTransformEnd(layer.id) }
                        ) { change, dragAmount ->
                            change.consume()

                            // Better scale: use distance from center
                            val scaleFactor = 1f + dragAmount.getDistance() / 200f
                            val direction = dragAmount / dragAmount.getDistance()
                            val outward = direction.x + direction.y > 0

                            val finalScale = if (outward) scaleFactor else 1f / scaleFactor.coerceAtLeast(0.1f)
                            onTransform(layer.id, Offset.Zero, finalScale, 0f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.OpenInFull, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
            }
        }
    }
}
