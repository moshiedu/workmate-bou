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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun StickerBoxComposable(
    layer: StickerLayer,
    isSelected: Boolean,
    bitmapScale: Float,
    bitmapOffset: Offset,
    onSelect: (String) -> Unit,
    onTransform: (String, Offset, Float, Float, Float) -> Unit, // id, pan, scaleXChange, scaleYChange, rotation
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
            .graphicsLayer {
                rotationZ = layer.rotation
                // Combining Flip with ScaleX
                scaleX = layer.scaleX * (if (layer.isFlipped) -1f else 1f)
                scaleY = layer.scaleY
                alpha = layer.opacity
                
                if (layer.hasShadow) {
                    shadowElevation = layer.shadowBlur
                    shape = androidx.compose.ui.graphics.RectangleShape
                }
            }
            // Main Body Gesture (Pan, Zoom, Rotate - MultiTouch)
            .pointerInput(layer.id, layer.isLocked || !isSelected) {
                if (layer.isLocked || !isSelected) return@pointerInput

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
                            // Global Shift Correction for Pan
                            val rad = Math.toRadians(layer.rotation.toDouble())
                            val cos = Math.cos(rad)
                            val sin = Math.sin(rad)
                            val correctedPan = Offset(
                                (panChange.x * cos - panChange.y * sin).toFloat(),
                                (panChange.x * sin + panChange.y * cos).toFloat()
                            )

                            // Apply local zoom to both X and Y (Aspect Ratio Preserved on MultiTouch Zoom)
                            onTransform(layer.id, correctedPan, zoomChange, zoomChange, rotationChange)
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
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
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
                    .then(
                        if (layer.hasBorder) {
                             Modifier.border(layer.borderWidth.dp, Color(layer.borderColor))
                         } else Modifier
                    )
            ) {
                 if (layer.text != null) {
                     Text(
                        text = layer.text,
                        fontSize = 80.sp,
                        color = if (layer.hasTint) Color.Unspecified else Color.Black,
                        modifier = Modifier.align(Alignment.Center)
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
            
            // Selection Overlay
            if (isSelected) {
                Box(
                    modifier = Modifier.matchParentSize()
                        .drawBehind {
                            drawRect(
                                color = Color.White,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        }
                )
            }
        }

        // --- HANDLES ---
        if (isSelected && !layer.isLocked) {
             val touchSize = 48.dp
             val halfTouch = 24.dp
            
             @Composable
             fun Handle(
                alignment: Alignment,
                offsetX: androidx.compose.ui.unit.Dp = 0.dp,
                offsetY: androidx.compose.ui.unit.Dp = 0.dp,
                icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
                iconTint: Color = Color.Black,
                isDelete: Boolean = false,
                onClick: (() -> Unit)? = null,
                onDrag: ((Offset) -> Unit)? = null,
                onDragEnd: (() -> Unit)? = null
            ) {
                val alignOffsetX = when(alignment) {
                    Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -halfTouch
                    Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> halfTouch
                    else -> 0.dp
                }
                val alignOffsetY = when(alignment) {
                    Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -halfTouch
                    Alignment.BottomEnd, Alignment.BottomCenter, Alignment.BottomStart -> halfTouch
                    else -> 0.dp
                }

                Box(
                    modifier = Modifier
                        .align(alignment)
                        .offset(x = alignOffsetX + offsetX, y = alignOffsetY + offsetY)
                        .size(touchSize)
                        .then(
                            if (onDrag != null) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = { onDragEnd?.invoke() }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        onDrag(dragAmount)
                                    }
                                }
                            } else if (onClick != null) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures { onClick() }
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isDelete) 20.dp else 10.dp)
                            .background(if (isDelete) Color(0xFFFF3B30) else Color.White, CircleShape)
                            .border(if (isDelete) 2.dp else 1.dp, if (isDelete) Color.White else Color(0xFFCCCCCC), CircleShape)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(icon, null, tint = if (isDelete) Color.White else iconTint, modifier = Modifier.size(if (isDelete) 12.dp else 10.dp))
                        }
                    }
                }
            }
            
            // Scaled Handle Drag Logic
            fun onScaleHandleDrag(dragAmount: Offset, handleSignX: Float, handleSignY: Float, lockAspect: Boolean = false) {
                 // 1. Inverse Rotate Drag Vector to align with Local Sticker Axes
                 val rad = -Math.toRadians(layer.rotation.toDouble()) // Inverse Rotation
                 val cos = Math.cos(rad)
                 val sin = Math.sin(rad)
                 val localDragX = (dragAmount.x * cos - dragAmount.y * sin).toFloat()
                 val localDragY = (dragAmount.x * sin + dragAmount.y * cos).toFloat()
                 
                 // 2. Project onto Handle Normal Direction (from center)
                 // If handle is Right (1, 0), and I drag Right (+x), localDragX is +ve. SignX is +ve. Product +ve -> Grow.
                 // If handle is Left (-1, 0), and I drag Left (-x), localDragX is -ve. SignX is -ve. Product +ve -> Grow.
                 
                 val widthSensitivity = 100f // Pixels to double size roughly
                 val heightSensitivity = 100f 
                 
                 // Calculate Delta Factors
                 // We apply drag * sign. 
                 // If scaleX is negative (flipped), does logic hold? 
                 // Visual handle is "Right". "Right" in local un-flipped space is +X.
                 // If Flipped, "Right" Visual is -X Local?
                 // No, Flipped is scaleX = -1. Local axes are flipped?
                 // Let's assume Handles rotate with the object. If flipped, the object is flipped visually.
                 // But handles position logic in Box(Modifier.align) ignores render transform (flip).
                 // So Handles are always Top/Right/Left/Bottom relative to Un-Transformed Box.
                 // BUT the Content is Flipped.
                 // So "Right" Handle is still +X in layout coordinates.
                 // So we don't need to account for Flip in drag direction unless Flip affects local axes.
                 // ScaleX being negative affects content drawing, NOT layout bounds or handle positions usually in this simple box model.
                 
                 val dx = localDragX * handleSignX
                 val dy = localDragY * handleSignY
                 
                 var sxChange = 1f + (dx / widthSensitivity)
                 var syChange = 1f + (dy / heightSensitivity)
                 
                 if (handleSignX == 0f) sxChange = 1f
                 if (handleSignY == 0f) syChange = 1f

                 if (lockAspect) {
                     // Use the dominant axis change for both
                     // Or average? Usually corner drag uses projected distance along diagonal.
                     // Simply: Use MAX change.
                     val factor = if (abs(sxChange - 1f) > abs(syChange - 1f)) sxChange else syChange
                     sxChange = factor
                     syChange = factor
                 }
                 
                 onTransform(layer.id, Offset.Zero, sxChange, syChange, 0f)
            }

            // Top-Left: Delete
            Handle(
                alignment = Alignment.TopStart,
                icon = Icons.Default.Close,
                isDelete = true,
                onClick = { onDelete(layer.id) }
            )
            
            // Top-Right: Rotate
             Handle(
                alignment = Alignment.TopEnd,
                icon = Icons.Default.Refresh,
                onDrag = { dragAmount ->
                    val degrees = (dragAmount.x + dragAmount.y) * 0.2f
                    onTransform(layer.id, Offset.Zero, 1f, 1f, degrees)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )

            // Bottom-Right: Scale (Uniform)
            Handle(
                alignment = Alignment.BottomEnd,
                icon = Icons.Default.OpenInFull,
                onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, 1f, 1f, lockAspect = true) },
                onDragEnd = { onTransformEnd(layer.id) }
            )
            
             // Bottom-Left: Scale (Uniform)
             Handle(
                alignment = Alignment.BottomStart,
                 // Icon? User mentioned circle.
                onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, -1f, 1f, lockAspect = true) },
                onDragEnd = { onTransformEnd(layer.id) }
             )
             
             // --- SIDES (Stretching) ---
             
             // Top Center - Stretch Y
             Handle(
                 alignment = Alignment.TopCenter,
                 onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, 0f, -1f) },
                 onDragEnd = { onTransformEnd(layer.id) }
             )
             
             // Bottom Center - Stretch Y
             Handle(
                 alignment = Alignment.BottomCenter,
                 onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, 0f, 1f) },
                 onDragEnd = { onTransformEnd(layer.id) }
             )
             
             // Left Center - Stretch X
             Handle(
                 alignment = Alignment.CenterStart,
                 onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, -1f, 0f) },
                 onDragEnd = { onTransformEnd(layer.id) }
             )
             
             // Right Center - Stretch X
             Handle(
                 alignment = Alignment.CenterEnd,
                 onDrag = { dragAmount -> onScaleHandleDrag(dragAmount, 1f, 0f) },
                 onDragEnd = { onTransformEnd(layer.id) }
             )
        }
    }
}

