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
    val drawPaths: List<DrawPath> = emptyList(),
    val shapes: List<Shape> = emptyList()
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
    val fontSize: Float = 24f,
    val fontFamily: String = "default",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.2f,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val hasOutline: Boolean = false,
    val outlineColor: Int = android.graphics.Color.BLACK,
    val outlineWidth: Float = 2f,
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 10f,
    val backgroundColor: Int = android.graphics.Color.TRANSPARENT,
    val backgroundOpacity: Float = 1f,
    val backgroundPadding: Float = 16f,
    val showBackground: Boolean = false,
    val isLocked: Boolean = false
)

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

data class DrawPath(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<androidx.compose.ui.geometry.Offset>,
    val color: Int,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

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
    FREEHAND, LINE, RECTANGLE, CIRCLE
}

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
    val drawPaths: List<DrawPath> = emptyList(),
    val shapes: List<Shape> = emptyList(),
    val currentDrawColor: Int = android.graphics.Color.RED,
    val currentStrokeWidth: Float = 5f,
    val isEraserMode: Boolean = false,
    val selectedDrawTool: DrawTool = DrawTool.FREEHAND,
    val rotationAngle: Float = 0f,
    val hue: Float = 0f,           // -180 to 180
    val temperature: Float = 0f,   // -1 to 1
    val tint: Float = 0f,          // -1 to 1
    val selectedTextLayerId: String? = null,
    val editingTextLayerId: String? = null,
    val showFloatingToolbar: Boolean = false
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
    
    fun updateHue(value: Float) {
        _uiState.update { it.copy(hue = value) }
        scheduleApply(saveHistory = true)
    }
    
    fun updateTemperature(value: Float) {
        _uiState.update { it.copy(temperature = value) }
        scheduleApply(saveHistory = true)
    }
    
    fun updateTint(value: Float) {
        _uiState.update { it.copy(tint = value) }
        scheduleApply(saveHistory = true)
    }

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
            rotationAngle = state.rotationAngle,
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
            
            // Render text layers onto bitmap if any exist
            if (state.textLayers.isNotEmpty()) {
                bitmap = renderTextLayersOnBitmap(bitmap, state.textLayers)
            }
            
            // Render drawings onto bitmap if any exist
            if (state.drawPaths.isNotEmpty() || state.shapes.isNotEmpty()) {
                bitmap = renderDrawingsOnBitmap(bitmap, state.drawPaths, state.shapes)
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
                "serif" -> android.graphics.Typeface.SERIF
                "monospace" -> android.graphics.Typeface.MONOSPACE
                "cursive" -> android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
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
    
    private fun renderDrawingsOnBitmap(originalBitmap: Bitmap, paths: List<DrawPath>, shapes: List<Shape>): Bitmap {
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(mutableBitmap)
        
        // Render freehand paths
        paths.forEach { drawPath ->
            val paint = android.graphics.Paint().apply {
                color = drawPath.color
                strokeWidth = drawPath.strokeWidth
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
                isAntiAlias = true
            }
            
            val path = android.graphics.Path()
            drawPath.points.forEach { point ->
                if (path.isEmpty) path.moveTo(point.x, point.y)
                else path.lineTo(point.x, point.y)
            }
            canvas.drawPath(path, paint)
        }
        
        // Render shapes
        shapes.forEach { shape ->
            val paint = android.graphics.Paint().apply {
                color = shape.color
                strokeWidth = shape.strokeWidth
                isAntiAlias = true
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
        
        return mutableBitmap
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    // Rotation Methods
    fun rotate90CW() {
        val newAngle = (_uiState.value.rotationAngle + 90f) % 360f
        _uiState.update { it.copy(rotationAngle = newAngle) }
        scheduleApply(saveHistory = true)
    }
    
    fun rotate90CCW() {
        val newAngle = (_uiState.value.rotationAngle - 90f + 360f) % 360f
        _uiState.update { it.copy(rotationAngle = newAngle) }
        scheduleApply(saveHistory = true)
    }
    
    fun flipHorizontal() {
        viewModelScope.launch {
            val bitmap = _uiState.value.previewBitmap ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            val matrix = android.graphics.Matrix().apply {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            }
            val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            
            _uiState.update { it.copy(previewBitmap = flipped, isLoading = false) }
            saveToHistory()
        }
    }
    
    fun flipVertical() {
        viewModelScope.launch {
            val bitmap = _uiState.value.previewBitmap ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            val matrix = android.graphics.Matrix().apply {
                postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)
            }
            val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            
            _uiState.update { it.copy(previewBitmap = flipped, isLoading = false) }
            saveToHistory()
        }
    }
    
    fun setRotationAngle(angle: Float) {
        _uiState.update { it.copy(rotationAngle = angle) }
        scheduleApply(saveHistory = false)
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
            drawPaths = _uiState.value.drawPaths,
            shapes = _uiState.value.shapes
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
                drawPaths = state.drawPaths,
                shapes = state.shapes,
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
        fontFamily: String = "default",
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
    
    // Drawing Methods
    fun addDrawPath(path: DrawPath) {
        _uiState.update { it.copy(drawPaths = it.drawPaths + path) }
        saveToHistory()
    }
    
    fun addShape(shape: Shape) {
        _uiState.update { it.copy(shapes = it.shapes + shape) }
        saveToHistory()
    }
    
    fun updateDrawColor(color: Int) {
        _uiState.update { it.copy(currentDrawColor = color) }
    }
    
    fun updateStrokeWidth(width: Float) {
        _uiState.update { it.copy(currentStrokeWidth = width) }
    }
    
    fun toggleEraserMode() {
        _uiState.update { it.copy(isEraserMode = !it.isEraserMode) }
    }
    
    fun selectDrawTool(tool: DrawTool) {
        _uiState.update { it.copy(selectedDrawTool = tool, isEraserMode = false) }
    }
    
    fun clearAllDrawings() {
        _uiState.update { it.copy(drawPaths = emptyList(), shapes = emptyList()) }
    }
    
    fun deleteDrawing(id: String) {
        _uiState.update {
            it.copy(
                drawPaths = it.drawPaths.filter { path -> path.id != id },
                shapes = it.shapes.filter { shape -> shape.id != id }
            )
        }
    }
    
    // Text Box Management
    fun createTextBoxAtCenter() {
        val newLayer = TextLayer(
            text = "Tap to edit",
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
        fontFamily: String,
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

}

