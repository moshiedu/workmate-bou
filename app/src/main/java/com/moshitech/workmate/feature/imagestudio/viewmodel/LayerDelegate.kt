package com.moshitech.workmate.feature.imagestudio.viewmodel

import androidx.compose.ui.geometry.Offset
import com.moshitech.workmate.feature.imagestudio.data.Layer
import com.moshitech.workmate.feature.imagestudio.data.LayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class LayerDelegate(
    private val _uiState: MutableStateFlow<PhotoEditorUiState>,
    private val onApplyNeeded: (Boolean) -> Unit
) {

    // Helper to get all layers for Z-index calculation
    fun getAllLayers(): List<Layer> {
        val state = _uiState.value
        return buildList {
            addAll(state.textLayers.map { Layer.Text(it) })
            addAll(state.stickerLayers.map { Layer.Sticker(it) })
            addAll(state.shapeLayers.map { Layer.Shape(it) })
        }
    }

    // ================= TEXT LAYERS =================
    
    fun addTextLayer(text: String, x: Float, y: Float, color: Int) {
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        val newLayer = TextLayer(
            text = text,
            x = x,
            y = y,
            color = color,
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
        onApplyNeeded(true)
    }

    fun updateTextProperty(id: String, saveHistory: Boolean = true, update: (TextLayer) -> TextLayer) {
        _uiState.update { state ->
            val updatedLayers = state.textLayers.map { layer ->
                if (layer.id == id) update(layer) else layer
            }
            state.copy(textLayers = updatedLayers)
        }
        if (saveHistory) onApplyNeeded(true)
    }

    fun duplicateTextLayer(id: String) {
        val layerToDuplicate = _uiState.value.textLayers.find { it.id == id } ?: return
        val newLayer = layerToDuplicate.copy(
            id = UUID.randomUUID().toString(),
            x = layerToDuplicate.x + 40f,
            y = layerToDuplicate.y + 40f
        )
        _uiState.update { it.copy(
            textLayers = it.textLayers + newLayer,
            selectedTextLayerId = newLayer.id
        ) }
        onApplyNeeded(true)
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
        onApplyNeeded(true)
    }
    
    fun deselectText() {
        _uiState.update { it.copy(selectedTextLayerId = null, editingTextLayerId = null, showFloatingToolbar = false) }
    }

    // ================= STICKER LAYERS =================

    fun addSticker(resId: Int = 0, text: String? = null): String {
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
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
            selectedTextLayerId = null
        ) }
        onApplyNeeded(true)
        return newSticker.id
    }

    fun removeSticker(id: String) {
        _uiState.update { it.copy(
            stickerLayers = it.stickerLayers.filter { layer -> layer.id != id },
            selectedStickerLayerId = null
        ) }
        onApplyNeeded(true)
    }
    
    fun selectSticker(id: String) {
         _uiState.update { it.copy(
            selectedStickerLayerId = id,
            selectedTextLayerId = null,
            editingTextLayerId = null
        ) }
    }
    
    fun deselectSticker() {
        _uiState.update { it.copy(selectedStickerLayerId = null) }
    }
    
    fun flipSticker(id: String) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(isFlipped = !layer.isFlipped)
                    else layer
                }
            )
        }
    }

    fun updateStickerOpacity(id: String, opacity: Float) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(opacity = opacity)
                    else layer
                }
            )
        }
        onApplyNeeded(true)
    }

    fun updateStickerBlendMode(id: String, blendMode: androidx.compose.ui.graphics.BlendMode) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(blendMode = blendMode)
                    else layer
                }
            )
        }
        onApplyNeeded(true)
    }

    fun updateStickerShadow(id: String, hasShadow: Boolean, color: Int, blur: Float, offsetX: Float, offsetY: Float) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(
                        hasShadow = hasShadow,
                        shadowColor = color,
                        shadowBlur = blur,
                        shadowOffsetX = offsetX,
                        shadowOffsetY = offsetY
                    )
                    else layer
                }
            )
        }
        onApplyNeeded(true)
    }

    fun updateStickerBorder(id: String, hasBorder: Boolean, color: Int, width: Float) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) layer.copy(
                        hasBorder = hasBorder,
                        borderColor = color,
                        borderWidth = width
                    )
                    else layer
                }
            )
        }
        onApplyNeeded(true)
    }

    fun updateStickerTransform(id: String, pan: androidx.compose.ui.geometry.Offset, scaleXChange: Float, scaleYChange: Float, rotation: Float) {
        _uiState.update { state ->
            state.copy(
                stickerLayers = state.stickerLayers.map { layer ->
                    if (layer.id == id && !layer.isLocked) {
                        layer.copy(
                            x = layer.x + pan.x,
                            y = layer.y + pan.y,
                            scaleX = layer.scaleX * scaleXChange,
                            scaleY = layer.scaleY * scaleYChange,
                            rotation = layer.rotation + rotation
                        )
                    } else layer
                }
            )
        }
        onApplyNeeded(true)
    }

    // ================= SHAPE LAYERS =================
    
    fun addShapeLayer(type: ShapeType): String {
        val bitmap = _uiState.value.originalBitmap ?: _uiState.value.previewBitmap ?: return ""
        val bmpW = bitmap.width.toFloat()
        val bmpH = bitmap.height.toFloat()
        val nextZIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        
        // PicsArt Style Defaults: Centered, Light Gray, Filled, Shadow
        val shapeSize = minOf(bmpW, bmpH) / 2f
        val width = if (type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize * 0.8f else shapeSize
        val height = if (type == ShapeType.LINE || type == ShapeType.ARROW) 20f else shapeSize
        
        val centerX = bmpW / 2f
        val centerY = bmpH / 2f
        val x = centerX - width / 2f
        val y = centerY - (if(type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize/2f else height/2f)

        val newShape = ShapeLayer(
            type = type,
            x = x, 
            y = y, 
            width = width, 
            height = if(type == ShapeType.LINE || type == ShapeType.ARROW) shapeSize else height,
            color = 0xFFCCCCCC.toInt(), // Light Gray Default
            strokeWidth = 0f, // No stroke by default if filled, or small? PicsArt usually filled.
            borderColor = android.graphics.Color.WHITE, // Default Border Color
            strokeStyle = StrokeStyle.SOLID,
            isFilled = true, // Default Filled
            hasShadow = false, // Default Shadow OFF (User request: "Just solid shape")
            shadowColor = android.graphics.Color.BLACK,
            shadowBlur = 20f,
            shadowX = 5f,
            shadowY = 5f,
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
        android.util.Log.d("ShapeDebug", "addShapeLayer: selectedShapeLayerId = ${newShape.id}, shapeLayers.size = ${_uiState.value.shapeLayers.size}")
        onApplyNeeded(true)
        return newShape.id
    }

    fun updateShapeType(id: String, newType: ShapeType) {
        _uiState.update { state ->
            val layer = state.shapeLayers.find { it.id == id } ?: return@update state
            
            // Recalculate dimensions if switching between Line/Arrow and Box shapes to avoid distortion
            val isLinear = newType == ShapeType.LINE || newType == ShapeType.ARROW
            val wasLinear = layer.type == ShapeType.LINE || layer.type == ShapeType.ARROW
            
            var newW = layer.width
            var newH = layer.height
            
            if (isLinear != wasLinear) {
                // Reset to default aspect if changing major type category
                val bitmap = _uiState.value.originalBitmap
                val size = if (bitmap != null) minOf(bitmap.width.toFloat(), bitmap.height.toFloat()) / 2f else 300f
                if (isLinear) {
                     newW = size * 0.8f
                     newH = 20f
                } else {
                     newW = size
                     newH = size
                }
            }

            state.copy(
                shapeLayers = state.shapeLayers.map { 
                    if (it.id == id) it.copy(type = newType, width = newW, height = newH) else it 
                }
            )
        }
        onApplyNeeded(true)
    }

    fun selectShapeLayer(id: String) {
        _uiState.update { it.copy(
            selectedShapeLayerId = id,
            selectedTextLayerId = null,
            editingTextLayerId = null,
            selectedStickerLayerId = null
        ) }
    }

    fun deselectShape() {
        _uiState.update { it.copy(selectedShapeLayerId = null) }
    }

    fun duplicateShape(id: String): String {
        val shapeToDuplicate = _uiState.value.shapeLayers.find { it.id == id } ?: return ""
        val newShape = shapeToDuplicate.copy(
            id = java.util.UUID.randomUUID().toString(),
            x = shapeToDuplicate.x + 40f,
            y = shapeToDuplicate.y + 40f,
            zIndex = (getAllLayers().maxOfOrNull { it.zIndex } ?: -1) + 1
        )
        _uiState.update { it.copy(
            shapeLayers = it.shapeLayers + newShape,
            selectedShapeLayerId = newShape.id
        ) }
        onApplyNeeded(true)
        return newShape.id
    }

    fun updateShapeLayer(id: String, saveHistory: Boolean = true, update: (ShapeLayer) -> ShapeLayer) {
        _uiState.update { state ->
            state.copy(
                shapeLayers = state.shapeLayers.map { layer ->
                    if (layer.id == id) update(layer) else layer
                }
            )
        }
        if (saveHistory) onApplyNeeded(true)
    }

    // ================= GENERAL LAYER MGMT =================

    fun lockLayer(id: String, locked: Boolean) {
        _uiState.update { state ->
            val text = state.textLayers.map { if (it.id == id) it.copy(isLocked = locked) else it }
            val stickers = state.stickerLayers.map { if (it.id == id) it.copy(isLocked = locked) else it }
            val shapes = state.shapeLayers.map { if (it.id == id) it.copy(isLocked = locked) else it }
            state.copy(textLayers = text, stickerLayers = stickers, shapeLayers = shapes)
        }
        onApplyNeeded(true)
    }

    fun deleteLayer(id: String, type: LayerType) {
         _uiState.update { state ->
            when(type) {
                LayerType.TEXT -> state.copy(textLayers = state.textLayers.filter { it.id != id })
                LayerType.STICKER -> state.copy(stickerLayers = state.stickerLayers.filter { it.id != id })
                LayerType.SHAPE -> state.copy(shapeLayers = state.shapeLayers.filter { it.id != id })
                else -> state
            }
         }
         onApplyNeeded(true)
    }

    fun toggleLayerVisibility(id: String, type: LayerType) {
        _uiState.update { state ->
            when (type) {
                LayerType.TEXT -> state.copy(textLayers = state.textLayers.map { if (it.id == id) it.copy(isVisible = !it.isVisible) else it })
                LayerType.STICKER -> state.copy(stickerLayers = state.stickerLayers.map { if (it.id == id) it.copy(isVisible = !it.isVisible) else it })
                LayerType.SHAPE -> state.copy(shapeLayers = state.shapeLayers.map { if (it.id == id) it.copy(isVisible = !it.isVisible) else it })
                else -> state
            }
        }
        onApplyNeeded(true)
    }

    fun updateLayerZIndex(id: String, type: LayerType, newZIndex: Int) {
        _uiState.update { state ->
            when (type) {
                LayerType.TEXT -> state.copy(textLayers = state.textLayers.map { if (it.id == id) it.copy(zIndex = newZIndex) else it })
                LayerType.STICKER -> state.copy(stickerLayers = state.stickerLayers.map { if (it.id == id) it.copy(zIndex = newZIndex) else it })
                LayerType.SHAPE -> state.copy(shapeLayers = state.shapeLayers.map { if (it.id == id) it.copy(zIndex = newZIndex) else it })
                else -> state
            }
        }
        onApplyNeeded(true)
    }
    
    fun renameLayer(id: String, type: LayerType, newName: String) {
        _uiState.update { state ->
            when (type) {
                LayerType.TEXT -> state.copy(textLayers = state.textLayers.map { if (it.id == id) it.copy(layerName = newName) else it })
                LayerType.STICKER -> state.copy(stickerLayers = state.stickerLayers.map { if (it.id == id) it.copy(layerName = newName) else it })
                LayerType.SHAPE -> state.copy(shapeLayers = state.shapeLayers.map { if (it.id == id) it.copy(layerName = newName) else it })
                else -> state
            }
        }
    }

    fun swapLayerZIndices(draggedId: String, targetId: String) {
        val allLayers = getAllLayers()
        val dragged = allLayers.find { it.id == draggedId } ?: return
        val target = allLayers.find { it.id == targetId } ?: return
        
        val draggedZ = dragged.zIndex
        val targetZ = target.zIndex
        
        updateLayerZIndex(draggedId, dragged.type, targetZ)
        updateLayerZIndex(targetId, target.type, draggedZ)
    }

    fun bringToFront(id: String) {
        val allLayers = getAllLayers()
        val layer = allLayers.find { it.id == id } ?: return
        val maxZIndex = allLayers.maxOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(id, layer.type, maxZIndex + 1)
    }

    fun sendToBack(id: String) {
        val allLayers = getAllLayers()
        val layer = allLayers.find { it.id == id } ?: return
        val minZIndex = allLayers.minOfOrNull { it.zIndex } ?: 0
        updateLayerZIndex(id, layer.type, minZIndex - 1)
    }
    
    private fun minOf(a: Float, b: Float): Float = kotlin.math.min(a, b)
}
