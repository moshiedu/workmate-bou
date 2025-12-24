package com.moshitech.workmate.feature.imagestudio.util

import android.content.Context
import android.graphics.*
import com.moshitech.workmate.feature.imagestudio.viewmodel.*

/**
 * Composite Renderer - Renders all layers onto base image
 * This is the core component that enables proper crop+layer interaction
 */
class CompositeRenderer(private val context: Context) {
    
    /**
     * Renders all layers onto base image with optional crop
     * @param baseImage The original/adjusted image
     * @param state Current editor state with all layers
     * @param cropRect Optional crop rectangle to apply
     * @return Composite bitmap with all layers rendered
     */
    fun renderComposite(
        baseImage: Bitmap,
        state: EditorState,
        cropRect: Rect? = null,
        bitmapScale: Float = 1f // New Parameter
    ): Bitmap {
        // 1. Apply adjustments to base image
        val adjustedImage = applyAdjustments(baseImage, state)
        
        // 2. Create mutable copy for composite
        val composite = adjustedImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(composite)
        
        // 3. Render all layers in z-order (back to front)
        renderAllLayers(canvas, state, bitmapScale)
        
        // 4. Apply Transforms (Rotate/Flip)
        // This must happen AFTER rendering layers but BEFORE cropping
        // so that layers rotate WITH the image.
        val transformedImage = applyTransforms(composite, state)
        
        // 5. Apply crop if specified
        // Note: cropRect is usually relative to the transformed image view
        return if (cropRect != null && isValidCropRect(cropRect, transformedImage)) {
            Bitmap.createBitmap(
                transformedImage,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
            )
        } else {
            transformedImage
        }
    }
    
    private fun renderAllLayers(canvas: Canvas, state: EditorState, bitmapScale: Float) {
        // Get all layers and sort by zIndex (lowest first = back to front)
        val allLayers = mutableListOf<Pair<Int, () -> Unit>>()
        
        // Add text layers
        state.textLayers.forEach { layer ->
            if (layer.isVisible) {
                allLayers.add(layer.zIndex to { renderTextLayer(canvas, layer) })
            }
        }
        
        // Add shape layers
        state.shapeLayers.forEach { layer ->
            if (layer.isVisible) {
                allLayers.add(layer.zIndex to { renderShapeLayer(canvas, layer) })
            }
        }
        
        // Add sticker layers
        state.stickerLayers.forEach { layer ->
            if (layer.isVisible) {
                allLayers.add(layer.zIndex to { renderStickerLayer(canvas, layer, bitmapScale) })
            }
        }
        
        // Add draw actions
        state.drawActions.forEach { action ->
            allLayers.add(0 to { renderDrawAction(canvas, action) })
        }
        
        // Sort by zIndex and render
        allLayers.sortedBy { it.first }.forEach { (_, render) ->
            render()
        }
    }
    
    private fun renderTextLayer(canvas: Canvas, layer: TextLayer) {
        canvas.save()
        
        // Apply blur if enabled (must be done before other transforms)
        if (layer.textBlur > 0f) {
            // Note: Canvas blur requires API 31+, so we'll use a workaround with MaskFilter
            val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                maskFilter = BlurMaskFilter(layer.textBlur, BlurMaskFilter.Blur.NORMAL)
            }
        }
        
        // Create text paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize
            typeface = getTypeface(layer.fontFamily, layer.isBold, layer.isItalic)
            textAlign = Paint.Align.LEFT  // Always use LEFT for consistent measurement
            
            // Apply color or gradient
            if (layer.isGradient && layer.gradientColors.size >= 2) {
                shader = LinearGradient(
                    0f, 0f,
                    layer.width, 0f,
                    layer.gradientColors.toIntArray(),
                    null,
                    Shader.TileMode.CLAMP
                )
            } else {
                color = layer.color
            }
            
            alpha = (layer.layerOpacity * 255).toInt()
            
            // Apply shadow
            if (layer.hasShadow) {
                setShadowLayer(
                    layer.shadowBlur,
                    layer.shadowOffsetX,
                    layer.shadowOffsetY,
                    layer.shadowColor
                )
            }
            
            // Apply neon effect (glow)
            if (layer.isNeon) {
                setShadowLayer(
                    30f,  // Large blur for glow effect
                    0f, 0f,  // No offset
                    layer.color  // Glow color matches text color
                )
            }
            
