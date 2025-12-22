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
        cropRect: Rect? = null
    ): Bitmap {
        // 1. Apply adjustments to base image
        val adjustedImage = applyAdjustments(baseImage, state)
        
        // 2. Create mutable copy for composite
        val composite = adjustedImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(composite)
        
        // 3. Render all layers in z-order (back to front)
        renderAllLayers(canvas, state)
        
        // 4. Apply crop if specified
        return if (cropRect != null && isValidCropRect(cropRect, composite)) {
            Bitmap.createBitmap(
                composite,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
            )
        } else {
            composite
        }
    }
    
    private fun renderAllLayers(canvas: Canvas, state: EditorState) {
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
                allLayers.add(layer.zIndex to { renderStickerLayer(canvas, layer) })
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
        
        // Apply transformations using TEXT bounds as pivot (not total bounds)
        // This matches Compose's graphicsLayer which pivots around Box content
        canvas.translate(layer.x, layer.y)  // Move to layer position
        canvas.translate(textWidth / 2f, textHeight / 2f)  // Offset to text center
        canvas.rotate(layer.rotation)  // Rotate around text center
        canvas.scale(layer.scale, layer.scale)  // Scale around text center
        
        // Apply 3D rotations if present (simplified - full 3D requires Camera)
        // Note: This is a simplified version, full 3D rotation requires android.graphics.Camera
        if (layer.rotationX != 0f || layer.rotationY != 0f) {
            // Skew approximation for 3D effect
            val skewX = layer.rotationY / 100f
            val skewY = layer.rotationX / 100f
            canvas.skew(skewX, skewY)
        }
        
        canvas.translate(-textWidth / 2f, -textHeight / 2f)  // Back to text top-left
        
        // Draw background AROUND the text (padding expands outward from text bounds)
        if (layer.showBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.backgroundColor
                style = Paint.Style.FILL
                alpha = (layer.backgroundOpacity * 255).toInt()
            }
            
            // Background rect expands from text bounds
            val bgRect = RectF(
                -paddingPx,  // Left edge
                fm.ascent - paddingPx,  // Top edge (ascent is negative)
                textWidth + paddingPx,  // Right edge
                fm.descent + paddingPx  // Bottom edge
            )
            
            canvas.drawRoundRect(bgRect, radiusPx, radiusPx, bgPaint)
        }
        
        // Draw text at baseline (0, 0 is top-left of text bounds)
        val baselineY = -fm.ascent  // Distance from top to baseline
        
        // Draw glitch effect (RGB offset)
        if (layer.isGlitch) {
            val glitchOffset = 4f
            
            // Red channel offset
            val redPaint = Paint(paint).apply {
                color = android.graphics.Color.RED
                alpha = (0.7f * layer.layerOpacity * 255).toInt()
                clearShadowLayer()
            }
            canvas.drawText(layer.text, -glitchOffset, baselineY - glitchOffset, redPaint)
            
            // Cyan channel offset
            val cyanPaint = Paint(paint).apply {
                color = android.graphics.Color.CYAN
                alpha = (0.7f * layer.layerOpacity * 255).toInt()
                clearShadowLayer()
            }
            canvas.drawText(layer.text, glitchOffset, baselineY + glitchOffset, cyanPaint)
        }
        
        // Apply outline if enabled
        if (layer.hasOutline && layer.outlineWidth > 0) {
            val outlinePaint = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.outlineWidth
                color = layer.outlineColor
                clearShadowLayer()  // No shadow on outline
            }
            canvas.drawText(layer.text, 0f, baselineY, outlinePaint)
        }
        
        // Draw main text
        canvas.drawText(layer.text, 0f, baselineY, paint)
        
        // Draw reflection if enabled
        if (layer.reflectionOpacity > 0f) {
            canvas.save()
            
            // Flip vertically and offset
            canvas.scale(1f, -1f)
            canvas.translate(0f, -textHeight - layer.reflectionOffset)
            
            val reflectionPaint = Paint(paint).apply {
                alpha = (layer.reflectionOpacity * layer.layerOpacity * 255).toInt()
                // Create gradient for fade effect
                shader = LinearGradient(
                    0f, 0f,
                    0f, textHeight,
                    intArrayOf(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.WHITE
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            
            canvas.drawText(layer.text, 0f, baselineY, reflectionPaint)
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
            else -> {
                // Placeholder for others
                val rect = RectF(0f, 0f, w, h)
                canvas.drawRect(rect, paint)
            }
        }
        
        canvas.restore()
    }
    
    private fun renderStickerLayer(canvas: Canvas, layer: StickerLayer) {
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
        
        // Match Compose graphicsLayer: translate to top-left, offset to center, rotate/scale
        canvas.translate(layer.x, layer.y)  // Top-left corner
        canvas.translate(contentWidth / 2f, contentHeight / 2f)  // Offset to center
        canvas.rotate(layer.rotation)  // Rotate around center
        
        val sx = if (layer.isFlipped) -layer.scale else layer.scale
        val sy = layer.scale
        canvas.scale(sx, sy)  // Scale around center
        canvas.translate(-contentWidth / 2f, -contentHeight / 2f)  // Back to top-left
        
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
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = action.path.color
                    strokeWidth = action.path.strokeWidth
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                
                val path = Path().apply {
                    if (action.path.points.isNotEmpty()) {
                        moveTo(action.path.points.first().x, action.path.points.first().y)
                        action.path.points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }
                }
                
                canvas.drawPath(path, paint)
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
}
