package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.graphics.Bitmap

data class EditorState(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val rotationAngle: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
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
    
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Text Layer"
)

data class StickerLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val resId: Int = 0,
    val uri: String? = null,
    val text: String? = null,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val isFlipped: Boolean = false,
    val isLocked: Boolean = false,
    
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Sticker Layer",
    
    val opacity: Float = 1f,
    
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 8f,
    val shadowOffsetX: Float = 4f,
    val shadowOffsetY: Float = 4f,
    
    val hasBorder: Boolean = false,
    val borderColor: Int = android.graphics.Color.WHITE,
    val borderWidth: Float = 2f,
    
    val hasTint: Boolean = false,
    val tintColor: Int = android.graphics.Color.BLUE,
    val tintStrength: Float = 0.5f
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
    val isSpray: Boolean = false,
    val blurRadius: Float = 0f,
    val blendMode: DrawBlendMode = DrawBlendMode.NORMAL,
    val strokeStyle: StrokeStyle = StrokeStyle.SOLID
)

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

enum class MosaicPattern(val displayName: String) {
    SQUARE("Square"),
    HEXAGONAL("Hexagonal"),
    CIRCULAR("Circular"),
    DIAMOND("Diamond")
}

enum class MosaicColorMode(val displayName: String) {
    AVERAGE("Average"),
    DOMINANT("Dominant"),

    POSTERIZE("Posterize")
}

enum class EditorTab {
    NONE, CROP, ADJUST, FILTERS, STICKERS, STICKER_CONTROLS, SHAPES, ROTATE, TEXT, DRAW
}

data class MosaicPreset(
    val name: String,
    val intensity: Float,
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
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val color: Int,
    val strokeWidth: Float,
    val borderColor: Int = android.graphics.Color.WHITE, // Added Border Color support
    val isFilled: Boolean = false,
    val isLocked: Boolean = false,
    val strokeStyle: StrokeStyle = StrokeStyle.SOLID,
    val opacity: Float = 1f,
    val hasShadow: Boolean = false,
    val shadowColor: Int = android.graphics.Color.BLACK,
    val shadowBlur: Float = 10f,
    val shadowX: Float = 5f,
    val shadowY: Float = 5f,
    
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val layerName: String = "Shape Layer"
)

data class PhotoEditorUiState(
    val originalBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val filterPreviewBitmap: Bitmap? = null,
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
    val drawActions: List<DrawAction> = emptyList(),
    val redoStack: List<DrawAction> = emptyList(),
    
    val currentDrawColor: Int = android.graphics.Color.RED,
    val currentStrokeWidth: Float = 5f,
    val selectedDrawTool: DrawTool = DrawTool.BRUSH,
    val currentOpacity: Float = 1f, 
    val mosaicIntensity: Float = 0.05f,
    val mosaicPattern: MosaicPattern = MosaicPattern.SQUARE,
    val mosaicColorMode: MosaicColorMode = MosaicColorMode.AVERAGE,
    val posterizeLevels: Int = 4,

    val rotationAngle: Float = 0f,
    val flipX: Boolean = false,
    val flipY: Boolean = false,
    val hue: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    // Modal Tool State
    val activeTool: EditorTab? = null,
    val toolSnapshot: EditorState? = null,
    val selectedTextLayerId: String? = null,
    val editingTextLayerId: String? = null,
    val selectedStickerLayerId: String? = null,
    val selectedShapeLayerId: String? = null,
    val showFloatingToolbar: Boolean = false,
    val activeDrawMode: DrawMode = DrawMode.PAINT,
    val currentStrokeStyle: StrokeStyle = StrokeStyle.SOLID,
    val currentShadowColor: Int = android.graphics.Color.BLACK,
    val currentShadowBlur: Float = 10f,
    val currentShadowX: Float = 5f,
    val currentShadowY: Float = 5f,
    val isPreviewMode: Boolean = false,
    
    val cropRect: android.graphics.Rect? = null,
    val originalImageBounds: android.graphics.Rect? = null,
    val isCropApplied: Boolean = false,
    
    val hasUnappliedLayers: Boolean = false,
    
    val editHistory: List<com.moshitech.workmate.feature.imagestudio.data.EditOperation> = emptyList(),
    val maxHistorySize: Int = 50,
    val containerWidth: Int = 1,
    val containerHeight: Int = 1,
    
    // Persisted Tool States
    val lastActiveShapeTab: ShapePropertyTab = ShapePropertyTab.SHAPES
)

enum class ShapePropertyTab {
    SHAPES,
    COLOR,
    BORDER,
    SHADOW,
    OPACITY
}
