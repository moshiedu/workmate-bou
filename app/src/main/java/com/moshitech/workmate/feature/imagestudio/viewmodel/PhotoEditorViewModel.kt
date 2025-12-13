package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.imagestudio.repository.EditRepository
import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorState(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val rotationAngle: Float = 0f,
    val activeFilterId: String? = null,
    val activeFilterMatrix: FloatArray? = null,
    val textLayers: List<TextLayer> = emptyList(),
    val stickerLayers: List<StickerLayer> = emptyList(),
    val drawActions: List<DrawAction> = emptyList()
)

data class TextLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float = 200f,
    val height: Float = 80f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val color: Int = android.graphics.Color.WHITE,
    val isGradient: Boolean = false,
    val gradientColors: List<Int> = listOf(android.graphics.Color.BLUE, android.graphics.Color.MAGENTA),
    val gradientAngle: Float = 0f,
    val fontSize: Float = 24f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isAllCaps: Boolean = false,
    val isSmallCaps: Boolean = false,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.2f,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.MIDDLE,
    val textOrientation: TextOrientation = TextOrientation.HORIZONTAL,
    val hasOutline: Boolean = false,
    val outlineColor: Int = android.graphics.Color.BLACK,
    val outlineWidth: Float = 2f,
    val outlineThickness: Float = 2f,
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 10f,
    val shadowOffsetX: Float = 2f,
    val shadowOffsetY: Float = 2f,
    val backgroundColor: Int = android.graphics.Color.TRANSPARENT,
    val backgroundOpacity: Float = 1f,
    val backgroundPadding: Float = 16f,
    val backgroundCornerRadius: Float = 4f,
    val showBackground: Boolean = false,
    val layerOpacity: Float = 1f,
    val textBlur: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val curvature: Float = 0f,
    val isNeon: Boolean = false,
    val isGlitch: Boolean = false,
    val blendMode: LayerBlendMode = LayerBlendMode.NORMAL,
    val reflectionOpacity: Float = 0f,
    val reflectionOffset: Float = 0f,
    val isLocked: Boolean = false,
    val fontFamily: AppFont = AppFont.DEFAULT,
    val textureUri: String? = null
)

data class StickerLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val resId: Int = 0, // Resource ID for local drawables
    val uri: String? = null, // URI for external images if needed later
    val text: String? = null, // Logic for Emoji stickers
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val isFlipped: Boolean = false,
    val isLocked: Boolean = false
)

enum class AppFont {
    DEFAULT, SERIF, SANS_SERIF, MONOSPACE, CURSIVE,
    LOBSTER, BANGERS, OSWALD, PLAYFAIR
}

enum class LayerBlendMode {
    NORMAL, OVERLAY, SCREEN, MULTIPLY, ADD, DIFFERENCE
}

enum class TextAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

enum class VerticalAlignment {
    TOP, MIDDLE, BOTTOM
}

enum class TextOrientation {
    HORIZONTAL, VERTICAL
}


data class DrawPath(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<androidx.compose.ui.geometry.Offset>,
    val color: Int,
    val strokeWidth: Float,
    val isEraser: Boolean = false,
    val isHighlighter: Boolean = false,
    val isNeon: Boolean = false,
    val isMosaic: Boolean = false,
    val strokeStyle: StrokeStyle = StrokeStyle.SOLID
)

sealed class DrawAction {
    data class Path(val path: DrawPath) : DrawAction()
    data class Shape(val shape: com.moshitech.workmate.feature.imagestudio.viewmodel.Shape) : DrawAction()
}

sealed class Shape {
    abstract val id: String
    abstract val color: Int
    abstract val strokeWidth: Float
    
    data class Line(
        override val id: String = java.util.UUID.randomUUID().toString(),
        val start: androidx.compose.ui.geometry.Offset,
        val end: androidx.compose.ui.geometry.Offset,
        override val color: Int,
        override val strokeWidth: Float
    ) : Shape()
    
    data class Rectangle(
        override val id: String = java.util.UUID.randomUUID().toString(),
        val topLeft: androidx.compose.ui.geometry.Offset,
        val size: androidx.compose.ui.geometry.Size,
        override val color: Int,
        override val strokeWidth: Float,
        val filled: Boolean = false
    ) : Shape()
    
    data class Circle(
        override val id: String = java.util.UUID.randomUUID().toString(),
        val center: androidx.compose.ui.geometry.Offset,
        val radius: Float,
        override val color: Int,
        override val strokeWidth: Float,
        val filled: Boolean = false
    ) : Shape()
}

enum class DrawTool {
    BRUSH, NEON, MOSAIC, HIGHLIGHTER, ERASER
}

enum class DrawMode {
    PAINT, SHAPES
}

