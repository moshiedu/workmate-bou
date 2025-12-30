package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeLayer
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeType
import com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.Rect
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.PI

@Composable
fun ShapeBoxComposable(
    layer: ShapeLayer,
    isSelected: Boolean,
    bitmapScale: Float,
    bitmapOffset: Offset,
    onSelect: (String) -> Unit,
    onTransform: (String, Offset, Float, Float) -> Unit,
    onResize: (String, Float, Float, Float, Float) -> Unit, // widthDelta, heightDelta, xDelta, yDelta
    onTransformEnd: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewConfiguration = androidx.compose.ui.platform.LocalViewConfiguration.current

    // Pre-calculate path size in pixels (Inner Shape Size)
    // Use simple float math for path, but we'll align display containers to pixels
    val shapeWidthPx = layer.width * layer.scale * bitmapScale
    val shapeHeightPx = layer.height * layer.scale * bitmapScale
    val shapeSize = androidx.compose.ui.geometry.Size(shapeWidthPx, shapeHeightPx)
    
    // Calculate border thickness offset - Round to Pixel Integer for stability
    val scaledStrokeRaw = if (layer.strokeWidth > 0f) layer.strokeWidth * layer.scale * bitmapScale else 0f
    val scaledStrokePxInt = scaledStrokeRaw.roundToInt()
    val scaledStrokePx = scaledStrokePxInt.toFloat()
    
    // Total Size = Shape + 2 * Stroke
    // Box dimensions must use Dp, so convert Px -> Dp correctly
    val totalWidthPx = shapeWidthPx + (2 * scaledStrokePx)
    val totalHeightPx = shapeHeightPx + (2 * scaledStrokePx)

    val shapePath = remember(layer.type, shapeSize) {
        getComposePathForShape(layer.type, shapeSize)
    }

    Box(
        modifier = modifier
            // Position using bitmap coordinates mapped to screen, offset by stroke thickness (Integers)
            .offset {
                IntOffset(
                    ((layer.x * bitmapScale + bitmapOffset.x).roundToInt() - scaledStrokePxInt),
                    ((layer.y * bitmapScale + bitmapOffset.y).roundToInt() - scaledStrokePxInt)
                )
            }
            .size(
                width = with(density) { totalWidthPx.toDp() },
                height = with(density) { totalHeightPx.toDp() }
            )
            .graphicsLayer {
                rotationZ = layer.rotation
                alpha = layer.opacity
            }
            // ... gestures ...
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                enabled = !layer.isLocked
            ) {
                 onSelect(layer.id)
            }
            .pointerInput(layer.id, layer.isLocked, layer.rotation) {
                if (layer.isLocked) return@pointerInput

                awaitEachGesture {
                    var zoom = 1f
                    var pan = Offset.Zero
                    var rotation = 0f
                    var pastTouchSlop = false

                    awaitFirstDown(requireUnconsumed = false)

                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) break

                        // Gestures logic remains same
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
                            // Correct pan for rotation
                            val rad = Math.toRadians(layer.rotation.toDouble())
                            val cos = Math.cos(rad)
                            val sin = Math.sin(rad)
                            val correctedPan = Offset(
                                (panChange.x * cos - panChange.y * sin).toFloat() / bitmapScale,
                                (panChange.x * sin + panChange.y * cos).toFloat() / bitmapScale
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
    ) {
        // Shape Rendering
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
                .then(
                    if (isSelected) {
                         Modifier.drawBehind {
                            // Selection Border (Dashed) - drawn around the box (which now includes border)
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
        ) {
            val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint()
            
            drawIntoCanvas { canvas ->
                val composePaint = androidx.compose.ui.graphics.Paint()
                val frameworkPaint = composePaint.asFrameworkPaint()
                
                // Translate inward by exact stroke pixel-width for perfect alignment
                canvas.save()
                canvas.translate(scaledStrokePx, scaledStrokePx)

                if (layer.hasShadow || layer.isFilled || layer.strokeWidth > 0f) {
                    val androidPath = shapePath.asAndroidPath()
                    
                    // 1. Draw Shadow
                    if (layer.hasShadow) {
                        frameworkPaint.color = layer.shadowColor
                        frameworkPaint.style = android.graphics.Paint.Style.FILL
                        frameworkPaint.setShadowLayer(
                            layer.shadowBlur,
                            layer.shadowX,
                            layer.shadowY,
                            layer.shadowColor
                        )
                        canvas.nativeCanvas.drawPath(androidPath, frameworkPaint)
                        frameworkPaint.clearShadowLayer()
                    }

                    // 2. Draw Fill
                    if (layer.isFilled) {
                        frameworkPaint.style = android.graphics.Paint.Style.FILL
                        frameworkPaint.color = layer.color
                        frameworkPaint.pathEffect = null
                        canvas.nativeCanvas.drawPath(androidPath, frameworkPaint)
                    }

                    // 3. Draw Outside Border
                    if (layer.strokeWidth > 0f) {
                        frameworkPaint.style = android.graphics.Paint.Style.STROKE
                        frameworkPaint.color = layer.borderColor
                        frameworkPaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        frameworkPaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        
                         when (layer.strokeStyle) {
                            StrokeStyle.DASHED -> frameworkPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 15f), 0f)
                            StrokeStyle.DOTTED -> frameworkPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 10f), 0f)
                            StrokeStyle.LONG_DASH -> frameworkPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(50f, 20f), 0f)
                            StrokeStyle.DASH_DOT -> frameworkPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 15f, 5f, 15f), 0f)
                            StrokeStyle.SOLID -> frameworkPaint.pathEffect = null
                        }

                        val isClosed = layer.type != ShapeType.LINE && layer.type != ShapeType.ARROW
                        
                        if (isClosed) {
                            frameworkPaint.strokeWidth = scaledStrokeRaw * 2f
                            
                            canvas.save()
                            canvas.clipPath(shapePath, ClipOp.Difference)
                            canvas.nativeCanvas.drawPath(androidPath, frameworkPaint)
                            canvas.restore()
                        } else {
                            frameworkPaint.strokeWidth = scaledStrokeRaw
                            canvas.nativeCanvas.drawPath(androidPath, frameworkPaint)
                        }
                    }
                }
                
                canvas.restore() // Restore translation
            }
        }

        if (isSelected && !layer.isLocked) {
            val touchSize = 48.dp
            val halfTouch = 24.dp
            
            // Reusable Handle Composable
            @Composable
            fun Handle(
                alignment: Alignment,
                offsetX: androidx.compose.ui.unit.Dp = 0.dp,
                offsetY: androidx.compose.ui.unit.Dp = 0.dp,
                icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
                iconTint: Color = Color.Black,
                isSideHandle: Boolean = false,
                isDelete: Boolean = false,
                onClick: (() -> Unit)? = null,
                onDrag: ((Offset) -> Unit)? = null,
                onDragEnd: (() -> Unit)? = null
            ) {
                // Determine Offset to center the 48dp box on the edge
                // Alignment puts the box INSIDE. We need to push it OUT by halfTouch.
                val alignOffsetX = when(alignment) {
                    Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -halfTouch
                    Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> halfTouch
                    else -> 0.dp
                }
                val alignOffsetY = when(alignment) {
                    Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> -halfTouch
                    Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> halfTouch
                    else -> 0.dp
                }

                val visualSize = if (isSideHandle) 10.dp else if (isDelete) 20.dp else 14.dp
                val bgColor = if (isDelete) Color(0xFFFF3B30) else Color.White
                val borderColor = if (isDelete) Color.White else Color(0xFFCCCCCC)
                val borderWidth = if (isDelete) 2.dp else 1.dp

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
                            .size(visualSize)
                            .background(bgColor, CircleShape)
                            .border(borderWidth, borderColor, CircleShape)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (icon != null) {
                            Icon(icon, null, tint = if (isDelete) Color.White else iconTint, modifier = Modifier.size(if (isDelete) 12.dp else 10.dp))
                        }
                    }
                }
            }

            // --- CORNER HANDLES (Uniform Scale / Rotate / Delete) ---
            
            // Top-Left: Delete
            Handle(
                alignment = Alignment.TopStart,
                icon = Icons.Default.Close,
                isDelete = true,
                onClick = { onDelete(layer.id) }
            )

            // Bottom-Right: Scale (Uniform)
            Handle(
                alignment = Alignment.BottomEnd,
                icon = Icons.Default.OpenInFull,
                onDrag = { dragAmount ->
                    val scaleFactor = 1f + dragAmount.getDistance() / 200f
                    val direction = dragAmount / dragAmount.getDistance()
                    val outward = direction.x + direction.y > 0
                    val finalScale = if (outward) scaleFactor else 1f / scaleFactor.coerceAtLeast(0.1f)
                    onTransform(layer.id, Offset.Zero, finalScale, 0f)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )

            // Top-Right: Rotate
            Handle(
                alignment = Alignment.TopEnd,
                icon = Icons.Default.Refresh,
                onDrag = { dragAmount ->
                    val degrees = (dragAmount.x + dragAmount.y) * 0.2f
                    onTransform(layer.id, Offset.Zero, 1f, degrees)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )
            
            // --- SIDE HANDLES (Stretch / Resize) ---
            // Only show side handles if not rotated (simplification for MVP, or handle rotation math)
            // For now, allow stretching which might skew if rotated, but usually users want that.
            // Actually, if we just modify width/height, it scales in local space, so safe even if rotated.
            
            // Right Side (Width+)
            Handle(
                alignment = Alignment.CenterEnd,
                isSideHandle = true,
                onDrag = { dragAmount ->
                     // DragAmount is already in local coordinates.
                     // X-axis drag increases width directly.
                     val dWidth = dragAmount.x / bitmapScale
                     onResize(layer.id, dWidth, 0f, 0f, 0f)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )
 
             // Bottom Side (Height+)
            Handle(
                alignment = Alignment.BottomCenter,
                isSideHandle = true,
                onDrag = { dragAmount ->
                     // Y-axis drag increases height directly.
                     val dHeight = dragAmount.y / bitmapScale
                     onResize(layer.id, 0f, dHeight, 0f, 0f)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )
            
            // Left Side (Width-)
            Handle(
                alignment = Alignment.CenterStart,
                isSideHandle = true,
                onDrag = { dragAmount ->
                     val rad = -Math.toRadians(layer.rotation.toDouble())
                     val dx = (dragAmount.x * Math.cos(rad) - dragAmount.y * Math.sin(rad)).toFloat()
                     // Dragging left means reducing width if moving right, increasing if moving left
                     onResize(layer.id, -dx / bitmapScale, 0f, dx / bitmapScale, 0f) 
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )

             // Top Side (Height-)
            Handle(
                alignment = Alignment.TopCenter,
                isSideHandle = true,
                onDrag = { dragAmount ->
                     val rad = -Math.toRadians(layer.rotation.toDouble())
                     val dy = (dragAmount.x * Math.sin(rad) + dragAmount.y * Math.cos(rad)).toFloat()
                     onResize(layer.id, 0f, -dy / bitmapScale, 0f, dy / bitmapScale)
                },
                onDragEnd = { onTransformEnd(layer.id) }
            )
        }
    }
}

private fun createPolygonPath(sides: Int, width: Float, height: Float): Path {
    val path = Path()
    val radius = kotlin.math.min(width, height) / 2f
    val cx = width / 2f
    val cy = height / 2f
    val angleStep = (2 * kotlin.math.PI / sides)
    val startAngle = -kotlin.math.PI / 2
    
    for (i in 0 until sides) {
        val angle = startAngle + i * angleStep
        val px = cx + (radius * kotlin.math.cos(angle)).toFloat()
        val px2 = cx + (radius * kotlin.math.cos(angle)).toFloat()
        val py = cy + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

private fun createStarPath(points: Int, width: Float, height: Float, innerRatio: Float = 0.4f): Path {
    val path = Path()
    val outerRadius = kotlin.math.min(width, height) / 2f
    val innerRadius = outerRadius * innerRatio
    val cx = width / 2f
    val cy = height / 2f
    val angleStep = Math.PI / points
    val startAngle = -Math.PI / 2
    
    for (i in 0 until (points * 2)) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * angleStep
        val px = cx + (radius * kotlin.math.cos(angle)).toFloat()
        val py = cy + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    return path
}

// Helper to generate Compose Path for shapes
private fun getComposePathForShape(type: ShapeType, size: androidx.compose.ui.geometry.Size): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    when(type) {
        ShapeType.RECTANGLE -> path.addRect(Rect(0f, 0f, size.width, size.height))
        ShapeType.CIRCLE -> path.addOval(Rect(0f, 0f, size.width, size.height))
        ShapeType.LINE -> {
             path.moveTo(0f, size.height / 2)
             path.lineTo(size.width, size.height / 2)
        }
        ShapeType.ARROW -> {
             path.moveTo(0f, size.height / 2)
             val headSize = kotlin.math.min(size.width, size.height) * 0.2f
             path.lineTo(size.width, size.height / 2)
             // Arrow Head
             path.moveTo(size.width, size.height / 2)
             path.lineTo(size.width - headSize, size.height / 2 - headSize / 2)
             path.moveTo(size.width, size.height / 2)
             path.lineTo(size.width - headSize, size.height / 2 + headSize / 2)
        }
        ShapeType.TRIANGLE -> {
            path.moveTo(size.width / 2f, 0f)
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
        }
        ShapeType.PENTAGON -> return createPolygonPath(5, size.width, size.height)
        ShapeType.STAR -> return createStarPath(5, size.width, size.height)
    }
    return path
}
