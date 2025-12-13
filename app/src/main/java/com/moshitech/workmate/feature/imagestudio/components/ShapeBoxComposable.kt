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
            // Use graphicsLayer for translation (pixels) to match other layers
            .graphicsLayer {
                translationX = layer.x
                translationY = layer.y
                scaleX = layer.scale
                scaleY = layer.scale
                rotationZ = layer.rotation
            }
            .size(layer.width.dp, layer.height.dp) // Size is intrinsic to the box
            // Center the pivot? GraphicsLayer default is Center.
            .pointerInput(layer.id, isSelected) {
                detectTapGestures {
                    onSelect(layer.id)
                }
            }
            .pointerInput(layer.id) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    onTransform(layer.id, pan, zoom, rotation)
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
                         val arrowSize = layer.strokeWidth * 3f
                         val arrowPath = Path().apply {
                            moveTo(end.x, end.y)
                            lineTo(end.x - arrowSize, end.y - arrowSize)
                            lineTo(end.x - arrowSize, end.y + arrowSize)
                            close()
                         }
                         // Fill the arrow head regardless? Or stroke it?
                         // Usually arrows head matches body style. If dashed line, arrow head might be weird.
                         // Let's solid fill arrow head for better look or use same paint
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
