package com.moshitech.workmate.feature.imagestudio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.geometry.Size
import kotlin.math.*
import androidx.compose.ui.unit.sp
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextAlignment
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer
import kotlinx.coroutines.invoke
import androidx.core.net.toUri
import com.moshitech.workmate.feature.imagestudio.viewmodel.AppFont

@Composable
fun TextBoxComposable(
    layer: TextLayer,
    isSelected: Boolean,
    isEditing: Boolean,
    onSelect: (String) -> Unit,
    onEdit: (String) -> Unit,
    onTransform: (String, Offset, Float, Float) -> Unit,
    onTransformEnd: (String) -> Unit, // New callback for history
    onTextChange: (String, String) -> Unit,
    onDuplicate: (String) -> Unit, 
    onDelete: (String) -> Unit,    
    modifier: Modifier = Modifier
) {
    val localDensity = LocalDensity.current
    // Apply transforms via graphicsLayer
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = layer.x
                translationY = layer.y
                scaleX = layer.scale
                scaleY = layer.scale
                rotationZ = layer.rotation
                rotationX = layer.rotationX
                rotationY = layer.rotationY
                cameraDistance = 8 * this.density
                
                // Blend Mode Application
                val composeBlendMode = when(layer.blendMode) {
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.NORMAL -> androidx.compose.ui.graphics.BlendMode.SrcOver
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.OVERLAY -> androidx.compose.ui.graphics.BlendMode.Overlay
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.SCREEN -> androidx.compose.ui.graphics.BlendMode.Screen
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.MULTIPLY -> androidx.compose.ui.graphics.BlendMode.Multiply
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.ADD -> androidx.compose.ui.graphics.BlendMode.Plus
                    com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.DIFFERENCE -> androidx.compose.ui.graphics.BlendMode.Difference
                }
                this.blendMode = composeBlendMode
                
                // Required for BlendMode to work correctly with transparency
                if (layer.blendMode != com.moshitech.workmate.feature.imagestudio.viewmodel.LayerBlendMode.NORMAL) {
                    compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                }
            }
            .then(
                if (layer.textBlur > 0f) Modifier.blur(layer.textBlur.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                else Modifier
            )
            // General Drag/Pinch/Rotate on the text body using custom detection for End event
            .pointerInput(layer.id, layer.isLocked, isEditing, layer.rotation, layer.scale) {
                if (!layer.isLocked && !isEditing) {
                    awaitEachGesture {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var rotation = 0f
                        
                        awaitFirstDown(requireUnconsumed = false)
                        var pastTouchSlop = false
                        
                        var canceled = false
                        while (!canceled) {
                            val event = awaitPointerEvent()
                            canceled = event.changes.any { change: androidx.compose.ui.input.pointer.PointerInputChange -> change.isConsumed }
                            
                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val rotationChange = event.calculateRotation()
                                val panChange = event.calculatePan()
                                
                                if (!pastTouchSlop) {
                                    zoom *= zoomChange
                                    rotation += rotationChange
                                    
                                    // Correct Pan for Rotation
                                    val rad = Math.toRadians(layer.rotation.toDouble())
                                    val cos = Math.cos(rad)
                                    val sin = Math.sin(rad)
                                    val rotX = panChange.x * cos - panChange.y * sin
                                    val rotY = panChange.x * sin + panChange.y * cos
                                    val correctedPanChange = Offset(rotX.toFloat(), rotY.toFloat()) * layer.scale
                                    
                                    pan += correctedPanChange
                                    
                                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                                    val panMotion = pan.getDistance()
                                    
                                    if (zoomMotion > viewConfiguration.touchSlop ||
                                        rotationMotion > viewConfiguration.touchSlop ||
                                        panMotion > viewConfiguration.touchSlop
                                    ) {
                                        pastTouchSlop = true
                                    }
                                }
                                
                                if (pastTouchSlop) {
                                    if (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero) {
                                        // Calculate corrected pan for immediate transform
                                        val rad = Math.toRadians(layer.rotation.toDouble())
                                        val cos = Math.cos(rad)
                                        val sin = Math.sin(rad)
                                        val rotX = panChange.x * cos - panChange.y * sin
                                        val rotY = panChange.x * sin + panChange.y * cos
                                        val correctedPanChange = Offset(rotX.toFloat(), rotY.toFloat()) * layer.scale
                                        
                                        onTransform(layer.id, correctedPanChange, zoomChange, rotationChange)
                                    }
                                    event.changes.forEach { change: androidx.compose.ui.input.pointer.PointerInputChange ->
                                        if (change.previousPosition != change.position) { 
                                            change.consume()
                                        } 
                                    }
                                }
                            }
                            
                            
                            var stillPressed = false
                            val changes: List<androidx.compose.ui.input.pointer.PointerInputChange> = event.changes
                            for (i in 0 until changes.size) {
                                if (changes[i].pressed) {
                                    stillPressed = true
                                    break
                                }
                            }
                            if (!stillPressed) break
                        }
                        
                        // Gesture Ended
                        if (pastTouchSlop) {
                            onTransformEnd(layer.id)
                        }
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
        // ... (Content remains same, using elided block to skip unchanged parts if possible, but replace tool needs context)
        // Since I need to replace the whole file content or a large chunk to be safe with indentation and context.
        // I will include the inner content but I need to be careful about not deleting the helper functions below.
        
        // Content with Reflection capability
        val textContent: @Composable (showBorder: Boolean) -> Unit = { showBorder ->
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(IntrinsicSize.Min)
                    .padding(12.dp) // Space for handles to not overlap too much
                    .drawBehind {
                        if (showBorder) {
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
                        if (layer.showBackground) Color(layer.backgroundColor)
                        else Color.Transparent,
                        RoundedCornerShape(layer.backgroundCornerRadius.dp)
                    )
                    .padding(layer.backgroundPadding.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                var textureBrush by remember(layer.textureUri) { mutableStateOf<Brush?>(null) }
                
                LaunchedEffect(layer.textureUri) {
                    if (layer.textureUri != null) {
                        try {
                            kotlinx.coroutines.Dispatchers.IO.invoke {
                                val uri = layer.textureUri.toUri()
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                val imageShader = androidx.compose.ui.graphics.ImageShader(
                                    bitmap.asImageBitmap(), 
                                    androidx.compose.ui.graphics.TileMode.Mirror, 
                                    androidx.compose.ui.graphics.TileMode.Mirror
                                )
                                textureBrush = ShaderBrush(imageShader)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            textureBrush = null
                        }
                    } else {
                        textureBrush = null
                    }
                }

                // Pre-calculate Brush
                val gradientBrush = if (layer.isGradient) {
                    createGradientBrush(
                        layer.gradientColors.map { Color(it) }, 
                        layer.gradientAngle
                    )
                } else null
                
                // Priority: Texture > Gradient
                val finalBrush = textureBrush ?: gradientBrush

                // Text Display Logic (For Non-Editing State)
                val displayText = buildAnnotatedString {
                    val isPlaceholder = layer.text.isEmpty() && !isEditing
                    
                    if (finalBrush != null && !layer.isNeon) {
                        pushStyle(SpanStyle(brush = finalBrush))
                    }
                    
                    if (isPlaceholder) {
                        // Placeholder text with white color for better visibility
                        pushStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f)))
                        append("Your Text Here")
                        pop()
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
                    
                    if (finalBrush != null && !layer.isNeon) {
                        pop()
                    }
                }

                // TextStyle (Neon & Glitch aware)
                val baseColor = if (layer.isGradient) Color.Unspecified else Color(layer.color)
                
                val useBrush = finalBrush != null
                val effectiveColor = if (layer.isNeon) Color.White 
                                     else if (useBrush && isEditing) Color.Black 
                                     else if (useBrush) Color.Unspecified 
                                     else baseColor

                val finalShadow = if (layer.isNeon) {
                    Shadow(color = Color(layer.color), blurRadius = 30f, offset = Offset.Zero)
                } else if (layer.hasShadow) {
                    Shadow(
                        color = Color(layer.shadowColor),
                        offset = Offset(layer.shadowOffsetX, layer.shadowOffsetY),
                        blurRadius = layer.shadowBlur
                    )
                } else null

                val textStyle = TextStyle(
                    color = effectiveColor, 
                    fontSize = with(localDensity) { layer.fontSize.toSp() }, // Correct: Bitmap Px -> Sp
                    fontFamily = when(layer.fontFamily) {
                        AppFont.DEFAULT -> androidx.compose.ui.text.font.FontFamily.Default
                        AppFont.SERIF -> androidx.compose.ui.text.font.FontFamily.Serif
                        AppFont.SANS_SERIF -> androidx.compose.ui.text.font.FontFamily.SansSerif
                        AppFont.MONOSPACE -> androidx.compose.ui.text.font.FontFamily.Monospace
                        AppFont.CURSIVE -> androidx.compose.ui.text.font.FontFamily.Cursive
                        AppFont.LOBSTER -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.lobster))
                        AppFont.BANGERS -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.bangers))
                        AppFont.OSWALD -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.oswald_medium))
                        AppFont.PLAYFAIR -> androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(com.moshitech.workmate.R.font.playfair_display))
                        else -> androidx.compose.ui.text.font.FontFamily.Default
                    },
                    fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.W400,
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
                    lineHeight = with(localDensity) { (layer.fontSize * layer.lineHeight).toSp() },
                    shadow = finalShadow
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
                            .fillMaxWidth()
                            .defaultMinSize(minWidth = 50.dp)
                            .focusRequester(focusRequester),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White)
                    )
                } else {
                    Box(modifier = Modifier.graphicsLayer { 
                        alpha = layer.layerOpacity 
                    }) {
                        
                        if (abs(layer.curvature) > 0f) {
                             // --- CURVED MODE ---
                             if (layer.isGlitch) {
                                 val glitchOffset = 2.dp
                                 Box(modifier = Modifier.offset(x = -glitchOffset, y = -glitchOffset)) {
                                     CurvedTextRenderer(
                                         text = displayText.text,
                                         layer = layer.copy(color = Color.Red.toArgb(), isNeon = false, layerOpacity = 0.7f),
                                         density = localDensity
                                     )
                                 }
                                 Box(modifier = Modifier.offset(x = glitchOffset, y = glitchOffset)) {
                                     CurvedTextRenderer(
                                         text = displayText.text,
                                         layer = layer.copy(color = Color.Cyan.toArgb(), isNeon = false, layerOpacity = 0.7f),
                                         density = localDensity
                                     )
                                 }
                             }

                             Text(
                                text = displayText,
                                style = textStyle.copy(color = Color.Transparent, shadow = null),
                                modifier = Modifier.defaultMinSize(minWidth = 50.dp).alpha(0f)
                             )
                             
                             CurvedTextRenderer(
                                 text = displayText.text,
                                 layer = layer,
                                 density = localDensity
                             )
                        } else {
                            // --- STRAIGHT MODE ---
                            if (layer.isGlitch) {
                                 val glitchOffset = 2.dp
                                 Text(
                                     text = displayText,
                                     style = textStyle.copy(color = Color.Red.copy(alpha = 0.7f), shadow = null),
                                     modifier = Modifier.offset(x = -glitchOffset, y = -glitchOffset)
                                 )
                                 Text(
                                     text = displayText,
                                     style = textStyle.copy(color = Color.Cyan.copy(alpha = 0.7f), shadow = null),
                                     modifier = Modifier.offset(x = glitchOffset, y = glitchOffset)
                                 )
                            }
                            
                            if (layer.outlineWidth > 0f) {
                                Text(
                                    text = displayText,
                                    style = textStyle.copy(
                                        color = Color(layer.outlineColor),
                                        drawStyle = Stroke(
                                            width = layer.outlineWidth, // Raw Pixels
                                            join = StrokeJoin.Round
                                        )
                                    ),
                                    modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                                )
                                Text(
                                    text = displayText,
                                    style = textStyle.copy(shadow = null),
                                    modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                                )
                            } else {
                                Text(
                                    text = displayText,
                                    style = textStyle,
                                    modifier = Modifier.defaultMinSize(minWidth = 50.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Reflection Render (Behind)
        if (!isEditing && layer.reflectionOpacity > 0f) {
            Box(
                modifier = Modifier
                     .graphicsLayer {
                         rotationX = 180f // Flip vertically
                         translationY = size.height - 22.dp.toPx() + layer.reflectionOffset.dp.toPx()
                         alpha = layer.reflectionOpacity
                         cameraDistance = 8 * density 
                     }
            ) {
                textContent(false)
            }
        }

        // Main Render
        textContent(isSelected)

        // Overlay Handles
        if (isSelected && !layer.isLocked) {
            val handleSize = 24.dp
            val handleOffset = 12.dp 

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
                        detectDragGestures(
                             onDragEnd = { onTransformEnd(layer.id) }
                        ) { change, dragAmount ->
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
                        detectDragGestures(
                            onDragEnd = { onTransformEnd(layer.id) }
                        ) { change, dragAmount ->
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

fun createGradientBrush(colors: List<Color>, angle: Float): Brush {
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val center = Offset(size.width / 2, size.height / 2)
            val angleRad = (angle * PI / 180).toFloat()
            val r = sqrt(size.width.pow(2) + size.height.pow(2)) / 2
            val start = Offset(
                center.x - r * cos(angleRad),
                center.y - r * sin(angleRad)
            )
            val end = Offset(
                center.x + r * cos(angleRad),
                center.y + r * sin(angleRad)
            )
            return LinearGradientShader(
                from = start,
                to = end,
                colors = colors,
                tileMode = androidx.compose.ui.graphics.TileMode.Clamp
            )
        }
    }
}

@Composable
fun CurvedTextRenderer(
    text: String,
    layer: TextLayer,
    density: androidx.compose.ui.unit.Density
) {
    if (text.isEmpty()) return

    val paint = remember { Paint() }
    val path = remember { Path() }
    val strokePaint = remember { Paint() } // For outline

    Canvas(modifier = Modifier.fillMaxSize()) {
        val fontSizePx = with(density) { layer.fontSize.sp.toPx() }
        
        // 1. Configure Main Paint
        paint.reset()
        paint.textSize = fontSizePx
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.LEFT // Use Layout alignment logic (Rule 6)
        
        // Native Paint LetterSpacing
        if (layer.letterSpacing != 0f) {
           paint.letterSpacing = layer.letterSpacing / layer.fontSize
        }

        // Typeface
        var style = Typeface.NORMAL
        if (layer.isBold && layer.isItalic) style = Typeface.BOLD_ITALIC
        else if (layer.isBold) style = Typeface.BOLD
        else if (layer.isItalic) style = Typeface.ITALIC
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, style)
        paint.isUnderlineText = layer.isUnderline
        paint.isStrikeThruText = layer.isStrikethrough
        
        // Shadow
        if (layer.hasShadow) {
            paint.setShadowLayer(
                layer.shadowBlur,
                layer.shadowOffsetX,
                layer.shadowOffsetY,
                layer.shadowColor
            )
        }

        // Color / Gradient
        if (layer.isGradient) {
             val shader = android.graphics.LinearGradient(
                 0f, 0f, size.width, size.height, 
                 layer.gradientColors.toIntArray(),
                 null,
                 android.graphics.Shader.TileMode.CLAMP
             )
             paint.shader = shader
        } else {
             paint.color = layer.color
             paint.shader = null
        }
        
        // 2. Configure Outline Paint
        val hasOutline = layer.outlineWidth > 0f
        if (hasOutline) {
            strokePaint.set(paint)
            strokePaint.style = Paint.Style.STROKE
            strokePaint.strokeWidth = with(density) { layer.outlineWidth.dp.toPx() }
            strokePaint.color = layer.outlineColor
            strokePaint.shader = null
            strokePaint.setShadowLayer(0f, 0f, 0f, 0)
            strokePaint.strokeJoin = Paint.Join.ROUND
        }

        // Rule 1: Measure text first (always)
        val textWidth = paint.measureText(text)
        val fm = paint.fontMetrics
        
        // Rule 2: Clamp curvature
        val angleDeg = layer.curvature.coerceIn(-180f, 180f)

        // Fallback for straight text (Small curvature)
        if (abs(angleDeg) < 5f) {
             val centerY = size.height / 2
             
             // Simple centering for straight text
             val xPos = when(layer.alignment) {
                 TextAlignment.LEFT -> 0f
                 TextAlignment.CENTER -> (size.width - textWidth) / 2f
                 TextAlignment.RIGHT -> size.width - textWidth
                 else -> (size.width - textWidth) / 2f
             }
             
             // Vertically center based on metrics
             val vOffsetCenter = centerY - (fm.ascent + fm.descent) / 2f
             
             drawIntoCanvas { canvas ->
                 if (hasOutline) canvas.nativeCanvas.drawText(text, xPos, vOffsetCenter, strokePaint)
                 canvas.nativeCanvas.drawText(text, xPos, vOffsetCenter, paint)
             }
             
        } else {
             // Rule 3: Use stable radius formula
             val angleRad = Math.toRadians(abs(angleDeg).toDouble())
             val radius = (textWidth / angleRad).toFloat()
             
             val centerX = size.width / 2
             val centerY = size.height / 2
             
             // Dynamic Oval Centering (Fix for "Flying Text" bug)
             // We shift the oval center so the Arc Vertex touches the View Center (centerY)
             
             val oval = android.graphics.RectF()
             
             val startAngle: Float
             
             if (angleDeg > 0) {
                 // Arch (Curve UP). Vertex is at Top (-90).
                 // We want Top of Oval to be at centerY.
                 // Oval Center = centerY + radius
                 val circleCenterY = centerY + radius
                 oval.set(
                     centerX - radius,
                     circleCenterY - radius,
                     centerX + radius,
                     circleCenterY + radius
                 )
                 startAngle = -90f - angleDeg / 2
             } else {
                 // Smile (Curve DOWN). Vertex is at Bottom (90).
                 // We want Bottom of Oval to be at centerY.
                 // Oval Center = centerY - radius
                 val circleCenterY = centerY - radius
                 oval.set(
                     centerX - radius,
                     circleCenterY - radius,
                     centerX + radius,
                     circleCenterY + radius
                 )
                 startAngle = 90f - abs(angleDeg) / 2
             }
                 
             path.reset()
             path.addArc(oval, startAngle, angleDeg)
             
             // Rule 6: Correct horizontal alignment
             val pathLength = (2 * PI * radius * (abs(angleDeg) / 360f)).toFloat()
             
             val hOffset = when (layer.alignment) {
                 TextAlignment.LEFT -> 0f
                 TextAlignment.CENTER -> (pathLength - textWidth) / 2f
                 TextAlignment.RIGHT -> pathLength - textWidth
                 else -> (pathLength - textWidth) / 2f
             }
             
             // Rule 7: Proper vertical offset using font metrics
             // Center the text vertically on the path
             val vOffset = -(fm.ascent + fm.descent) / 2f
             
             // Rule 8: Draw order
             drawIntoCanvas { canvas ->
                 if (hasOutline) {
                     canvas.nativeCanvas.drawTextOnPath(text, path, hOffset, vOffset, strokePaint)
                 }
                 canvas.nativeCanvas.drawTextOnPath(text, path, hOffset, vOffset, paint)
             }
        }
    }
}

// Helper extension for gesture calculation
fun androidx.compose.ui.input.pointer.PointerEvent.calculateCentroidSize(useCurrent: Boolean = true): Float {
    val positions = changes.map { if (useCurrent) it.position else it.previousPosition }
    if (positions.isEmpty()) return 0f
    
    // Calculate Centroid
    var centroidX = 0f
    var centroidY = 0f
    positions.forEach {
        centroidX += it.x
        centroidY += it.y
    }
    centroidX /= positions.size
    centroidY /= positions.size
    
    // Calculate average distance from centroid
    var sumDistance = 0f
    positions.forEach {
        val dx = it.x - centroidX
        val dy = it.y - centroidY
        sumDistance += sqrt(dx*dx + dy*dy)
    }
    return sumDistance / positions.size
}

fun androidx.compose.ui.input.pointer.PointerEvent.calculateZoom(): Float {
    val currentCentroidSize = calculateCentroidSize(useCurrent = true)
    val previousCentroidSize = calculateCentroidSize(useCurrent = false)
    if (previousCentroidSize == 0f) return 1f
    return currentCentroidSize / previousCentroidSize
}

fun androidx.compose.ui.input.pointer.PointerEvent.calculateRotation(): Float {
    if (changes.size < 2) return 0f
    val current = changes.map { it.position }
    val previous = changes.map { it.previousPosition }
    
    val currentAngle = atan2(current[1].y - current[0].y, current[1].x - current[0].x)
    val previousAngle = atan2(previous[1].y - previous[0].y, previous[1].x - previous[0].x)
    
    return (currentAngle - previousAngle) * 180f / PI.toFloat()
}

fun androidx.compose.ui.input.pointer.PointerEvent.calculatePan(): androidx.compose.ui.geometry.Offset {
    val current = changes.map { it.position }
    val previous = changes.map { it.previousPosition }
    
    val currentCentroid = current.fold(androidx.compose.ui.geometry.Offset.Zero) { acc, offset -> acc + offset } / current.size.toFloat()
    val previousCentroid = previous.fold(androidx.compose.ui.geometry.Offset.Zero) { acc, offset -> acc + offset } / previous.size.toFloat()
    
    return currentCentroid - previousCentroid
}
