package com.moshitech.workmate.feature.imagestudio.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeLayer
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeType
import com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
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
    onTransformEnd: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewConfiguration = androidx.compose.ui.platform.LocalViewConfiguration.current

    Box(
        modifier = modifier
            // Position using bitmap coordinates mapped to screen
            .offset {
                IntOffset(
                    (layer.x * bitmapScale + bitmapOffset.x).roundToInt(),
                    (layer.y * bitmapScale + bitmapOffset.y).roundToInt()
                )
            }
            // Intrinsic size based on shape's dimensions
            .size(
                width = with(density) { layer.width.toDp() },
                height = with(density) { layer.height.toDp() }
            )
            .graphicsLayer {
                scaleX = layer.scale
                scaleY = layer.scale
                rotationZ = layer.rotation
                alpha = layer.opacity
            }
            .pointerInput(layer.id, layer.isLocked) {
                if (layer.isLocked) return@pointerInput
                detectTapGestures { onSelect(layer.id) }
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
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (layer.hasShadow) {
                        Modifier.shadow(
                            elevation = with(density) { layer.shadowBlur.toDp() },
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            ambientColor = Color(layer.shadowColor),
                            spotColor = Color(layer.shadowColor)
                        )
                    } else Modifier
                )
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
        ) {
            val color = Color(layer.color)
            val pathEffect = when (layer.strokeStyle) {
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DASHED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DOTTED -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.LONG_DASH -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(50f, 20f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DASH_DOT -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 5f, 15f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.SOLID -> null
            }

            // Using drawIntoCanvas to leverage native paint for consistent rendering if needed, 
            // but for Compose Canvas, we can use drawScope methods directly which are simpler.
            // The previous 'drawIntoCanvas' block was a bit complex. Let's simplify to standard Compose DrawScope where possible
            // OR stick to the logic that works.
            // For now, I'll use standard DrawScope commands as they are cleaner, unless shadow requires native.
            // Actually, I put .shadow modifier on the Canvas, so standard drawing is fine.
            
            val stroke = Stroke(
                width = layer.strokeWidth,
                pathEffect = pathEffect,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )

            when (layer.type) {
                ShapeType.RECTANGLE -> drawRect(color, style = if (layer.isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke)
                ShapeType.CIRCLE -> drawOval(color, style = if (layer.isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke)
                ShapeType.LINE -> drawLine(
                    color,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    strokeWidth = layer.strokeWidth,
                    pathEffect = pathEffect,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                ShapeType.ARROW -> {
                    drawLine(
                        color,
                        Offset(0f, size.height / 2),
                        Offset(size.width, size.height / 2),
                        strokeWidth = layer.strokeWidth,
                        pathEffect = pathEffect,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    val arrowSize = layer.strokeWidth * 3f + 10f
                    val arrowPath = Path().apply {
                        moveTo(size.width, size.height / 2)
                        lineTo(size.width - arrowSize, size.height / 2 - arrowSize / 1.5f)
                        lineTo(size.width - arrowSize, size.height / 2 + arrowSize / 1.5f)
                        close()
                    }
                    drawPath(arrowPath, color, style = androidx.compose.ui.graphics.drawscope.Fill)
                }
                ShapeType.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, color, style = if (layer.isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke)
                }
                ShapeType.PENTAGON -> drawPath(createPolygonPath(5, size.width, size.height), color, style = if (layer.isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke)
                ShapeType.STAR -> drawPath(createStarPath(5, size.width, size.height), color, style = if (layer.isFilled) androidx.compose.ui.graphics.drawscope.Fill else stroke)
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

            // Resize (Bottom-Right)
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

            // Rotate (Top-Right)
            Box(
                modifier = Modifier
                    .offset(x = handleOffset, y = -handleOffset)
                    .size(handleSize)
                    .background(Color(0xFF2196F3), CircleShape)
                    .align(Alignment.TopEnd)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onTransformEnd(layer.id) }
                        ) { change, dragAmount ->
                            change.consume()
                            val degrees = (dragAmount.x + dragAmount.y) * 0.2f
                            onTransform(layer.id, Offset.Zero, 1f, degrees)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
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
