package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeLayer
import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

@Composable
fun ShapeBoxComposable(
    layer: ShapeLayer,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onTransform: (String, Offset, Float, Float) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            // Standard 3.1 & 3.2: Position via offset, visual transforms via graphicsLayer
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    (layer.x - layer.width / 2).toInt(),
                    (layer.y - layer.height / 2).toInt()
                )
            }
            .graphicsLayer {
                // translationX = layer.x // REMOVED per contract
                // translationY = layer.y // REMOVED per contract
                scaleX = layer.scale
                scaleY = layer.scale
                rotationZ = layer.rotation
                alpha = layer.opacity
            }
            .size(layer.width.dp, layer.height.dp) // Size is intrinsic to the box
            // Center the pivot? GraphicsLayer default is Center.
            .pointerInput(layer.id, isSelected) {
                detectTapGestures {
                    onSelect(layer.id)
                }
            }
            .pointerInput(layer.id, layer.rotation) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    // Standard 4.1: Convert local rotation to global delta, but DO NOT scale position
                    val rad = Math.toRadians(layer.rotation.toDouble())
                    val cos = Math.cos(rad)
                    val sin = Math.sin(rad)
                    
                    val rotX = pan.x * cos - pan.y * sin
                    val rotY = pan.x * sin + pan.y * cos
                    
                    // Sending raw rotated delta. Scale handling belongs in proper conversion layer.
                    val correctedPan = Offset(rotX.toFloat(), rotY.toFloat())
                    
                    onTransform(layer.id, correctedPan, zoom, rotation)
                }
            }
    ) {
        // Render Shape
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val color = Color(layer.color)
            
            // Resolve PathEffect for Stroke Style
            val pathEffect = when (layer.strokeStyle) {
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DASHED -> 
                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DOTTED -> 
                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.LONG_DASH -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(50f, 20f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.DASH_DOT -> androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 5f, 15f), 0f)
                com.moshitech.workmate.feature.imagestudio.viewmodel.StrokeStyle.SOLID -> null
            }
            
            val stroke = Stroke(
                width = layer.strokeWidth,
                pathEffect = pathEffect,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
            
            // Set up Paint for Shadows
            val frameworkPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint()
            if (layer.hasShadow) {
                frameworkPaint.setShadowLayer(
                    layer.shadowBlur,
                    layer.shadowX,
                    layer.shadowY,
                    layer.shadowColor
                )
            }
            // We need to draw into a layer or use drawIntoCanvas to utilize setShadowLayer effectively if utilizing native paint
            // simpler approach: Draw Shadow manually if needed, or rely on Paint.
            // Using `drawIntoCanvas` allows direct Native Paint usage which supports shadows better.
            
            drawIntoCanvas { canvas ->
                val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                    this.color = color
                    if (!layer.isFilled) {
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = layer.strokeWidth
                        this.pathEffect = pathEffect
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        this.strokeJoin = androidx.compose.ui.graphics.StrokeJoin.Round
                    } else {
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Fill
                    }
                }
                
                if (layer.hasShadow) {
                    shadowPaint.asFrameworkPaint().setShadowLayer(
                        layer.shadowBlur,
                        layer.shadowX,
                        layer.shadowY,
                        layer.shadowColor
                    )
                }
                
                // Drawing Logic using native canvas equivalent or Wrapper
                when (layer.type) {
                     ShapeType.RECTANGLE -> {
                         canvas.drawRect(
                             androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                             shadowPaint
                         )
                     }
                     ShapeType.CIRCLE -> {
                         // Oval fits in bounds
                         canvas.drawOval(
                             androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                             shadowPaint
                         )
                     }
                     ShapeType.LINE -> {
                         // Force stroke for Line
                         shadowPaint.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                         canvas.drawLine(
                             Offset(0f, size.height / 2),
                             Offset(size.width, size.height / 2),
                             shadowPaint
                         )
                     }
                     ShapeType.ARROW -> {
                         shadowPaint.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                         val start = Offset(0f, size.height / 2)
                         val end = Offset(size.width, size.height / 2)
                         
                         // Arrow Line
                         canvas.drawLine(start, end, shadowPaint)
                         
                         // Arrow Head
                         val arrowSize = layer.strokeWidth * 3f + 10f
                         val arrowPath = Path().apply {
                            moveTo(end.x, end.y)
                            lineTo(end.x - arrowSize, end.y - arrowSize / 1.5f)
                            lineTo(end.x - arrowSize, end.y + arrowSize / 1.5f)
                            close()
                         }
                         
                         // Fill Arrow Head
                         val headPaint = androidx.compose.ui.graphics.Paint().apply {
                             this.color = color
                             this.style = androidx.compose.ui.graphics.PaintingStyle.Fill
                             if (layer.hasShadow) {
                                  this.asFrameworkPaint().setShadowLayer(
                                    layer.shadowBlur, layer.shadowX, layer.shadowY, layer.shadowColor
                                  )
                             }
                         }
                         canvas.drawPath(arrowPath, headPaint)
                     }
                     ShapeType.TRIANGLE -> {
                         val path = Path().apply {
                             moveTo(size.width / 2f, 0f)
                             lineTo(size.width, size.height)
                             lineTo(0f, size.height)
                             close()
                         }
                         canvas.drawPath(path, shadowPaint)
                     }
                     ShapeType.PENTAGON -> {
                         val path = createPolygonPath(5, size.width, size.height)
                         canvas.drawPath(path, shadowPaint)
                     }
                     ShapeType.STAR -> {
                         val path = createStarPath(5, size.width, size.height)
                         canvas.drawPath(path, shadowPaint)
                     }
                }
            }
        }
        
        // Render Selection UI
        if (isSelected) {
            Box(
                 modifier = Modifier
                     .fillMaxSize()
                     .border(2.dp, Color(0xFF007AFF)) 
                     .background(Color.Transparent)
            )
            
            // Handles (Corner Resize / Rotate)
            // Reusing logic from Sticker/Text would be ideal
            // Bottom Right Resizer
             Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .size(24.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color(0xFF007AFF), CircleShape)
            )
            
            // Delete Handle (Top Left)
            IconButton(
                onClick = { onDelete(layer.id) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-12).dp, y = (-12).dp)
                    .size(24.dp)
                    .background(Color.Red, CircleShape)
            ) {
                 Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
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