enum class StrokeStyle {
    SOLID, DASHED, DOTTED
}

enum class ShapeType {
    RECTANGLE, CIRCLE, LINE, ARROW
}

data class ShapeLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ShapeType,
    val x: Float, // Center X
    val y: Float, // Center Y
    val width: Float,
    val height: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val color: Int,
    val strokeWidth: Float,
    val isFilled: Boolean = false,
    val isLocked: Boolean = false,
    val strokeStyle: StrokeStyle = StrokeStyle.SOLID,
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 10f,
    val shadowX: Float = 5f,
    val shadowY: Float = 5f
)

data class PhotoEditorUiState(
    val originalBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val activeFilterId: String? = null,
    val activeFilterMatrix: FloatArray? = null,
    val message: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showSaveDialog: Boolean = false,
    val saveFilename: String = "Workmate_${System.currentTimeMillis()}",
    val textLayers: List<TextLayer> = emptyList(),
    val showTextDialog: Boolean = false,
    val editingTextId: String? = null,
    val stickerLayers: List<StickerLayer> = emptyList(),
    val shapeLayers: List<ShapeLayer> = emptyList(),
    // Unified Drawing State
    val drawActions: List<DrawAction> = emptyList(),
    val redoStack: List<DrawAction> = emptyList(),
    
    val currentDrawColor: Int = android.graphics.Color.RED,
    val currentStrokeWidth: Float = 5f,
    val selectedDrawTool: DrawTool = DrawTool.BRUSH,
    val currentOpacity: Float = 1f, 

    val rotationAngle: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val hue: Float = 0f,           // -180 to 180
    val temperature: Float = 0f,   // -1 to 1
    val tint: Float = 0f,          // -1 to 1
    val selectedTextLayerId: String? = null,
    val editingTextLayerId: String? = null,
    val selectedStickerLayerId: String? = null,
    val selectedShapeLayerId: String? = null,
    val showFloatingToolbar: Boolean = false,
    // Add missing properties
    val activeDrawMode: DrawMode = DrawMode.PAINT,
    val currentStrokeStyle: StrokeStyle = StrokeStyle.SOLID,
    val currentShadowColor: Int = android.graphics.Color.BLACK,
    val currentShadowBlur: Float = 10f,
    val currentShadowX: Float = 5f,
    val currentShadowY: Float = 5f
)

class PhotoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EditRepository(application)
    
    private val _uiState = MutableStateFlow(PhotoEditorUiState())
    val uiState: StateFlow<PhotoEditorUiState> = _uiState.asStateFlow()

    private var applyJob: Job? = null
    
    // Undo/Redo History
    private val historyStack = mutableListOf<EditorState>()
    private var currentHistoryIndex = -1
    private val maxHistorySize = 20

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bitmap = repository.loadBitmap(uri)
            if (bitmap != null) {
                _uiState.update { 
                    it.copy(
                        originalBitmap = bitmap,
                        previewBitmap = bitmap,
                        isLoading = false
                    ) 
                }
            } else {
                _uiState.update { it.copy(isLoading = false, message = "Failed to load image") }
            }
        }
        // Initialize history with default state
        saveToHistory()
    }

    fun updateBrightness(value: Float) {
        _uiState.update { it.copy(brightness = value) }
        scheduleApply(saveHistory = true)
    }

    fun updateContrast(value: Float) {
        _uiState.update { it.copy(contrast = value) }
        scheduleApply(saveHistory = true)
    }

    fun updateSaturation(value: Float) {
        _uiState.update { it.copy(saturation = value) }
        scheduleApply(saveHistory = true)
    }
    
    fun setHue(hue: Float) {
        _uiState.update { it.copy(hue = hue) }
        scheduleApply(saveHistory = true)
    }

    fun setTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = temp) }
        scheduleApply(saveHistory = true)
    }

    fun setTint(tint: Float) {
        _uiState.update { it.copy(tint = tint) }
        scheduleApply(saveHistory = true)
    }
    
    fun resetAdjustments() {
        _uiState.update { it.copy(
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            hue = 0f,
            temperature = 0f,
            tint = 0f
        ) }
        scheduleApply(saveHistory = true)
    }

    // Filter Logic
    fun applyFilter(id: String, matrix: FloatArray) {
        if (MonetizationManager.isFilterLocked(id)) {
            _uiState.update { it.copy(message = "This filter is a Pro feature") }
            return
        }
        _uiState.update { it.copy(activeFilterId = id, activeFilterMatrix = matrix) }
        scheduleApply(saveHistory = true)
    }
    
    fun clearFilter() {
        _uiState.update { it.copy(activeFilterId = null, activeFilterMatrix = null) }
        scheduleApply(saveHistory = true)
    }

    private fun scheduleApply(saveHistory: Boolean = false) {
        applyJob?.cancel()
        applyJob = viewModelScope.launch {
            delay(100) // Debounce slightly
            applyTransformationsNow()
            if (saveHistory) {
                saveToHistory()
            }
        }
    }


    private suspend fun applyTransformationsNow() {
        val state = _uiState.value
        val original = state.originalBitmap ?: return
        
        _uiState.update { it.copy(isLoading = true) }
        
        val newBitmap = repository.applyTransformations(
            original = original,
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            filterMatrix = state.activeFilterMatrix,
            rotationAngle = 0f, // Use 0f to keep previewBitmap unrotated (Visual only)
            hue = state.hue,
            temperature = state.temperature,
            tint = state.tint
        )
        
        _uiState.update { it.copy(previewBitmap = newBitmap, isLoading = false) }
    }
    
    fun showSaveDialog() {
        val defaultName = "Workmate_${System.currentTimeMillis()}"
        _uiState.update { it.copy(showSaveDialog = true, saveFilename = defaultName) }
    }
    
    fun updateSaveFilename(filename: String) {
        _uiState.update { it.copy(saveFilename = filename) }
    }
    
    fun dismissSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }
    
    fun saveImage(filename: String, onSaved: (Uri) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            var bitmap = state.previewBitmap ?: return@launch
            
            _uiState.update { it.copy(isSaving = true) }

            // Apply Rotation and Flip (Baked for Save)
            if (state.rotationAngle != 0f || state.flipX || state.flipY) {
                val matrix = android.graphics.Matrix()
                matrix.postRotate(state.rotationAngle)
                matrix.postScale(
                    if (state.flipX) -1f else 1f, 
                    if (state.flipY) -1f else 1f
                )
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            
            // Render text layers onto bitmap if any exist
            if (state.textLayers.isNotEmpty()) {
                bitmap = renderTextLayersOnBitmap(bitmap, state.textLayers)
            }
            
            // Render drawings onto bitmap if any exist
            // Render drawings onto bitmap if any exist
            if (state.drawActions.isNotEmpty()) {
                bitmap = renderDrawingsOnBitmap(bitmap, state.drawActions)
            }
            
            // Check Pro Quality
            val quality = if (MonetizationManager.isHighQualitySaveLocked()) 85 else 100
            val uri = repository.saveBitmap(bitmap, filename, android.graphics.Bitmap.CompressFormat.JPEG, quality)
            
            _uiState.update { it.copy(isSaving = false, showSaveDialog = false) }
            
            if (uri != null) {
                _uiState.update { it.copy(message = "Image saved to gallery!") }
                onSaved(uri)
            } else {
                _uiState.update { it.copy(message = "Failed to save image") }
            }
        }
    }
    
    private fun renderTextLayersOnBitmap(originalBitmap: Bitmap, textLayers: List<TextLayer>): Bitmap {
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)
        
        textLayers.forEach { layer ->
            // Create typeface based on font family and style
            val baseTypeface = when (layer.fontFamily) {
                AppFont.SERIF -> android.graphics.Typeface.SERIF
                AppFont.MONOSPACE -> android.graphics.Typeface.MONOSPACE
                AppFont.CURSIVE -> android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                AppFont.LOBSTER -> androidx.core.content.res.ResourcesCompat.getFont(getApplication(), com.moshitech.workmate.R.font.lobster)
                AppFont.BANGERS -> androidx.core.content.res.ResourcesCompat.getFont(getApplication(), com.moshitech.workmate.R.font.bangers)
                AppFont.OSWALD -> androidx.core.content.res.ResourcesCompat.getFont(getApplication(), com.moshitech.workmate.R.font.oswald_medium)
                AppFont.PLAYFAIR -> androidx.core.content.res.ResourcesCompat.getFont(getApplication(), com.moshitech.workmate.R.font.playfair_display)
                else -> android.graphics.Typeface.DEFAULT
            }
            
            val style = when {
                layer.isBold && layer.isItalic -> android.graphics.Typeface.BOLD_ITALIC
                layer.isBold -> android.graphics.Typeface.BOLD
                layer.isItalic -> android.graphics.Typeface.ITALIC
                else -> android.graphics.Typeface.NORMAL
            }
            
            val typeface = android.graphics.Typeface.create(baseTypeface, style)
            
            val paint = android.graphics.Paint().apply {
                color = layer.color
                textSize = layer.fontSize * 3f
                isAntiAlias = true
                this.typeface = typeface
                textAlign = when (layer.alignment) {
                    TextAlignment.LEFT -> android.graphics.Paint.Align.LEFT
                    TextAlignment.CENTER -> android.graphics.Paint.Align.CENTER
                    TextAlignment.RIGHT -> android.graphics.Paint.Align.RIGHT
                    TextAlignment.JUSTIFY -> android.graphics.Paint.Align.LEFT // Justify not supported in Paint
                }
            }
            
            // Draw shadow if enabled
            if (layer.hasShadow) {
                paint.setShadowLayer(4f, 2f, 2f, layer.shadowColor)
            }
            
            // Draw outline if enabled
            if (layer.hasOutline) {
                val outlinePaint = android.graphics.Paint()
                outlinePaint.color = layer.outlineColor
                outlinePaint.textSize = layer.fontSize * 3f
                outlinePaint.isAntiAlias = true
                outlinePaint.typeface = typeface
                outlinePaint.textAlign = paint.textAlign
                outlinePaint.style = android.graphics.Paint.Style.STROKE
                outlinePaint.strokeWidth = layer.outlineWidth
                canvas.drawText(layer.text, layer.x, layer.y, outlinePaint)
            }
            
            // Draw main text
            paint.style = android.graphics.Paint.Style.FILL
            canvas.drawText(layer.text, layer.x, layer.y, paint)
        }
        
        return mutableBitmap
    }
    
    private fun renderDrawingsOnBitmap(originalBitmap: Bitmap, actions: List<DrawAction>): Bitmap {
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)
        
        actions.forEach { action ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
            }

            when (action) {
                is DrawAction.Path -> {
                    val drawPath = action.path
                    paint.apply {
                        color = if (drawPath.isEraser) android.graphics.Color.TRANSPARENT else drawPath.color
                        strokeWidth = drawPath.strokeWidth
                        style = android.graphics.Paint.Style.STROKE
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        strokeJoin = android.graphics.Paint.Join.ROUND
                        
                        if (drawPath.isEraser) {
                            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                        } else if (drawPath.isHighlighter) {
                             xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DARKEN) // or SC(Source Over) with alpha logic handled in color
                             alpha = 128 // 50% Opacity for Highlighter
                        } else if (drawPath.isNeon) {
                             // Neon Glow
                             setShadowLayer(12f, 0f, 0f, drawPath.color)
                             // Main stroke is brighter or white? Often Neon is color with color glow.
                             // Let's keep it simple: Color with Glow.
                        }
                    }

                    val path = android.graphics.Path()
                    drawPath.points.forEachIndexed { index, point ->
                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }
                    canvas.drawPath(path, paint)
                    
                    // Enhancement: Draw a white core for Neon to make it pop
                    if (drawPath.isNeon) {
                        paint.setShadowLayer(0f, 0f, 0f, 0) // Clear shadow
                        paint.color = android.graphics.Color.WHITE
                        paint.strokeWidth = drawPath.strokeWidth / 3f // Thinner core
                        paint.alpha = 200
                        canvas.drawPath(path, paint)
                    }
                }
                is DrawAction.Shape -> {
                    val shape = action.shape
                    paint.apply {
                        color = shape.color
                        strokeWidth = shape.strokeWidth
                        alpha = (android.graphics.Color.alpha(shape.color)) // Use color alpha
                    }
                    
                    when (shape) {
                        is Shape.Line -> {
                            paint.style = android.graphics.Paint.Style.STROKE
                            paint.strokeCap = android.graphics.Paint.Cap.ROUND
                            canvas.drawLine(
                                shape.start.x, shape.start.y,
                                shape.end.x, shape.end.y,
                                paint
                            )
                        }
                        is Shape.Rectangle -> {
                            paint.style = if (shape.filled) android.graphics.Paint.Style.FILL 
                                          else android.graphics.Paint.Style.STROKE
                            canvas.drawRect(
                                shape.topLeft.x,
                                shape.topLeft.y,
                                shape.topLeft.x + shape.size.width,
                                shape.topLeft.y + shape.size.height,
                                paint
                            )
                        }
                        is Shape.Circle -> {
                            paint.style = if (shape.filled) android.graphics.Paint.Style.FILL 
                                          else android.graphics.Paint.Style.STROKE
                            canvas.drawCircle(
                                shape.center.x,
                                shape.center.y,
                                shape.radius,
                                paint
                            )
                        }
                    }
                }
            }
        }
        
        return mutableBitmap
    }


    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    // Rotation Methods
