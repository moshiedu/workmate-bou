package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// ADDED:
import androidx.core.net.toUri
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
    val flipX: Boolean = false, // NEW
    val flipY: Boolean = false, // NEW
    val activeFilterId: String? = null,
    val activeFilterMatrix: FloatArray? = null,
    val textLayers: List<TextLayer> = emptyList(),
    val stickerLayers: List<StickerLayer> = emptyList(),
    val shapeLayers: List<ShapeLayer> = emptyList(),
    val drawActions: List<DrawAction> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EditorState

        if (brightness != other.brightness) return false
        if (contrast != other.contrast) return false
        if (saturation != other.saturation) return false
        if (hue != other.hue) return false
        if (temperature != other.temperature) return false
        if (tint != other.tint) return false
        if (rotationAngle != other.rotationAngle) return false
        if (flipX != other.flipX) return false
        if (flipY != other.flipY) return false
        if (activeFilterId != other.activeFilterId) return false
        if (activeFilterMatrix != null) {
            if (other.activeFilterMatrix == null) return false
            if (!activeFilterMatrix.contentEquals(other.activeFilterMatrix)) return false
        } else if (other.activeFilterMatrix != null) return false
        if (textLayers != other.textLayers) return false
        if (stickerLayers != other.stickerLayers) return false
        if (shapeLayers != other.shapeLayers) return false
        if (drawActions != other.drawActions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = brightness.hashCode()
        result = 31 * result + contrast.hashCode()
        result = 31 * result + saturation.hashCode()
        result = 31 * result + hue.hashCode()
        result = 31 * result + temperature.hashCode()
        result = 31 * result + tint.hashCode()
        result = 31 * result + rotationAngle.hashCode()
        result = 31 * result + flipX.hashCode()
        result = 31 * result + flipY.hashCode()
        result = 31 * result + (activeFilterId?.hashCode() ?: 0)
        result = 31 * result + (activeFilterMatrix?.contentHashCode() ?: 0)
        result = 31 * result + textLayers.hashCode()
        result = 31 * result + stickerLayers.hashCode()
        result = 31 * result + shapeLayers.hashCode()
        result = 31 * result + drawActions.hashCode()
        return result
    }
}

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
    val fontSize: Float = 50f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isAllCaps: Boolean = false,
    val isSmallCaps: Boolean = false,
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.0f,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.MIDDLE,
    val textOrientation: TextOrientation = TextOrientation.HORIZONTAL,
    val hasOutline: Boolean = false,
    val outlineColor: Int = android.graphics.Color.BLACK,
    val outlineWidth: Float = 0f,
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
    val textureUri: String? = null,
    
    // NEW: Layer management (Phase 2)
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Text Layer"
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
    val isLocked: Boolean = false,
    
    // NEW: Layer management (Phase 2)
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Sticker Layer",
    
    // NEW: Advanced controls (Phase 3)
    val opacity: Float = 1f,  // 0f (transparent) to 1f (opaque)
    
    // NEW: Shadow effects
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 8f,
    val shadowOffsetX: Float = 4f,
    val shadowOffsetY: Float = 4f,
    
    // NEW: Border effects
    val hasBorder: Boolean = false,
    val borderColor: Int = android.graphics.Color.WHITE,
    val borderWidth: Float = 2f,
    
    // NEW: Color tint
    val hasTint: Boolean = false,
    val tintColor: Int = android.graphics.Color.BLUE,
    val tintStrength: Float = 0.5f  // 0f to 1f
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
    val isSpray: Boolean = false,  // NEW: Spray paint effect
    val blurRadius: Float = 0f,  // NEW: Blur effect
    val blendMode: DrawBlendMode = DrawBlendMode.NORMAL,  // NEW: Blend mode
    val strokeStyle: StrokeStyle = StrokeStyle.SOLID
)

// Blend modes for drawing
enum class DrawBlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    ADD
}

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
    BRUSH, NEON, MOSAIC, HIGHLIGHTER, ERASER, BLUR, SPRAY
}

enum class DrawMode {
    PAINT, SHAPES
}

enum class StrokeStyle {
    SOLID, DASHED, DOTTED, LONG_DASH, DASH_DOT
}

enum class ShapeType {
    RECTANGLE, CIRCLE, LINE, ARROW, TRIANGLE, STAR, PENTAGON
}

// Mosaic pattern types
enum class MosaicPattern(val displayName: String) {
    SQUARE("Square"),
    HEXAGONAL("Hexagonal"),
    CIRCULAR("Circular"),
    DIAMOND("Diamond")
}

// Mosaic color processing modes
enum class MosaicColorMode(val displayName: String) {
    AVERAGE("Average"),
    DOMINANT("Dominant"),
    POSTERIZE("Posterize")
}

// Mosaic preset for quick settings
data class MosaicPreset(
    val name: String,
    val intensity: Float, // 0.01-0.20 scale
    val strokeWidth: Float = 10f,
    val opacity: Float = 1f
) {
    companion object {
        val SUBTLE = MosaicPreset("Subtle", intensity = 0.15f, strokeWidth = 15f, opacity = 0.6f)
        val MEDIUM = MosaicPreset("Medium", intensity = 0.08f, strokeWidth = 10f, opacity = 0.8f)
        val HEAVY = MosaicPreset("Heavy", intensity = 0.03f, strokeWidth = 8f, opacity = 1.0f)
        
        val defaults = listOf(SUBTLE, MEDIUM, HEAVY)
    }
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
    val opacity: Float = 1f, // NEW: Transparency control (0f to 1f)
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 10f,
    val shadowX: Float = 5f,
    val shadowY: Float = 5f,
    
    // NEW: Layer management (Phase 2)
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Shape Layer"
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
    val mosaicIntensity: Float = 0.05f, // Pixelation scale: 0.01 (1%) to 0.20 (20%)
    val mosaicPattern: MosaicPattern = MosaicPattern.SQUARE,
    val mosaicColorMode: MosaicColorMode = MosaicColorMode.AVERAGE,
    val posterizeLevels: Int = 4, // 2-8 color levels for posterize effect

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
    val currentShadowY: Float = 5f,
    val isPreviewMode: Boolean = false,
    
    // NEW: Crop state management
    val cropRect: android.graphics.Rect? = null,
    val originalImageBounds: android.graphics.Rect? = null,
    val isCropApplied: Boolean = false,
    
    // NEW: Layer management
    val hasUnappliedLayers: Boolean = false,
    
    // NEW: Edit history
    val editHistory: List<com.moshitech.workmate.feature.imagestudio.data.EditOperation> = emptyList(),
    val maxHistorySize: Int = 50,
    // NEW: Container dimensions for coordinate mapping
    val containerWidth: Int = 1,
    val containerHeight: Int = 1,
)

class PhotoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EditRepository(application)
    private val editorPreferences = com.moshitech.workmate.feature.imagestudio.data.EditorPreferences(application)
    private val compositeRenderer = com.moshitech.workmate.feature.imagestudio.util.CompositeRenderer(application)
    
    private val _uiState = MutableStateFlow(PhotoEditorUiState())
    val uiState: StateFlow<PhotoEditorUiState> = _uiState.asStateFlow()
    
    // Coordinate Mapping Helpers
    fun updateContainerSize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            _uiState.update { it.copy(containerWidth = width, containerHeight = height) }
        }
    }
    
    fun getBitmapScale(): Float {
        val state = uiState.value
        val bitmap = state.originalBitmap ?: return 1f
        // Scale is determined by "FitCenter" logic: min(containerW/bmW, containerH/bmH)
        val scaleW = state.containerWidth.toFloat() / bitmap.width
        val scaleH = state.containerHeight.toFloat() / bitmap.height
        return kotlin.math.min(scaleW, scaleH)
    }
    
    fun getBitmapOffset(): Pair<Float, Float> {
        val state = uiState.value
        val bitmap = state.originalBitmap ?: return 0f to 0f
        val scale = getBitmapScale()
        val scaledW = bitmap.width * scale
        val scaledH = bitmap.height * scale
        
        val offX = (state.containerWidth - scaledW) / 2f
        val offY = (state.containerHeight - scaledH) / 2f
        
        return offX to offY
    }
    
    // Convert UI Coordinate -> Bitmap Coordinate
    fun uiToBitmapCoord(uiX: Float, uiY: Float): Pair<Float, Float> {
        val (offX, offY) = getBitmapOffset()
        val scale = getBitmapScale()
        val bmX = (uiX - offX) / scale
        val bmY = (uiY - offY) / scale
        return bmX to bmY
    }


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
                        isLoading = false,
                        // Reset all editor state when loading new image (or crop result)
                        textLayers = emptyList(),
                        shapeLayers = emptyList(),
                        stickerLayers = emptyList(),
                        drawActions = emptyList(),
                        cropRect = null,
                        isCropApplied = false,
                        hasUnappliedLayers = false,
                        brightness = 0f,
                        contrast = 1f,
                        saturation = 1f,
                        hue = 0f,
                        temperature = 0f,
                        tint = 0f,
                        activeFilterId = null,
                        activeFilterMatrix = null
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
        
        _uiState.update { it.copy(previewBitmap = newBitmap) }
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
    
    fun saveImage(filename: String, uiScale: Float? = null, onSaved: (Uri) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            var bitmap = state.previewBitmap ?: return@launch
            
            _uiState.update { it.copy(isSaving = true) }

            // REMOVED: Rotation and Flip now handled by CompositeRenderer via EditorState
            
            // Get current editor state (layers are already in bitmap coordinates)
            val currentState = getCurrentEditorState()
            
            // Calculate bitmap scale relative to screen container
            // This is critical for fixed-size layers (Stickers) to render at correct relative size
            val containerW = _uiState.value.containerWidth
            val containerH = _uiState.value.containerHeight
            val bmpW = bitmap.width.toFloat()
            val bmpH = bitmap.height.toFloat()
            
            // Use provided UI scale if available (exact match), otherwise fallback to calculation
            val bitScale = uiScale ?: if (containerW > 0 && containerH > 0) {
                kotlin.math.min(
                    containerW.toFloat() / bmpW,
                    containerH.toFloat() / bmpH
                )
            } else 1f
            
            // Use CompositeRenderer to properly render all layers with formatting
            bitmap = compositeRenderer.renderComposite(
                baseImage = bitmap,
                state = currentState,
                cropRect = null,
                bitmapScale = bitScale
            )
            
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
        
        // Create a pixelated bitmap for Mosaic effect if needed
        // Optimization: Only create if there's a Mosaic action
        val hasMosaic = actions.any { it is DrawAction.Path && it.path.isMosaic }
        val pixelatedBitmap = if (hasMosaic) createPixelatedBitmap(originalBitmap) else null
        val mosaicShader = if (pixelatedBitmap != null) {
            android.graphics.BitmapShader(pixelatedBitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
        } else null

        actions.forEach { action ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
            }

            when (action) {
                is DrawAction.Path -> {
                    val drawPath = action.path
                    paint.apply {
                        if (drawPath.isMosaic && mosaicShader != null) {
                            shader = mosaicShader
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = drawPath.strokeWidth
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            strokeJoin = android.graphics.Paint.Join.ROUND
                            alpha = android.graphics.Color.alpha(drawPath.color) // Apply transparency if mosaic has color (it usually doesn't, but path stores it)
                            // Ideally Mosaic uses original pixels. Transparency on Mosaic means mixing original with... what?
                            // Mixing Pixelated with Transparent (i.e. original/underlying).
                            // If Paint alpha is 50%, it draws 50% pixelated over 100% original.
                            // Effectively "fading in" the mosaic effect.
                        } else {
                            color = if (drawPath.isEraser) android.graphics.Color.TRANSPARENT else drawPath.color
                            strokeWidth = drawPath.strokeWidth
                            style = android.graphics.Paint.Style.STROKE
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        
                        if (drawPath.isEraser) {
                            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                        } else if (drawPath.isHighlighter) {
                             xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY) 
                             // Alpha is already in `color` which is applied above (drawPath.color includes alpha).
                             // We don't need to force set alpha here if we use drawPath.color.
                             // Wait, `color` assignment above handles it.
                             // But checking above code: `color = drawPath.color`.
                             // Remove explicit alpha override here.
                        } else if (drawPath.isNeon) {
                             setShadowLayer(drawPath.strokeWidth * 1.5f, 0f, 0f, drawPath.color)
                        }
                    }

                    val path = android.graphics.Path()
                    drawPath.points.forEachIndexed { index, point ->
                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }
                    canvas.drawPath(path, paint)
                    
                    if (drawPath.isNeon) {
                        paint.shader = null // Ensure no shader for core
                        paint.setShadowLayer(0f, 0f, 0f, 0)
                        paint.color = android.graphics.Color.WHITE
                        paint.strokeWidth = drawPath.strokeWidth / 3f 
                        paint.alpha = 255 // Solid core
                        canvas.drawPath(path, paint)
                    }
                }
                is DrawAction.Shape -> {
                    // Shape rendering remains same
                    val shape = action.shape
                    paint.apply {
                        color = shape.color
                        strokeWidth = shape.strokeWidth
                        alpha = (android.graphics.Color.alpha(shape.color)) 
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
    
    private fun createPixelatedBitmap(
        bitmap: Bitmap, 
        intensity: Float = 0.05f,
        colorMode: MosaicColorMode = MosaicColorMode.AVERAGE,
        posterizeLevels: Int = 4
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scale = intensity.coerceIn(0.01f, 0.20f) // Clamp between 1% and 20%
        
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        
        // Create scaled down version
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
        
        // Apply color mode processing
        val processedBitmap = when (colorMode) {
            MosaicColorMode.AVERAGE -> smallBitmap // Already averaged by scaling
            MosaicColorMode.DOMINANT -> applyDominantColor(smallBitmap, bitmap, scale)
            MosaicColorMode.POSTERIZE -> applyPosterize(smallBitmap, posterizeLevels)
        }
        
        // Scale back up to original size
        val pixelatedBitmap = Bitmap.createScaledBitmap(processedBitmap, width, height, false)
        
        if (processedBitmap != smallBitmap) {
            processedBitmap.recycle()
        }
        if (smallBitmap != bitmap) {
            smallBitmap.recycle()
        }
        
        return pixelatedBitmap
    }
    
    // Apply dominant color algorithm
    private fun applyDominantColor(smallBitmap: Bitmap, originalBitmap: Bitmap, scale: Float): Bitmap {
        val result = smallBitmap.copy(smallBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val blockSize = (1f / scale).toInt().coerceAtLeast(1)
        
        for (y in 0 until smallBitmap.height) {
            for (x in 0 until smallBitmap.width) {
                // Sample from original bitmap
                val origX = (x * blockSize).coerceIn(0, originalBitmap.width - 1)
                val origY = (y * blockSize).coerceIn(0, originalBitmap.height - 1)
                
                val dominantColor = getDominantColorInBlock(
                    originalBitmap, 
                    origX, 
                    origY, 
                    blockSize.coerceAtMost(originalBitmap.width - origX),
                    blockSize.coerceAtMost(originalBitmap.height - origY)
                )
                result.setPixel(x, y, dominantColor)
            }
        }
        return result
    }
    
    // Get dominant color in a block
    private fun getDominantColorInBlock(bitmap: Bitmap, startX: Int, startY: Int, width: Int, height: Int): Int {
        val colorMap = mutableMapOf<Int, Int>()
        val tolerance = 30 // Color similarity threshold
        
        for (dy in 0 until height step 2) { // Sample every 2 pixels for performance
            for (dx in 0 until width step 2) {
                val x = (startX + dx).coerceIn(0, bitmap.width - 1)
                val y = (startY + dy).coerceIn(0, bitmap.height - 1)
                val color = bitmap.getPixel(x, y)
                
                // Find similar color in map
                val similarColor = colorMap.keys.find { existingColor ->
                    colorDistance(existingColor, color) < tolerance
                }
                
                if (similarColor != null) {
                    colorMap[similarColor] = colorMap[similarColor]!! + 1
                } else {
                    colorMap[color] = 1
                }
            }
        }
        
        return colorMap.maxByOrNull { it.value }?.key ?: android.graphics.Color.GRAY
    }
    
    // Calculate color distance
    private fun colorDistance(c1: Int, c2: Int): Int {
        val r = android.graphics.Color.red(c1) - android.graphics.Color.red(c2)
        val g = android.graphics.Color.green(c1) - android.graphics.Color.green(c2)
        val b = android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2)
        return kotlin.math.sqrt((r * r + g * g + b * b).toDouble()).toInt()
    }
    
    // Apply posterize effect
    private fun applyPosterize(bitmap: Bitmap, levels: Int): Bitmap {
        val result = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val step = 255 / (levels - 1)
        
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val color = bitmap.getPixel(x, y)
                val posterized = posterizeColor(color, step)
                result.setPixel(x, y, posterized)
            }
        }
        return result
    }
    
    // Posterize a single color
    private fun posterizeColor(color: Int, step: Int): Int {
        val r = (android.graphics.Color.red(color) / step) * step
        val g = (android.graphics.Color.green(color) / step) * step
        val b = (android.graphics.Color.blue(color) / step) * step
        val a = android.graphics.Color.alpha(color)
        return android.graphics.Color.argb(a, r, g, b)
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

// Mosaic intensity control (0-100 UI range mapped to 0.20-0.01 scale)
// Higher intensity = MORE pixelation = SMALLER scale = BIGGER blocks
fun updateMosaicIntensity(intensity: Float) {
    _uiState.update { 
        it.copy(mosaicIntensity = (0.20f - (intensity / 100f * 0.19f)).coerceIn(0.01f, 0.20f))
    }
}

// Update mosaic pattern
fun updateMosaicPattern(pattern: MosaicPattern) {
    _uiState.update { it.copy(mosaicPattern = pattern) }
}

// Apply mosaic preset
fun applyMosaicPreset(preset: MosaicPreset) {
    _uiState.update { 
        it.copy(
            mosaicIntensity = preset.intensity,
            currentStrokeWidth = preset.strokeWidth,
            currentOpacity = preset.opacity
        )
    }
}

// Update mosaic color mode
fun updateMosaicColorMode(colorMode: MosaicColorMode) {
    _uiState.update { it.copy(mosaicColorMode = colorMode) }
}

// Update posterize levels
fun updatePosterizeLevels(levels: Int) {
    _uiState.update { it.copy(posterizeLevels = levels.coerceIn(2, 8)) }
}

// Reset mosaic settings to defaults
fun resetMosaicSettings() {
    _uiState.update { 
        it.copy(
            mosaicIntensity = 0.05f,
            mosaicPattern = MosaicPattern.SQUARE,
            mosaicColorMode = MosaicColorMode.AVERAGE,
            posterizeLevels = 4,
            currentStrokeWidth = 10f,
            currentOpacity = 1f
        )
    }
}

fun setRotationAngle(angle: Float) {
    _uiState.update { it.copy(rotationAngle = angle) }
}
    
    // Undo/Redo Methods
    fun saveToHistory() {
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
            stickerLayers = _uiState.value.stickerLayers, // Ensure stickerLayers are saved
            shapeLayers = _uiState.value.shapeLayers,
            drawActions = _uiState.value.drawActions
        )

        // Check if the new state is identical to the current history state
        if (currentHistoryIndex >= 0 && currentHistoryIndex < historyStack.size) {
            val lastState = historyStack[currentHistoryIndex]
            if (currentState == lastState) {
                return // Skip saving duplicate state
            }
        }
        
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

    // ... (Skipping intermediary methods)

    fun updateDrawColor(color: Int, saveHistory: Boolean = true) {
    _uiState.update { state -> 
        // Apply current opacity to the selected color
        val currentAlpha = (state.currentOpacity * 255).toInt().coerceIn(0, 255)
        val newColor = androidx.core.graphics.ColorUtils.setAlphaComponent(color, currentAlpha)
        
        state.copy(
            currentDrawColor = newColor,
            shapeLayers = if (state.selectedShapeLayerId != null) {
                state.shapeLayers.map { 
                    if (it.id == state.selectedShapeLayerId && !it.isLocked) it.copy(color = newColor) else it
                }
            } else state.shapeLayers
        ) 
    }
    if (saveHistory) saveToHistory()
}

    fun updateStrokeWidth(width: Float, saveHistory: Boolean = true) {
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
        if (saveHistory) saveToHistory()
    }

    // ...

    // Simplified update for properties
    fun updateTextProperty(id: String, saveHistory: Boolean = true, update: (TextLayer) -> TextLayer) {
        _uiState.update {
            it.copy(
                textLayers = it.textLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) update(layer) else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }

    // ...

    fun updateShapeLayer(id: String, saveHistory: Boolean = true, update: (ShapeLayer) -> ShapeLayer) {
        _uiState.update { state ->
            state.copy(
                shapeLayers = state.shapeLayers.map { if (it.id == id) update(it) else it }
            )
        }
        if (saveHistory) saveToHistory()
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
        val currentState = _uiState.value
        // Check if bitmap generation is needed
        val needsBitmapUpdate = currentState.brightness != state.brightness ||
                                currentState.contrast != state.contrast ||
                                currentState.saturation != state.saturation ||
                                currentState.hue != state.hue ||
                                currentState.temperature != state.temperature ||
                                currentState.tint != state.tint ||
                                currentState.activeFilterId != state.activeFilterId 
                                // activeFilterMatrix usually changes with ID, checking ID + standard comparison is enough usually, 
                                // but let's assume if ID is same matrix is likely same or irrelevant if ID null.
        
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
                stickerLayers = state.stickerLayers,
                shapeLayers = state.shapeLayers,
                drawActions = state.drawActions,
                // Reset dialogs
                showTextDialog = false,
                editingTextId = null,
                // Clear selections to prevent ghost references
                selectedTextLayerId = null,
                selectedStickerLayerId = null,
                selectedShapeLayerId = null,
                editingTextLayerId = null,
                showFloatingToolbar = false
            )
        }
        
        if (needsBitmapUpdate) {
            scheduleApply(saveHistory = false)
        }
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
        x: Float = -1f, // Default to -1 to trigger auto-centering
        y: Float = -1f,
        color: Int = android.graphics.Color.WHITE,
        fontSize: Float = 60f, // Larger default for high-res bitmaps
        fontFamily: AppFont = AppFont.DEFAULT,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        alignment: TextAlignment = TextAlignment.LEFT,
        hasOutline: Boolean = false,
        outlineColor: Int = android.graphics.Color.BLACK,
        hasShadow: Boolean = false
    ) {
        // Calculate center of bitmap if no coordinates provided
        // Layers are ALWAYS stored in bitmap coordinates
        val (finalX, finalY) = if (x == -1f || y == -1f) {
            val bitmap = uiState.value.originalBitmap
            if (bitmap != null) {
                bitmap.width / 2f to bitmap.height / 2f
            } else {
                100f to 100f
            }
        } else {
            // Coordinates provided explicitly
            x to y
        }
    
        // Auto-assign z-index to be on top of existing layers
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        val newLayer = TextLayer(
            text = text,
            x = finalX,
            y = finalY,
            color = color,
            fontSize = fontSize, // This is now in bitmap pixels (or relative scale)
            fontFamily = fontFamily,
            isBold = isBold,
            isItalic = isItalic,
            alignment = alignment,
            hasOutline = hasOutline,
            outlineColor = outlineColor,
            hasShadow = hasShadow,
            zIndex = nextZIndex
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
        saveToHistory()
    }

    // Deprecated: Migrating to addDrawAction
    fun addDrawPath(path: DrawPath) {
        addDrawAction(DrawAction.Path(path))
    }

    // Deprecated: Migrating to addDrawAction
    fun addShape(shape: Shape) {
        addDrawAction(DrawAction.Shape(shape))
    }

    // Unified Undo/Redo handles drawing now.
    // drawActions specific undo/redo removed to prevent state desync.

    // New Helpers
fun updateOpacity(opacity: Float) {
    _uiState.update { state ->
        val currentAlpha = (opacity * 255).toInt().coerceIn(0, 255)
        val newColor = androidx.core.graphics.ColorUtils.setAlphaComponent(state.currentDrawColor, currentAlpha)
        state.copy(currentOpacity = opacity, currentDrawColor = newColor)
    }
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
        // Auto-assign z-index to be on top of existing layers
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        // Initialize in Bitmap Coordinates
        val bitmap = _uiState.value.originalBitmap
        val centerX = bitmap?.width?.toFloat()?.div(2f) ?: 500f
        val centerY = bitmap?.height?.toFloat()?.div(2f) ?: 500f
        
        val newLayer = TextLayer(
            text = "",
            x = centerX, 
            y = centerY,
            width = 200f,
            height = 80f,
            zIndex = nextZIndex
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
        // Auto-assign z-index to be on top of existing layers
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        val newLayer = TextLayer(
            text = "",
            x = x,
            y = y,
            width = 200f,
            height = 80f,
            zIndex = nextZIndex
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

    // Preview Mode Toggle
    fun togglePreviewMode() {
        _uiState.update {
            it.copy(
                isPreviewMode = !it.isPreviewMode,
                selectedTextLayerId = if (!it.isPreviewMode) null else it.selectedTextLayerId,
                selectedShapeLayerId = if (!it.isPreviewMode) null else it.selectedShapeLayerId,
                selectedStickerLayerId = if (!it.isPreviewMode) null else it.selectedStickerLayerId
            )
        }
    }

    // Deselect functions for tab switching
    fun deselectText() {
        _uiState.update { it.copy(selectedTextLayerId = null, editingTextLayerId = null) }
    }
    
    fun deselectShape() {
        _uiState.update { it.copy(selectedShapeLayerId = null) }
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
    
    fun updateStickerOpacity(id: String, opacity: Float, saveHistory: Boolean = true) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) {
                        layer.copy(opacity = opacity.coerceIn(0f, 1f))
                    } else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun updateStickerPosition(id: String, x: Float? = null, y: Float? = null, saveHistory: Boolean = true) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) {
                        layer.copy(
                            x = x ?: layer.x,
                            y = y ?: layer.y
                        )
                    } else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun updateStickerScale(id: String, scale: Float, saveHistory: Boolean = true) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) layer.copy(scale = scale.coerceIn(0.1f, 10f))
                    else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun updateStickerRotation(id: String, rotation: Float, saveHistory: Boolean = true) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) layer.copy(rotation = rotation)
                    else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun duplicateSticker(id: String) {
        val originalSticker = _uiState.value.stickerLayers.find { it.id == id } ?: return
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        val duplicatedSticker = originalSticker.copy(
            id = java.util.UUID.randomUUID().toString(),
            x = originalSticker.x + 20f, // Offset slightly
            y = originalSticker.y + 20f,
            zIndex = nextZIndex
        )
        
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers + duplicatedSticker,
                selectedStickerLayerId = duplicatedSticker.id
            )
        }
        saveToHistory()
    }
    
    fun bringStickerToFront(id: String) {
        val maxZIndex = getAllLayers().maxOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(id, com.moshitech.workmate.feature.imagestudio.data.LayerType.STICKER, maxZIndex + 1)
    }
    
    fun sendStickerToBack(id: String) {
        val minZIndex = getAllLayers().minOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(id, com.moshitech.workmate.feature.imagestudio.data.LayerType.STICKER, minZIndex - 1)
    }
    
    fun updateStickerShadow(
        id: String,
        hasShadow: Boolean? = null,
        shadowColor: Int? = null,
        shadowBlur: Float? = null,
        shadowOffsetX: Float? = null,
        shadowOffsetY: Float? = null,
        saveHistory: Boolean = true
    ) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) {
                        layer.copy(
                            hasShadow = hasShadow ?: layer.hasShadow,
                            shadowColor = shadowColor ?: layer.shadowColor,
                            shadowBlur = shadowBlur ?: layer.shadowBlur,
                            shadowOffsetX = shadowOffsetX ?: layer.shadowOffsetX,
                            shadowOffsetY = shadowOffsetY ?: layer.shadowOffsetY
                        )
                    } else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun updateStickerBorder(
        id: String,
        hasBorder: Boolean? = null,
        borderColor: Int? = null,
        borderWidth: Float? = null,
        saveHistory: Boolean = true
    ) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) {
                        layer.copy(
                            hasBorder = hasBorder ?: layer.hasBorder,
                            borderColor = borderColor ?: layer.borderColor,
                            borderWidth = borderWidth ?: layer.borderWidth
                        )
                    } else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
    }
    
    fun updateStickerTint(
        id: String,
        hasTint: Boolean? = null,
        tintColor: Int? = null,
        tintStrength: Float? = null,
        saveHistory: Boolean = true
    ) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id) {
                        layer.copy(
                            hasTint = hasTint ?: layer.hasTint,
                            tintColor = tintColor ?: layer.tintColor,
                            tintStrength = tintStrength ?: layer.tintStrength
                        )
                    } else layer
                }
            )
        }
        if (saveHistory) saveToHistory()
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
    
    fun updateTextLayerWidth(id: String, newWidth: Float) {
         _uiState.update { state ->
             val updatedLayers = state.textLayers.map { layer ->
                 if (layer.id == id) {
                     layer.copy(width = newWidth)
                 } else {
                     layer
                 }
             }
             state.copy(textLayers = updatedLayers)
         }
         // saveToHistory() // We might not want to save on every drag frame?
         // But the UI calls it on 'onWidthChange'.
         // The UI calls `onTransformEnd` which calls `saveToHistory`.
         // `onWidthChange` is called DURING drag.
         // So we should NOT call saveToHistory here if we want to avoid flooding history stacks
         // OR if onWidthChange is only called on end?
         // In TextBoxComposable, I implemented `detectDragGestures`.
         // It calls `onWidthChange` on every drag event.
         // So WE SHOULD NOT call `saveToHistory` inside this function.
         // `onTransformEnd` is called at the end of the gesture. 
         // `PhotoEditorScreen` hooks `onTransformEnd` -> `viewModel.saveToHistory()`.
         // So this is correct: Just update state.
    }
    


    // Sticker Methods
    fun addSticker(resId: Int = 0, text: String? = null) {
        // Auto-assign z-index to be on top of existing layers
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        // Initialize in Bitmap Coordinates (layers are always in bitmap space)
        val bitmap = _uiState.value.originalBitmap
        val centerX = bitmap?.width?.toFloat()?.div(2f) ?: 500f
        val centerY = bitmap?.height?.toFloat()?.div(2f) ?: 500f

        val newSticker = StickerLayer(
            resId = resId,
            text = text,
            x = centerX, 
            y = centerY, 
            zIndex = nextZIndex
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
    // Use ORIGINAL bitmap to define initial size/position in the correct coordinate space
    val bitmap = _uiState.value.originalBitmap ?: _uiState.value.previewBitmap ?: return
    val bmpW = bitmap.width.toFloat()
    val bmpH = bitmap.height.toFloat()
    
    // Auto-assign z-index to be on top of existing layers
    val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
    
    // Size relative to image, e.g., 1/4th of the smaller dimension
    val shapeSize = minOf(bmpW, bmpH) / 3f
    val width = if (type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize * 0.8f else shapeSize
    val height = if (type == ShapeType.LINE || type == ShapeType.ARROW) 20f else shapeSize // Initial height for line/arrow is stroke-ish or small box? No, Shape.Line has start/end. ShapeLayer has w/h.
    
    // For Line/Arrow, ShapeLayer wrapper width/height defines the bounding box? 
    // Wait, the Shape types (Rect, Circle) use `width` and `height` from ShapeLayer props in `renderShapeLayersOnBitmap`.
    // But `Shape.Line` uses start/end. 
    // Let's verify how `ShapeLayer` maps to specific Shape classes in `addShapeAction` vs `ShapeLayer` list.
    // Actually `ShapeLayer` is a data class for "Vector Layers that are movable".
    // Let's check `ShapeBoxComposable`. It uses `layer.width` and `layer.height`.
    
    // Center position
    val centerX = bmpW / 2f
    val centerY = bmpH / 2f
    val x = centerX - width / 2f
    val y = centerY - (if(type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize/2f else height/2f) // Box centered.

    val newShape = ShapeLayer(
        type = type,
        x = x, 
        y = y, 
        width = width, 
        height = if(type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize else height, // ensure box is square-ish for rotation context or appropriate
        color = _uiState.value.currentDrawColor,
        strokeWidth = _uiState.value.currentStrokeWidth,
        strokeStyle = _uiState.value.currentStrokeStyle,
        shadowColor = _uiState.value.currentShadowColor,
        shadowBlur = _uiState.value.currentShadowBlur,
        shadowX = _uiState.value.currentShadowX,
        shadowY = _uiState.value.currentShadowY,
        zIndex = nextZIndex
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
    



    fun updateShapeShadow(id: String, hasShadow: Boolean, color: Int, blur: Float, x: Float, y: Float, saveHistory: Boolean = true) {
        updateShapeLayer(id, saveHistory) {
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
    
    fun updateShapeOpacity(id: String, opacity: Float, saveHistory: Boolean = true) {
        updateShapeLayer(id, saveHistory) { it.copy(opacity = opacity.coerceIn(0f, 1f)) }
    }
    
    fun updateShapePosition(id: String, x: Float? = null, y: Float? = null, saveHistory: Boolean = true) {
        updateShapeLayer(id, saveHistory) { layer ->
            layer.copy(
                x = x ?: layer.x,
                y = y ?: layer.y
            )
        }
    }
    
    fun duplicateShape(id: String) {
        val original = _uiState.value.shapeLayers.find { it.id == id } ?: return
        val maxZIndex = _uiState.value.shapeLayers.maxOfOrNull { it.zIndex } ?: 0
        
        val duplicate = original.copy(
            id = java.util.UUID.randomUUID().toString(),
            x = original.x + 20f, // Offset slightly
            y = original.y + 20f,
            zIndex = maxZIndex + 1
        )
        
        _uiState.update { state ->
            state.copy(
                shapeLayers = state.shapeLayers + duplicate,
                selectedShapeLayerId = duplicate.id // Auto-select the duplicate
            )
        }
        saveToHistory()
    }
    
    fun bringShapeToFront(id: String) {
        val maxZIndex = _uiState.value.shapeLayers.maxOfOrNull { it.zIndex } ?: 0
        updateShapeLayer(id) { it.copy(zIndex = maxZIndex + 1) }
    }
    
    fun sendShapeToBack(id: String) {
        val minZIndex = _uiState.value.shapeLayers.minOfOrNull { it.zIndex } ?: 0
        updateShapeLayer(id) { it.copy(zIndex = minZIndex - 1) }
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



    private fun renderShapeLayersOnBitmap(originalBitmap: Bitmap, shapeLayers: List<ShapeLayer>): Bitmap {
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        shapeLayers.forEach { layer ->
            canvas.save()

            // Transform Canvas: Translate to Center, Rotate, Scale, Translate back to TopLeft
            val cx = layer.x + layer.width / 2f
            val cy = layer.y + layer.height / 2f
            
            canvas.translate(cx, cy)
            canvas.rotate(layer.rotation) // Degrees
            canvas.scale(layer.scale, layer.scale)
            canvas.translate(-layer.width / 2f, -layer.height / 2f)

            // Setup Paint
            paint.color = layer.color
            paint.strokeWidth = layer.strokeWidth * 1f // Scale is applied to canvas
            paint.style = if (layer.isFilled) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
            paint.strokeCap = android.graphics.Paint.Cap.ROUND
            paint.strokeJoin = android.graphics.Paint.Join.ROUND

            if (layer.hasShadow) {
                paint.setShadowLayer(layer.shadowBlur, layer.shadowX, layer.shadowY, layer.shadowColor)
            } else {
                paint.clearShadowLayer()
            }

            when (layer.strokeStyle) {
                StrokeStyle.DASHED -> paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 15f), 0f)
                StrokeStyle.DOTTED -> paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 10f), 0f)
                StrokeStyle.LONG_DASH -> paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(50f, 20f), 0f)
                StrokeStyle.DASH_DOT -> paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 15f, 5f, 15f), 0f)
                StrokeStyle.SOLID -> paint.pathEffect = null
            }

            val w = layer.width
            val h = layer.height

            when (layer.type) {
                ShapeType.RECTANGLE -> {
                    canvas.drawRect(0f, 0f, w, h, paint)
                }
                ShapeType.CIRCLE -> {
                    canvas.drawOval(0f, 0f, w, h, paint)
                }
                ShapeType.LINE -> {
                    val styleBackup = paint.style
                    paint.style = android.graphics.Paint.Style.STROKE
                    canvas.drawLine(0f, h/2f, w, h/2f, paint)
                    paint.style = styleBackup
                }
                ShapeType.ARROW -> {
                     val styleBackup = paint.style
                     paint.style = android.graphics.Paint.Style.STROKE
                     canvas.drawLine(0f, h/2f, w, h/2f, paint)
                     
                     // Arrow Head
                     val headSize = layer.strokeWidth * 3f + 10f
                     val path = android.graphics.Path()
                     path.moveTo(w, h/2f)
                     path.lineTo(w - headSize, h/2f - headSize / 1.5f)
                     path.lineTo(w - headSize, h/2f + headSize / 1.5f)
                     path.close()
                     
                     paint.style = android.graphics.Paint.Style.FILL
                     canvas.drawPath(path, paint)
                     paint.style = styleBackup
                }
                ShapeType.TRIANGLE -> {
                    val path = android.graphics.Path()
                    path.moveTo(w/2f, 0f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h)
                    path.close()
                    canvas.drawPath(path, paint)
                }
                ShapeType.PENTAGON -> {
                    val path = createPolygonPath(5, w, h)
                    canvas.drawPath(path, paint)
                }
                ShapeType.STAR -> {
                     val path = createStarPath(5, w, h)
                     canvas.drawPath(path, paint)
                }
            }

            canvas.restore()
        }
        return bitmap
    }
    
    private fun createPolygonPath(sides: Int, width: Float, height: Float): android.graphics.Path {
        val path = android.graphics.Path()
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

    private fun createStarPath(points: Int, width: Float, height: Float, innerRatio: Float = 0.4f): android.graphics.Path {
        val path = android.graphics.Path()
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
    
    // ========== NEW: Crop Management Functions ==========
    
    fun setCropRect(rect: android.graphics.Rect) {
        _uiState.update { it.copy(cropRect = rect) }
    }
    
    fun resetCrop() {
        _uiState.update { it.copy(cropRect = null) }
    }
    
    fun applyCrop() {
        val cropRect = uiState.value.cropRect ?: return
        val bitmap = uiState.value.originalBitmap ?: return
        
        viewModelScope.launch {
            try {
                val currentState = getCurrentEditorState()
                
                val croppedBitmap = compositeRenderer.renderComposite(
                    baseImage = bitmap,
                    state = currentState,
                    cropRect = cropRect
                )
                
                _uiState.update {
                    it.copy(
                        originalBitmap = croppedBitmap,
                        cropRect = null,
                        isCropApplied = true,
                        textLayers = emptyList(),
                        shapeLayers = emptyList(),
                        stickerLayers = emptyList(),
                        drawActions = emptyList(),
                        hasUnappliedLayers = false
                    )
                }
                
                trackOperation(com.moshitech.workmate.feature.imagestudio.data.EditOperation.CropApplied(cropRect))
                saveToHistory()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(message = "Failed to apply crop: ${e.message}") }
            }
        }
    }

    fun prepareImageForCropping(onReady: (android.net.Uri) -> Unit) {
        val bitmap = uiState.value.originalBitmap ?: return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Render Composite (flattens layers + applies adjustments)
                val currentState = getCurrentEditorState()
                
                val composite = compositeRenderer.renderComposite(
                    baseImage = bitmap,
                    state = currentState,
                    cropRect = null
                )
                
                // 2. Save to Temp File
                val context = getApplication<android.app.Application>()
                val cacheDir = context.cacheDir
                val file = java.io.File(cacheDir, "crop_temp_${System.currentTimeMillis()}.png")
                val stream = java.io.FileOutputStream(file)
                composite.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                
                // 3. Return URI via callback
                val uri = android.net.Uri.fromFile(file)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onReady(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(message = "Failed to prepare for cropping: ${e.message}") }
            }
        }
    }
    
    // ========== NEW: Layer Application Functions ==========
    
    fun applyAllLayers() {
        val bitmap = uiState.value.originalBitmap ?: return
        
        viewModelScope.launch {
            try {
                val currentState = getCurrentEditorState()
                
                val composite = compositeRenderer.renderComposite(bitmap, currentState, null)
                
                _uiState.update {
                    it.copy(
                        originalBitmap = composite,
                        textLayers = emptyList(),
                        shapeLayers = emptyList(),
                        stickerLayers = emptyList(),
                        drawActions = emptyList(),
                        hasUnappliedLayers = false
                    )
                }
                
                trackOperation(com.moshitech.workmate.feature.imagestudio.data.EditOperation.LayersFlattened())
                saveToHistory()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(message = "Failed to apply layers: ${e.message}") }
            }
        }
    }
    
    // ========== NEW: Edit History Tracking ==========
    
    private fun trackOperation(operation: com.moshitech.workmate.feature.imagestudio.data.EditOperation) {
        _uiState.update { state ->
            val newHistory = (state.editHistory + operation).takeLast(state.maxHistorySize)
            state.copy(editHistory = newHistory)
        }
    }
    
    // ========== NEW: Preference Management ==========
    
    fun setLayerApplicationMode(mode: com.moshitech.workmate.feature.imagestudio.data.EditorPreferences.LayerApplicationMode) {
        viewModelScope.launch {
            editorPreferences.setLayerApplicationMode(mode)
        }
    }
    
    fun setShowCropConfirmation(show: Boolean) {
        viewModelScope.launch {
            editorPreferences.setShowCropConfirmation(show)
        }
    }
    
    val layerApplicationMode = editorPreferences.layerApplicationMode
    val showCropConfirmation = editorPreferences.showCropConfirmation
    val hasSeenLayerPreferenceDialog = editorPreferences.hasSeenLayerPreferenceDialog
    
    // ========== Helper Functions ==========
    
    private fun getCurrentEditorState(): EditorState {
        val state = uiState.value
        return EditorState(
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            hue = state.hue,
            temperature = state.temperature,
            tint = state.tint,
            rotationAngle = state.rotationAngle,
            flipX = state.flipX,
            flipY = state.flipY,
            activeFilterId = state.activeFilterId,
            activeFilterMatrix = state.activeFilterMatrix,
            textLayers = state.textLayers,
            stickerLayers = state.stickerLayers,
            shapeLayers = state.shapeLayers,
            drawActions = state.drawActions
        )
    }
    
    /**
     * Transforms an EditorState from UI coordinates to bitmap coordinates
     * This is needed before passing to CompositeRenderer for saving
     * 
     * UI coordinates are affected by:
     * - Image scaling (fit-to-screen)
     * - Image offset (centering in container)
     * 
     * Bitmap coordinates are the original image pixel coordinates
     */
    private fun transformEditorStateToBitmapCoords(state: EditorState): EditorState {
        val scale = getBitmapScale()
        val (offsetX, offsetY) = getBitmapOffset()
        
        return state.copy(
            textLayers = state.textLayers.map { transformTextLayer(it, scale, offsetX, offsetY) },
            stickerLayers = state.stickerLayers.map { transformStickerLayer(it, scale, offsetX, offsetY) },
            shapeLayers = state.shapeLayers.map { transformShapeLayer(it, scale, offsetX, offsetY) },
            drawActions = state.drawActions.map { transformDrawAction(it, scale, offsetX, offsetY) }
        )
    }
    
    private fun transformTextLayer(layer: TextLayer, scale: Float, offsetX: Float, offsetY: Float): TextLayer {
        return layer.copy(
            x = (layer.x - offsetX) / scale,
            y = (layer.y - offsetY) / scale
        )
    }
    
    private fun transformStickerLayer(layer: StickerLayer, scale: Float, offsetX: Float, offsetY: Float): StickerLayer {
        return layer.copy(
            x = (layer.x - offsetX) / scale,
            y = (layer.y - offsetY) / scale
        )
    }
    
    private fun transformShapeLayer(layer: ShapeLayer, scale: Float, offsetX: Float, offsetY: Float): ShapeLayer {
        return layer.copy(
            x = (layer.x - offsetX) / scale,
            y = (layer.y - offsetY) / scale
        )
    }
    
    private fun transformDrawAction(action: DrawAction, scale: Float, offsetX: Float, offsetY: Float): DrawAction {
        return when (action) {
            is DrawAction.Path -> {
                val transformedPoints = action.path.points.map { point ->
                    androidx.compose.ui.geometry.Offset(
                        x = (point.x - offsetX) / scale,
                        y = (point.y - offsetY) / scale
                    )
                }
                DrawAction.Path(action.path.copy(points = transformedPoints))
            }
            is DrawAction.Shape -> {
                val transformedShape = when (val shape = action.shape) {
                    is Shape.Line -> shape.copy(
                        start = androidx.compose.ui.geometry.Offset(
                            x = (shape.start.x - offsetX) / scale,
                            y = (shape.start.y - offsetY) / scale
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            x = (shape.end.x - offsetX) / scale,
                            y = (shape.end.y - offsetY) / scale
                        )
                    )
                    is Shape.Rectangle -> shape.copy(
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = (shape.topLeft.x - offsetX) / scale,
                            y = (shape.topLeft.y - offsetY) / scale
                        )
                    )
                    is Shape.Circle -> shape.copy(
                        center = androidx.compose.ui.geometry.Offset(
                            x = (shape.center.x - offsetX) / scale,
                            y = (shape.center.y - offsetY) / scale
                        )
                    )
                }
                DrawAction.Shape(transformedShape)
            }
        }
    }
    
    // ========== NEW: Layer Management Functions (Phase 2) ==========
    
    fun toggleLayerVisibility(layerId: String, layerType: com.moshitech.workmate.feature.imagestudio.data.LayerType) {
        _uiState.update { state ->
            when (layerType) {
                com.moshitech.workmate.feature.imagestudio.data.LayerType.TEXT -> state.copy(
                    textLayers = state.textLayers.map {
                        if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.STICKER -> state.copy(
                    stickerLayers = state.stickerLayers.map {
                        if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.SHAPE -> state.copy(
                    shapeLayers = state.shapeLayers.map {
                        if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                    }
                )
                else -> state
            }
        }
        saveToHistory()
    }
    
    fun updateLayerZIndex(layerId: String, layerType: com.moshitech.workmate.feature.imagestudio.data.LayerType, newZIndex: Int) {
        _uiState.update { state ->
            when (layerType) {
                com.moshitech.workmate.feature.imagestudio.data.LayerType.TEXT -> state.copy(
                    textLayers = state.textLayers.map {
                        if (it.id == layerId) it.copy(zIndex = newZIndex) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.STICKER -> state.copy(
                    stickerLayers = state.stickerLayers.map {
                        if (it.id == layerId) it.copy(zIndex = newZIndex) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.SHAPE -> state.copy(
                    shapeLayers = state.shapeLayers.map {
                        if (it.id == layerId) it.copy(zIndex = newZIndex) else it
                    }
                )
                else -> state
            }
        }
        saveToHistory()
    }
    
    fun renameLayer(layerId: String, layerType: com.moshitech.workmate.feature.imagestudio.data.LayerType, newName: String) {
        _uiState.update { state ->
            when (layerType) {
                com.moshitech.workmate.feature.imagestudio.data.LayerType.TEXT -> state.copy(
                    textLayers = state.textLayers.map {
                        if (it.id == layerId) it.copy(layerName = newName) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.STICKER -> state.copy(
                    stickerLayers = state.stickerLayers.map {
                        if (it.id == layerId) it.copy(layerName = newName) else it
                    }
                )
                com.moshitech.workmate.feature.imagestudio.data.LayerType.SHAPE -> state.copy(
                    shapeLayers = state.shapeLayers.map {
                        if (it.id == layerId) it.copy(layerName = newName) else it
                    }
                )
                else -> state
            }
        }
    }
    
    fun getAllLayers(): List<com.moshitech.workmate.feature.imagestudio.data.Layer> {
        val state = uiState.value
        return buildList {
            addAll(state.textLayers.map { com.moshitech.workmate.feature.imagestudio.data.Layer.Text(it) })
            addAll(state.stickerLayers.map { com.moshitech.workmate.feature.imagestudio.data.Layer.Sticker(it) })
            addAll(state.shapeLayers.map { com.moshitech.workmate.feature.imagestudio.data.Layer.Shape(it) })
        }
    }
    
    fun bringToFront(layerId: String, layerType: com.moshitech.workmate.feature.imagestudio.data.LayerType) {
        val maxZIndex = getAllLayers().maxOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(layerId, layerType, maxZIndex + 1)
    }
    
    fun sendToBack(layerId: String, layerType: com.moshitech.workmate.feature.imagestudio.data.LayerType) {
        val minZIndex = getAllLayers().minOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(layerId, layerType, minZIndex - 1)
    }
    
    /**
     * Swap z-indices between two layers for drag-and-drop reordering
     */
    fun swapLayerZIndices(draggedLayerId: String, targetLayerId: String) {
        val allLayers = getAllLayers()
        val draggedLayer = allLayers.find { it.id == draggedLayerId } ?: return
        val targetLayer = allLayers.find { it.id == targetLayerId } ?: return
        
        val draggedZIndex = draggedLayer.zIndex
        val targetZIndex = targetLayer.zIndex
        
        // Swap the z-indices
        updateLayerZIndex(draggedLayerId, draggedLayer.type, targetZIndex)
        updateLayerZIndex(targetLayerId, targetLayer.type, draggedZIndex)
    }
}