            // Apply text blur
            if (layer.textBlur > 0f) {
                maskFilter = BlurMaskFilter(layer.textBlur, BlurMaskFilter.Blur.NORMAL)
            }
        }
        
        // Measure text - this is the CONTENT size (what Compose's Box wraps)
        val fm = paint.fontMetrics
        val textWidth = paint.measureText(layer.text)
        val textHeight = fm.descent - fm.ascent
        
        // Padding (convert dp to px)
        val density = context.resources.displayMetrics.density
        val paddingPx = layer.backgroundPadding * density
        val radiusPx = layer.backgroundCornerRadius * density
        val handlePaddingPx = 12 * density
        
        // CRITICAL FIX 1: Match UI's defaultMinSize(minWidth = 50.dp)
        val minWidthPx = 50f * density
        val contentWidth = kotlin.math.max(textWidth, minWidthPx)
        val contentHeight = textHeight
        
        // CRITICAL FIX 2: Strict Container Simulation
        // UI Structure: OuterBox -> Padding(12dp) -> Background -> Padding(bgPadding) -> Text
        // Rotation/Scale applies to OuterBox. Pivot is Center of OuterBox.
        val outerWidth = contentWidth + (2 * paddingPx) + (2 * handlePaddingPx)
        val outerHeight = contentHeight + (2 * paddingPx) + (2 * handlePaddingPx)
        
        val pivotX = outerWidth / 2f
        val pivotY = outerHeight / 2f
        
        // Step 1: Translate to Top-Left of Outer Box (layer.x, layer.y)
        canvas.translate(layer.x, layer.y)
        
        // Step 2: Rotate/Scale around Pivot (Center of Outer Box)
        canvas.translate(pivotX, pivotY)
        canvas.rotate(layer.rotation)
        canvas.scale(layer.scale, layer.scale)
        canvas.translate(-pivotX, -pivotY)
        
        // Apply 3D rotations if present
        if (layer.rotationX != 0f || layer.rotationY != 0f) {
            canvas.save() // Save before skew
            canvas.translate(pivotX, pivotY)
            val skewX = layer.rotationY / 100f
            val skewY = layer.rotationX / 100f
            canvas.skew(skewX, skewY)
            canvas.translate(-pivotX, -pivotY)
        }

        // Step 3: Draw Content relative to Outer Box Top-Left
        // Text Content Origin (Visual Top-Left of text box excluding handles)
        // X = handlePaddingPx
        // Y = handlePaddingPx
        // Inside that is Background. Inside that is bgPadding.
        
        // Background Rect Logic
        // Drawn relative to Outer Box Origin (0,0)
        // Rect starts at handlePaddingPx
        if (layer.showBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.backgroundColor
                style = Paint.Style.FILL
                alpha = (layer.backgroundOpacity * 255).toInt()
            }
            
            val bgRect = RectF(
                handlePaddingPx,                                    // Left
                handlePaddingPx,                                    // Top
                handlePaddingPx + contentWidth + (2 * paddingPx),   // Right
                handlePaddingPx + contentHeight + (2 * paddingPx)   // Bottom
            )
            
            canvas.drawRoundRect(bgRect, radiusPx, radiusPx, bgPaint)
        }
        
        // Text Logic
        // Text Origin X = handlePaddingPx + paddingPx + AlignmentOffset
        // Text Origin Y = handlePaddingPx + paddingPx + Baseline
        
        val alignOffset = when (layer.alignment) {
            TextAlignment.LEFT, TextAlignment.JUSTIFY -> 0f
            TextAlignment.CENTER -> (contentWidth - textWidth) / 2f
            TextAlignment.RIGHT -> contentWidth - textWidth
        }
        
        val textOriginX = handlePaddingPx + paddingPx + alignOffset
        val textOriginY = handlePaddingPx + paddingPx + (-fm.ascent) // Baseline
        
        // Draw text at calculated origin
        val alignOffsetPlaceholder = 0f // Already calculated into textOriginX

        
        // Draw glitch effect (RGB offset)
        if (layer.isGlitch) {
            val glitchOffset = 4f
            
            // Red channel offset
            val redPaint = Paint(paint).apply {
                color = android.graphics.Color.RED
                alpha = (0.7f * layer.layerOpacity * 255).toInt()
                clearShadowLayer()
            }
            canvas.drawText(layer.text, textOriginX - glitchOffset, textOriginY - glitchOffset, redPaint)
            
            // Cyan channel offset
            val cyanPaint = Paint(paint).apply {
                color = android.graphics.Color.CYAN
                alpha = (0.7f * layer.layerOpacity * 255).toInt()
                clearShadowLayer()
            }
            canvas.drawText(layer.text, textOriginX + glitchOffset, textOriginY + glitchOffset, cyanPaint)
        }
        
        // Apply outline if enabled
        if (layer.hasOutline && layer.outlineWidth > 0) {
            val outlinePaint = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.outlineWidth
                color = layer.outlineColor
                clearShadowLayer()  // No shadow on outline
            }
            canvas.drawText(layer.text, textOriginX, textOriginY, outlinePaint)
        }
        
        // Draw main text
        canvas.drawText(layer.text, textOriginX, textOriginY, paint)
        
        // Draw reflection if enabled
        if (layer.reflectionOpacity > 0f) {
            canvas.save()
            
            // Calculate Text Bottom (absolute Y in local space)
            // Top = handlePaddingPx + paddingPx
            // Bottom = Top + textHeight
            val textBottomY = handlePaddingPx + paddingPx + textHeight
            
            // Flip VERTICALLY around the Text Bottom axis
            canvas.translate(0f, textBottomY)
            canvas.scale(1f, -1f)
            canvas.translate(0f, -textBottomY)
            
            // Apply offset (move reflection further down)
            // Since we stuck to positive Y going down, and we flipped the CONTENT relative to Bottom,
            // The reflected "Top" is now at Bottom.
            // We want to shift it down by offset.
            // Since Y is flipped? No, we flipped the canvas matrix only?
            // Actually, simply translating Y by +offset works in the standard coordinate space IF we apply it AFTER flip?
            // Let's just translate "downwards" in the flipped space.
            canvas.translate(0f, layer.reflectionOffset)
            
            val reflectionPaint = Paint(paint).apply {
                alpha = (layer.reflectionOpacity * layer.layerOpacity * 255).toInt()
                // Create gradient for fade effect
                shader = LinearGradient(
                    0f, textOriginY,
                    0f, textOriginY - textHeight, // Fading out towards the "bottom" of the reflection (which is visually top)
                    intArrayOf(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.WHITE
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            
            canvas.drawText(layer.text, textOriginX, textOriginY, reflectionPaint)
            canvas.restore()
        }
        
        canvas.restore()
    }
    
    private fun renderShapeLayer(canvas: Canvas, layer: ShapeLayer) {
        canvas.save()
        
        // Shape size
        val w = layer.width
        val h = layer.height
        
        // Match Compose graphicsLayer: translate to top-left, offset to center, rotate/scale
        canvas.translate(layer.x, layer.y)  // Top-left corner
        canvas.translate(w / 2f, h / 2f)  // Offset to center
        canvas.rotate(layer.rotation)  // Rotate around center
        canvas.scale(layer.scale, layer.scale)  // Scale around center
        canvas.translate(-w / 2f, -h / 2f)  // Back to top-left for drawing
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.color
            strokeWidth = layer.strokeWidth
            style = if (layer.isFilled) Paint.Style.FILL else Paint.Style.STROKE
            
            // Apply opacity
            alpha = (layer.opacity * 255).toInt()
            
            // Apply stroke style
            pathEffect = when (layer.strokeStyle) {
                StrokeStyle.DASHED -> DashPathEffect(floatArrayOf(30f, 15f), 0f)
                StrokeStyle.DOTTED -> DashPathEffect(floatArrayOf(5f, 10f), 0f)
                StrokeStyle.LONG_DASH -> DashPathEffect(floatArrayOf(50f, 20f), 0f)
                StrokeStyle.DASH_DOT -> DashPathEffect(floatArrayOf(30f, 15f, 5f, 15f), 0f)
                StrokeStyle.SOLID -> null
            }
            
            // Apply shadow
            if (layer.hasShadow) {
                setShadowLayer(
                    layer.shadowBlur,
                    layer.shadowX,
                    layer.shadowY,
                    layer.shadowColor
                )
            }
        }
        
        // Render based on shape type
        when (layer.type) {
            ShapeType.RECTANGLE -> {
                val rect = RectF(0f, 0f, w, h)
                canvas.drawRect(rect, paint)
            }
            ShapeType.CIRCLE -> {
                val radius = minOf(w, h) / 2
                canvas.drawCircle(w/2f, h/2f, radius, paint)
            }
            ShapeType.LINE -> {
                canvas.drawLine(0f, h/2f, w, h/2f, paint)
            }
            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(w / 2f, 0f)      // Top center
                    lineTo(w, h)            // Bottom right
                    lineTo(0f, h)           // Bottom left
                    close()
                }
                canvas.drawPath(path, paint)
            }
            ShapeType.ARROW -> {
                val path = Path().apply {
                    // Simple right-pointing arrow usually
                    // But here we draw upright? Or matches icon?
                    // Let's draw an Up Arrow since w/h are bounding box
                    // Arrow Head
                    moveTo(w / 2f, 0f)
                    lineTo(w, h * 0.5f)
                    lineTo(w * 0.7f, h * 0.5f)
                    // Stem
                    lineTo(w * 0.7f, h)
                    lineTo(w * 0.3f, h)
                    lineTo(w * 0.3f, h * 0.5f)
                    lineTo(0f, h * 0.5f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            ShapeType.STAR -> {
                val path = Path()
                val centerX = w / 2f
                val centerY = h / 2f
                val outerRadius = minOf(w, h) / 2f
                val innerRadius = outerRadius * 0.382f // Standard 5-point star ratio
                
                for (i in 0 until 10) {
                    val angle = Math.PI / 5 * i - Math.PI / 2 // Start at top -90deg
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val x = centerX + Math.cos(angle).toFloat() * r
                    val y = centerY + Math.sin(angle).toFloat() * r
                    
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            ShapeType.PENTAGON -> {
                val path = Path()
                val centerX = w / 2f
                val centerY = h / 2f
                val radius = minOf(w, h) / 2f
                
                for (i in 0 until 5) {
                    val angle = Math.PI * 2 / 5 * i - Math.PI / 2 // Start top
                    val x = centerX + Math.cos(angle).toFloat() * radius
                    val y = centerY + Math.sin(angle).toFloat() * radius
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
        }
        
        canvas.restore()
    }
    
    private fun renderStickerLayer(canvas: Canvas, layer: StickerLayer, bitmapScale: Float) {
        canvas.save()
        
        // Load sticker content first to determine size
        var bitmap: Bitmap? = null
        var textToDraw: String? = null
        var textSize = 100f
        
        try {
            if (layer.resId != 0) {
                bitmap = BitmapFactory.decodeResource(context.resources, layer.resId)
            } else if (layer.text != null && layer.text.isNotEmpty()) {
                textToDraw = layer.text
            } else if (layer.uri != null) {
                val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(layer.uri))
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            }
        } catch (e: Exception) { e.printStackTrace() }
        
        // Determine content bounds
        val contentWidth: Float
        val contentHeight: Float
        
        if (bitmap != null) {
            contentWidth = bitmap.width.toFloat()
            contentHeight = bitmap.height.toFloat()
        } else if (textToDraw != null) {
            val p = Paint().apply { this.textSize = 100f }
            val b = Rect()
            p.getTextBounds(textToDraw, 0, textToDraw.length, b)
            contentWidth = b.width().toFloat()
            contentHeight = b.height().toFloat()
            textSize = 100f
        } else {
            contentWidth = 100f
            contentHeight = 100f
        }
        
        // Match Compose UI Standard: Stickers are initialized at 100.dp
        val density = context.resources.displayMetrics.density
        // Revert: DO NOT divide by bitmapScale. 
        // UI uses Modifier.size(100.dp) but SCALES it by bitScale in graphicsLayer.
        // So effective size on bitmap is just 100dp.
        val baseStickerSizePx = 100f * density
        
        // Calculate normalization scale (to make the sticker 100dp visually by default)
        // effectively solving the "Huge Bitmap" issue
        // We use the larger dimension to fit within the 100dp box (mimicking ContentScale.Fit)
        // For Text (Emoji), UI uses 64.sp which is approx 64% of the 100dp box.
        val targetSize = if (textToDraw != null) baseStickerSizePx * 0.64f else baseStickerSizePx
        
        val maxContentDim = kotlin.math.max(contentWidth, contentHeight)
        val normalizationScale = if (maxContentDim > 0) targetSize / maxContentDim else 1f
        
        val normalizedWidth = contentWidth * normalizationScale
        val normalizedHeight = contentHeight * normalizationScale
        
        // Match Compose graphicsLayer: translate to top-left, offset to center, rotate/scale
        canvas.translate(layer.x, layer.y)  // Top-left corner
        
        // CRITICAL FIX: Rotate around the CENTER of the 100dp CONTAINER (baseStickerSizePx)
        // The UI pivot is at 50dp (Screen Px).
        // To match this on Bitmap, we must convert 50dp -> Bitmap Px.
        // Formula: baseStickerSizePx / 2
        val boxCenter = baseStickerSizePx / 2f
        canvas.translate(boxCenter, boxCenter)  // Offset to Container Center
        canvas.rotate(layer.rotation)  // Rotate around Container Center
        
        // Apply Scales (Layer Scale + Normalization Scale)
        val sx = (if (layer.isFlipped) -layer.scale else layer.scale) * normalizationScale
        val sy = layer.scale * normalizationScale
        
        // We scale from the "raw" 0,0 center. 
        // Since we translated to center, scaling applies to the drawing axes.
        canvas.scale(sx, sy)
        
        // Translate back to top-left of the content relative to the center
        // Since we want content CENTERED in the container:
        // Draw Origin should be -contentWidth/2, -contentHeight/2
        canvas.translate(-contentWidth / 2f, -contentHeight / 2f)
        
        // Create paint with opacity, shadow, and tint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Apply opacity
            alpha = (layer.opacity * 255).toInt()
            
            // Apply shadow
            if (layer.hasShadow) {
                setShadowLayer(
                    layer.shadowBlur,
                    layer.shadowOffsetX,
                    layer.shadowOffsetY,
                    layer.shadowColor
                )
            }
            
            // Apply tint using ColorFilter
            if (layer.hasTint) {
                colorFilter = android.graphics.PorterDuffColorFilter(layer.tintColor, PorterDuff.Mode.SRC_ATOP)
            }
        }
        
        // Draw content centered (offset by -half)
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, -contentWidth / 2f, -contentHeight / 2f, paint)
        } else if (textToDraw != null) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                this.textAlign = Paint.Align.CENTER
                alpha = (layer.opacity * 255).toInt()
                
                if (layer.hasShadow) {
                    setShadowLayer(
                        layer.shadowBlur,
                        layer.shadowOffsetX,
                        layer.shadowOffsetY,
                        layer.shadowColor
                    )
                }
            }
            // Draw text centered at 0,0
            val fm = textPaint.fontMetrics
            val yOffset = -(fm.descent + fm.ascent) / 2f
            canvas.drawText(textToDraw, 0f, yOffset, textPaint)
        }
        
        // Draw border if enabled
        if (layer.hasBorder) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.borderWidth
                color = layer.borderColor
                alpha = (layer.opacity * 255).toInt()
            }
            
            // Draw border around content bounds
            val borderRect = RectF(
                -contentWidth / 2f,
                -contentHeight / 2f,
                contentWidth / 2f,
                contentHeight / 2f
            )
            canvas.drawRect(borderRect, borderPaint)
        }
        
        canvas.restore()
    }
    
    
    private fun renderDrawAction(canvas: Canvas, action: DrawAction) {
        when (action) {
            is DrawAction.Path -> {
                val isBlur = action.path.blurRadius > 0f
                val isSpray = action.path.isSpray
                val isNeon = action.path.isNeon
                val isHighlighter = action.path.isHighlighter
                
                val path = Path().apply {
                    if (action.path.points.isNotEmpty()) {
                        moveTo(action.path.points.first().x, action.path.points.first().y)
                        action.path.points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }
                }

                if (isBlur) {
                    val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = action.path.color
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    val dynamicBlurRadius = action.path.strokeWidth * 0.8f
                    
                    // Layer 1: Wide glow
                    blurPaint.strokeWidth = action.path.strokeWidth + (dynamicBlurRadius * 3f)
                    blurPaint.alpha = (Color.alpha(action.path.color) * 0.15f).toInt()
                    canvas.drawPath(path, blurPaint)
                    
                    // Layer 2: Medium glow
                    blurPaint.strokeWidth = action.path.strokeWidth + (dynamicBlurRadius * 1.5f)
                    blurPaint.alpha = (Color.alpha(action.path.color) * 0.3f).toInt()
                    canvas.drawPath(path, blurPaint)
                    
                    // Layer 3: Core
                    blurPaint.strokeWidth = action.path.strokeWidth
                    blurPaint.alpha = (Color.alpha(action.path.color) * 0.5f).toInt()
                    canvas.drawPath(path, blurPaint)
                    
                } else if (isSpray) {
                    val sprayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = action.path.color
                        strokeWidth = action.path.strokeWidth
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    val sprayJitter = action.path.strokeWidth * 0.5f
                    
                    // Use ComposePathEffect to simulate spray/scatter
                    sprayPaint.pathEffect = ComposePathEffect(
                        DiscretePathEffect(sprayJitter / 2f, sprayJitter),
                        DashPathEffect(floatArrayOf(action.path.strokeWidth * 0.1f, action.path.strokeWidth * 1.5f), 0f)
                    )
                    canvas.drawPath(path, sprayPaint)
                    
                } else {
                    // Standard tools (Brush, Neon, Highlighter, Eraser)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = action.path.color
                        strokeWidth = action.path.strokeWidth
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        
                        // Apply Neon Glow
                        if (isNeon) {
                           maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
                        }
                        
                        // Apply blend mode
                        when (action.path.blendMode) {
                            DrawBlendMode.MULTIPLY -> xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                            DrawBlendMode.SCREEN -> xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                            DrawBlendMode.OVERLAY -> xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                            DrawBlendMode.ADD -> xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
                            else -> {}
                        }
                    }
                    
                    canvas.drawPath(path, paint)
                    
                    // Render Neon Core
                    if (isNeon) {
                         paint.maskFilter = null
                         paint.color = Color.WHITE
                         paint.strokeWidth = action.path.strokeWidth / 3f
                         canvas.drawPath(path, paint)
                    }
                }
            }
            is DrawAction.Shape -> {
                // Shape drawing handled separately
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = action.shape.color
                    strokeWidth = action.shape.strokeWidth
                    style = Paint.Style.STROKE
                }
                // Draw shape based on type
                when (action.shape) {
                    is Shape.Line -> {
                        canvas.drawLine(
                            action.shape.start.x, action.shape.start.y,
                            action.shape.end.x, action.shape.end.y,
                            paint
                        )
                    }
                    is Shape.Rectangle -> {
                        val rect = RectF(
                            action.shape.topLeft.x, 
                            action.shape.topLeft.y,
                            action.shape.topLeft.x + action.shape.size.width, 
                            action.shape.topLeft.y + action.shape.size.height
                        )
                        canvas.drawRect(rect, paint)
                    }
                    is Shape.Circle -> {
                        canvas.drawCircle(
                            action.shape.center.x, action.shape.center.y,
                            action.shape.radius,
                            paint
                        )
                    }
                }
            }
        }
    }
    
    private fun applyAdjustments(bitmap: Bitmap, state: EditorState): Bitmap {
        // If no adjustments, return original
        if (state.brightness == 0f && 
            state.contrast == 1f && 
            state.saturation == 1f && 
            state.hue == 0f) {
            return bitmap
        }
        
        // Create mutable copy
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        // Apply color matrix adjustments
        val colorMatrix = ColorMatrix().apply {
            // Brightness
            val brightnessMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, state.brightness * 255,
                0f, 1f, 0f, 0f, state.brightness * 255,
                0f, 0f, 1f, 0f, state.brightness * 255,
                0f, 0f, 0f, 1f, 0f
            ))
            postConcat(brightnessMatrix)
            
            // Saturation
            setSaturation(state.saturation)
        }
        
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        val canvas = Canvas(result)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return result
    }
    
    private fun getTypeface(fontFamily: AppFont, isBold: Boolean, isItalic: Boolean): Typeface {
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        
        return when (fontFamily) {
            AppFont.SERIF -> Typeface.create(Typeface.SERIF, style)
            AppFont.SANS_SERIF -> Typeface.create(Typeface.SANS_SERIF, style)
            AppFont.MONOSPACE -> Typeface.create(Typeface.MONOSPACE, style)
            else -> Typeface.create(Typeface.DEFAULT, style)
        }
    }
    
    private fun isValidCropRect(rect: Rect, bitmap: Bitmap): Boolean {
        return rect.left >= 0 && 
               rect.top >= 0 && 
               rect.right <= bitmap.width && 
               rect.bottom <= bitmap.height &&
               rect.width() > 0 &&
               rect.height() > 0
    }

    private fun applyTransforms(bitmap: Bitmap, state: EditorState): Bitmap {
        // If no transforms, return original
        if (state.rotationAngle == 0f && !state.flipX && !state.flipY) {
            return bitmap
        }

        val matrix = Matrix()
        
        // 1. Apply Rotation
        if (state.rotationAngle != 0f) {
            matrix.postRotate(state.rotationAngle)
        }
        
        // 2. Apply Flip
        if (state.flipX || state.flipY) {
            val sx = if (state.flipX) -1f else 1f
            val sy = if (state.flipY) -1f else 1f
            matrix.postScale(sx, sy)
        }

        return try {
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap // Fallback to original if transform fails (e.g. OOM)
        }
    }
}