fun rotate90CW() {
    val newAngle = (_uiState.value.rotationAngle + 90f) % 360f
    _uiState.update { it.copy(rotationAngle = newAngle) }
    // Visual only - no scheduleApply
}

fun rotate90CCW() {
    val newAngle = (_uiState.value.rotationAngle - 90f + 360f) % 360f
    _uiState.update { it.copy(rotationAngle = newAngle) }
    // Visual only - no scheduleApply
}

fun flipHorizontal() {
    _uiState.update { it.copy(flipX = !it.flipX) }
    saveToHistory()
}

fun flipVertical() {
    _uiState.update { it.copy(flipY = !it.flipY) }
    saveToHistory()
}

fun setRotationAngle(angle: Float) {
    _uiState.update { it.copy(rotationAngle = angle) }
}
    
    // Undo/Redo Methods
    private fun saveToHistory() {
        val currentState = EditorState(
            brightness = _uiState.value.brightness,
            contrast = _uiState.value.contrast,
            saturation = _uiState.value.saturation,
            hue = _uiState.value.hue,
            temperature = _uiState.value.temperature,
            tint = _uiState.value.tint,
            rotationAngle = _uiState.value.rotationAngle,
            activeFilterId = _uiState.value.activeFilterId,
            activeFilterMatrix = _uiState.value.activeFilterMatrix,
            textLayers = _uiState.value.textLayers,
            drawActions = _uiState.value.drawActions
        )
        
        // Remove any states after current index (when user made changes after undo)
        if (currentHistoryIndex < historyStack.size - 1) {
            historyStack.subList(currentHistoryIndex + 1, historyStack.size).clear()
        }
        
        // Add new state
        historyStack.add(currentState)
        currentHistoryIndex = historyStack.size - 1
        
        // Limit history size
        if (historyStack.size > maxHistorySize) {
            historyStack.removeAt(0)
            currentHistoryIndex--
        }
        
        updateHistoryButtons()
    }
    
    fun undo() {
        if (currentHistoryIndex > 0) {
            currentHistoryIndex--
            restoreState(historyStack[currentHistoryIndex])
            updateHistoryButtons()
        }
    }
    
    fun redo() {
        if (currentHistoryIndex < historyStack.size - 1) {
            currentHistoryIndex++
            restoreState(historyStack[currentHistoryIndex])
            updateHistoryButtons()
        }
    }
    
    fun resetToOriginal() {
        val defaultState = EditorState()
        restoreState(defaultState)
        
        // Clear history and start fresh
        historyStack.clear()
        currentHistoryIndex = -1
        saveToHistory()
    }
    
    private fun restoreState(state: EditorState) {
        _uiState.update {
            it.copy(
                brightness = state.brightness,
                contrast = state.contrast,
                saturation = state.saturation,
                hue = state.hue,
                temperature = state.temperature,
                tint = state.tint,
                rotationAngle = state.rotationAngle,
                activeFilterId = state.activeFilterId,
                activeFilterMatrix = state.activeFilterMatrix,
                textLayers = state.textLayers,
                drawActions = state.drawActions,
                // Reset dialogs
                showTextDialog = false,
                editingTextId = null
            )
        }
        scheduleApply(saveHistory = false)
    }
    
    private fun updateHistoryButtons() {
        _uiState.update {
            it.copy(
                canUndo = currentHistoryIndex > 0,
                canRedo = currentHistoryIndex < historyStack.size - 1
            )
        }
    }
    
    // Text Layer Methods
    fun showTextDialog() {
        _uiState.update { it.copy(showTextDialog = true, editingTextId = null) }
    }
    
    fun showEditTextDialog(textId: String) {
        _uiState.update { it.copy(showTextDialog = true, editingTextId = textId) }
    }
    
    fun dismissTextDialog() {
        _uiState.update { it.copy(showTextDialog = false, editingTextId = null) }
    }
    
    fun addTextLayer(
        text: String,
        x: Float = 100f,
        y: Float = 100f,
        color: Int = android.graphics.Color.WHITE,
        fontSize: Float = 20f,
        fontFamily: AppFont = AppFont.DEFAULT,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        alignment: TextAlignment = TextAlignment.LEFT,
        hasOutline: Boolean = false,
        outlineColor: Int = android.graphics.Color.BLACK,
        hasShadow: Boolean = false
    ) {
        val newLayer = TextLayer(
            text = text,
            x = x,
            y = y,
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            isBold = isBold,
            isItalic = isItalic,
            alignment = alignment,
            hasOutline = hasOutline,
            outlineColor = outlineColor,
            hasShadow = hasShadow
        )
        _uiState.update { it.copy(textLayers = it.textLayers + newLayer, showTextDialog = false) }
        saveToHistory()
    }
    

    
    fun moveTextLayer(textId: String, x: Float, y: Float) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == textId) layer.copy(x = x, y = y)
                    else layer
                }
            )
        }
        saveToHistory()
    }
    
    fun deleteTextLayer(textId: String) {
        _uiState.update {
            it.copy(textLayers = it.textLayers.filter { layer -> layer.id != textId })
        }
        saveToHistory()
    }
    

    
    fun updateDrawColor(color: Int) {
        _uiState.update { state -> 
            state.copy(
                currentDrawColor = color,
                shapeLayers = if (state.selectedShapeLayerId != null) {
                    state.shapeLayers.map { 
                        if (it.id == state.selectedShapeLayerId && !it.isLocked) it.copy(color = color) else it
                    }
                } else state.shapeLayers
            ) 
        }
    }

    fun updateStrokeWidth(width: Float) {
        _uiState.update { state -> 
            state.copy(
                currentStrokeWidth = width,
                shapeLayers = if (state.selectedShapeLayerId != null) {
                    state.shapeLayers.map { 
                        if (it.id == state.selectedShapeLayerId && !it.isLocked) it.copy(strokeWidth = width) else it
                    }
                } else state.shapeLayers
            ) 
        }
    }
    
    fun selectDrawTool(tool: DrawTool) {
        _uiState.update { it.copy(selectedDrawTool = tool) }
    }
    
    // --- Unified Draw Logic ---

    fun addDrawAction(action: DrawAction) {
        _uiState.update { currentState ->
            currentState.copy(
                drawActions = currentState.drawActions + action,
                redoStack = emptyList(), // Clear redo on new action
                canUndo = true,
                canRedo = false
            )
        }
    }

    // Deprecated: Migrating to addDrawAction
    fun addDrawPath(path: DrawPath) {
        addDrawAction(DrawAction.Path(path))
    }

    // Deprecated: Migrating to addDrawAction
    fun addShape(shape: Shape) {
        addDrawAction(DrawAction.Shape(shape))
    }

    fun undoDrawAction() { // Renamed to avoid conflict with general undo()
        val currentState = _uiState.value
        if (currentState.drawActions.isNotEmpty()) {
            val lastAction = currentState.drawActions.last()
            _uiState.update {
                it.copy(
                    drawActions = it.drawActions.dropLast(1),
                    redoStack = it.redoStack + lastAction,
                    canUndo = it.drawActions.size > 1,
                    canRedo = true
                )
            }
        }
    }

    fun redoDrawAction() { // Renamed to avoid conflict with general redo()
        val currentState = _uiState.value
        if (currentState.redoStack.isNotEmpty()) {
            val actionToRedo = currentState.redoStack.last()
            _uiState.update {
                it.copy(
                    drawActions = it.drawActions + actionToRedo,
                    redoStack = it.redoStack.dropLast(1),
                    canUndo = true,
                    canRedo = it.redoStack.size > 1
                )
            }
        }
    }

    // New Helpers
    fun updateOpacity(opacity: Float) {
        _uiState.update { it.copy(currentOpacity = opacity) }
    }

    // Legacy Support (To be removed after UI update)
    // fun undoDrawPath() { ... } 
    // fun redoDrawPath() { ... }
    
    fun clearAllDrawings() {
        _uiState.update { it.copy(drawActions = emptyList()) }
    }
    
    fun deleteDrawing(id: String) {
        _uiState.update { state ->
            state.copy(
                drawActions = state.drawActions.filter { action ->
                    when (action) {
                        is DrawAction.Path -> action.path.id != id
                        is DrawAction.Shape -> action.shape.id != id
                    }
                }
            )
        }
    }
    
    // Text Box Management
    fun createTextBoxAtCenter() {
        val newLayer = TextLayer(
            text = "Your Text Here",
            x = 90f, // Centered horizontally (screen width ~360-400dp, text width 200dp)
            y = 400f, // Centered vertically in visible area
            width = 200f,
            height = 80f
        )
        _uiState.update { 
            it.copy(
                textLayers = it.textLayers + newLayer,
                selectedTextLayerId = newLayer.id,
                editingTextLayerId = null,
                showFloatingToolbar = true,
                showTextDialog = false
            ) 
        }
        saveToHistory()
    }
    
    // Kept (and simplified) for backward compatibility or tap-to-create
    fun createTextBoxAt(x: Float, y: Float) {
        val newLayer = TextLayer(
            text = "",
            x = x,
            y = y,
            width = 200f,
            height = 80f
        )
        _uiState.update { 
            it.copy(
                textLayers = it.textLayers + newLayer,
                selectedTextLayerId = newLayer.id,
                editingTextLayerId = newLayer.id,
                showFloatingToolbar = false
            ) 
        }
        saveToHistory()
    }

    fun updateTextLayerTransform(id: String, pan: androidx.compose.ui.geometry.Offset, zoom: Float, rotation: Float) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) {
                        layer.copy(
                            x = layer.x + pan.x,
                            y = layer.y + pan.y,
                            scale = layer.scale * zoom,
                            rotation = layer.rotation + rotation
                        )
                    } else layer
                }
            )
        }
    }
    
    fun selectTextLayer(id: String) {
        _uiState.update { 
            it.copy(
                selectedTextLayerId = id,
                editingTextLayerId = null,
                showFloatingToolbar = true
            ) 
        }
    }
    
    fun removeTextLayer(id: String) {
        val currentLayers = _uiState.value.textLayers.toMutableList()
        currentLayers.removeAll { it.id == id }
        _uiState.update { it.copy(
            textLayers = currentLayers,
            selectedTextLayerId = null,
            editingTextLayerId = null
        ) }
    }

    fun duplicateTextLayer(id: String) {
        val layerToDuplicate = _uiState.value.textLayers.find { it.id == id } ?: return
        val newLayer = layerToDuplicate.copy(
            id = java.util.UUID.randomUUID().toString(),
            x = layerToDuplicate.x + 40f, // Offset slightly
            y = layerToDuplicate.y + 40f
        )
        val currentLayers = _uiState.value.textLayers.toMutableList()
        currentLayers.add(newLayer)
        _uiState.update { it.copy(
            textLayers = currentLayers,
            selectedTextLayerId = newLayer.id // Select the new layer
        ) }
    }
    
    fun enterTextEditMode(id: String) {
        val layer = _uiState.value.textLayers.find { it.id == id }
        if (layer?.isLocked == true) return
        
        _uiState.update { 
            it.copy(
                selectedTextLayerId = id,
                editingTextLayerId = id,
                showFloatingToolbar = false
            ) 
        }
    }
    
    fun exitTextEditMode() {
        _uiState.update { 
            it.copy(
                editingTextLayerId = null,
                showFloatingToolbar = _uiState.value.selectedTextLayerId != null
            ) 
        }
        saveToHistory()
    }
    
    fun deselectTextLayer() {
        _uiState.update { 
            it.copy(
                selectedTextLayerId = null,
                editingTextLayerId = null,
                showFloatingToolbar = false
            ) 
        }
    }

    // Layer Management Functions
    fun lockLayer(id: String, locked: Boolean) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id) layer.copy(isLocked = locked) else layer
                }
            )
        }
        saveToHistory()
    }

    fun duplicateLayer(id: String) {
        val layer = _uiState.value.textLayers.find { it.id == id } ?: return
        val newLayer = layer.copy(
            id = java.util.UUID.randomUUID().toString(),
            x = layer.x + 40f,
            y = layer.y + 40f
        )
        _uiState.update {
            it.copy(
                textLayers = it.textLayers + newLayer,
                selectedTextLayerId = newLayer.id
            )
        }
        saveToHistory()
    }

    fun bringToFront(id: String) {
        val layer = _uiState.value.textLayers.find { it.id == id } ?: return
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.filter { it.id != id } + layer
            )
        }
        saveToHistory()
    }

    fun sendToBack(id: String) {
        val layer = _uiState.value.textLayers.find { it.id == id } ?: return
        _uiState.update {
            it.copy(
                textLayers = listOf(layer) + it.textLayers.filter { it.id != id }
            )
        }
        saveToHistory()
    }
    


    // Sticker Methods
    fun addSticker(resId: Int = 0, text: String? = null) {
        val newSticker = StickerLayer(
            resId = resId,
            text = text,
            x = 400f, // Center-ish
            y = 400f // Center-ish
        )
        _uiState.update { it.copy(
            stickerLayers = it.stickerLayers + newSticker,
            selectedStickerLayerId = newSticker.id,
            selectedTextLayerId = null // Deselect text
        ) }
        saveToHistory()
    }

    fun removeSticker(id: String) {
        _uiState.update { it.copy(
            stickerLayers = it.stickerLayers.filter { layer -> layer.id != id },
            selectedStickerLayerId = null
        ) }
        saveToHistory()
    }

    fun selectSticker(id: String) {
        _uiState.update { it.copy(
            selectedStickerLayerId = id,
            selectedTextLayerId = null, // Deselect text
            editingTextLayerId = null,
            showFloatingToolbar = false // Stickers might use different toolbar or gestures
        ) }
    }
    
    fun deselectSticker() {
        _uiState.update { it.copy(selectedStickerLayerId = null) }
    }

    fun updateStickerTransform(id: String, pan: androidx.compose.ui.geometry.Offset, zoom: Float, rotation: Float) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) {
                        layer.copy(
                            x = layer.x + pan.x,
                            y = layer.y + pan.y,
                            scale = (layer.scale * zoom).coerceIn(0.1f, 10f),
                            rotation = layer.rotation + rotation
                        )
                    } else layer
                }
            )
        }
    }

    fun flipSticker(id: String) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) {
                        layer.copy(isFlipped = !layer.isFlipped)
                    } else layer
                }
            )
        }
    }

    // Simplified update for properties
    fun updateTextProperty(id: String, update: (TextLayer) -> TextLayer) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) update(layer) else layer
                }
            )
        }
        saveToHistory()
    }
    
    // Backward compatibility shim if needed, or rely on updateTextProperty
    fun updateTextLayer(
        textId: String,
        text: String,
        color: Int,
        fontSize: Float,
        fontFamily: AppFont,
        isBold: Boolean,
        isItalic: Boolean,
        alignment: TextAlignment,
        hasOutline: Boolean,
        outlineColor: Int,
        hasShadow: Boolean
    ) {
         updateTextProperty(textId) {
             it.copy(
                 text = text,
                 color = color,
                 fontSize = fontSize,
                 fontFamily = fontFamily,
                 isBold = isBold,
                 isItalic = isItalic,
                 alignment = alignment,
                 hasOutline = hasOutline,
                 outlineColor = outlineColor,
                 hasShadow = hasShadow
             )
         }
    }

    fun updateTextInline(id: String, newText: String) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(text = newText)
                    else layer
                }
            )
        }
    }

    fun updateTextBoxBackground(id: String, show: Boolean) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id) layer.copy(showBackground = show)
                    else layer
                }
            )
        }
        saveToHistory()
    }

    fun setDrawMode(mode: DrawMode) {
        _uiState.update { it.copy(activeDrawMode = mode) }
    }

    fun setStrokeStyle(style: StrokeStyle) {
        _uiState.update { it.copy(currentStrokeStyle = style) }
    }

    fun addShapeLayer(type: ShapeType) {
        val newShape = ShapeLayer(
            type = type,
            x = 0f, y = 0f, // Default Center relative to image center
            width = 200f, height = 200f,
            color = _uiState.value.currentDrawColor,
            strokeWidth = _uiState.value.currentStrokeWidth,
            strokeStyle = _uiState.value.currentStrokeStyle,
            shadowColor = _uiState.value.currentShadowColor,
            shadowBlur = _uiState.value.currentShadowBlur,
            shadowX = _uiState.value.currentShadowX,
            shadowY = _uiState.value.currentShadowY
        )
        _uiState.update { 
            it.copy(
                shapeLayers = it.shapeLayers + newShape,
                selectedShapeLayerId = newShape.id,
                selectedTextLayerId = null,
                selectedStickerLayerId = null
            ) 
        }
        saveToHistory()
    }

    fun selectShapeLayer(id: String) {
        _uiState.update {
            it.copy(
                selectedShapeLayerId = id,
                selectedTextLayerId = null,
                selectedStickerLayerId = null,
                showFloatingToolbar = true,
                activeDrawMode = DrawMode.SHAPES // Auto-switch to shapes mode
            )
        }
    }

    fun deselectShapeLayer() {
        _uiState.update { it.copy(selectedShapeLayerId = null) }
    }
    
    fun updateShapeLayer(id: String, update: (ShapeLayer) -> ShapeLayer) {
        _uiState.update { state ->
            state.copy(
                shapeLayers = state.shapeLayers.map { if (it.id == id) update(it) else it }
            )
        }
        // Debounce history saving locally if needed, or rely on distinct calls
        // For sliders, we might usually wait, but here we save for simplicity
        saveToHistory()
    }

    fun updateShapeShadow(id: String, hasShadow: Boolean, color: Int, blur: Float, x: Float, y: Float) {
        updateShapeLayer(id) {
            it.copy(
                hasShadow = hasShadow,
                shadowColor = color,
                shadowBlur = blur,
                shadowX = x,
                shadowY = y
            )
        }
    }
    
    fun updateShapeStrokeStyle(id: String, style: StrokeStyle) {
        updateShapeLayer(id) { it.copy(strokeStyle = style) }
    }
    
    fun deleteShapeLayer(id: String) {
        _uiState.update { 
            it.copy(
                shapeLayers = it.shapeLayers.filter { layer -> layer.id != id },
                selectedShapeLayerId = if (it.selectedShapeLayerId == id) null else it.selectedShapeLayerId
            ) 
        }
        saveToHistory()
    }

    fun updateShapeLayerTransform(id: String, pan: androidx.compose.ui.geometry.Offset, zoom: Float, rotation: Float) {
        _uiState.update { state ->
            state.copy(
                shapeLayers = state.shapeLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) {
                        layer.copy(
                            x = layer.x + pan.x,
                            y = layer.y + pan.y,
                            scale = layer.scale * zoom,
                            rotation = layer.rotation + rotation
                        )
                    } else layer
                }
            )
        }
    }

}


