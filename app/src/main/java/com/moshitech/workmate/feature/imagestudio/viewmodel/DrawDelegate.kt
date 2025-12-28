package com.moshitech.workmate.feature.imagestudio.viewmodel

import com.moshitech.workmate.feature.imagestudio.data.LayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DrawDelegate(
    private val _uiState: MutableStateFlow<PhotoEditorUiState>,
    private val onApplyNeeded: (Boolean) -> Unit
) {

    fun setDrawMode(mode: DrawMode) {
        _uiState.update { it.copy(activeDrawMode = mode) }
    }
    
    fun setDrawColor(color: Int) {
        _uiState.update { it.copy(currentDrawColor = color) }
    }
    
    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(currentStrokeWidth = width) }
    }

    fun setStrokeStyle(style: StrokeStyle) {
        _uiState.update { it.copy(currentStrokeStyle = style) }
    }
    
    // Unified Draw Logic
    
    fun addDrawAction(action: DrawAction) {
        _uiState.update { currentState ->
            currentState.copy(
                drawActions = currentState.drawActions + action,
                redoStack = emptyList(), // Clear redo
                canUndo = true,
                canRedo = false
            )
        }
        onApplyNeeded(true)
    }

    fun addDrawPath(path: DrawPath) {
        addDrawAction(DrawAction.Path(path))
    }

    fun addShape(shape: Shape) {
        addDrawAction(DrawAction.Shape(shape))
    }
    
    fun undoLastDrawAction() {
        _uiState.update { state ->
            if (state.drawActions.isNotEmpty()) {
                val lastAction = state.drawActions.last()
                state.copy(
                    drawActions = state.drawActions.dropLast(1),
                    redoStack = state.redoStack + lastAction,
                    canUndo = state.drawActions.size > 1,
                    canRedo = true
                )
            } else state
        }
        onApplyNeeded(true)
    }
    
    fun redoLastDrawAction() {
        _uiState.update { state ->
            if (state.redoStack.isNotEmpty()) {
                val actionToRedo = state.redoStack.last()
                state.copy(
                    drawActions = state.drawActions + actionToRedo,
                    redoStack = state.redoStack.dropLast(1),
                    canUndo = true,
                    canRedo = state.redoStack.size > 1
                )
            } else state
        }
        onApplyNeeded(true)
    }

    fun clearAllDrawings() {
        _uiState.update { it.copy(drawActions = emptyList(), redoStack = emptyList(), canUndo = false, canRedo = false) }
        onApplyNeeded(true)
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
        onApplyNeeded(true)
    }
    
    fun updateOpacity(opacity: Float) {
        _uiState.update { state ->
            val currentAlpha = (opacity * 255).toInt().coerceIn(0, 255)
            val newColor = androidx.core.graphics.ColorUtils.setAlphaComponent(state.currentDrawColor, currentAlpha)
            state.copy(currentOpacity = opacity, currentDrawColor = newColor)
        }
    }
    
    fun selectDrawTool(tool: DrawTool) {
        _uiState.update { it.copy(selectedDrawTool = tool) }
    }
    
    // Mosaic & Posterize Logic
    
    fun resetMosaicSettings() {
        _uiState.update { it.copy(
            mosaicIntensity = 0.05f,
            mosaicPattern = MosaicPattern.SQUARE,
            mosaicColorMode = MosaicColorMode.AVERAGE,
            posterizeLevels = 4
        ) }
    }
    
    fun applyMosaicPreset(preset: MosaicPreset) {
        _uiState.update { it.copy(
            mosaicIntensity = preset.intensity,
            currentStrokeWidth = preset.strokeWidth,
            currentOpacity = preset.opacity
        ) }
        onApplyNeeded(true) // Preset changes drawing params, but not drawn content directly unless active path updates? 
        // Usually presets just set state for NEXT draw action. So maybe false?
        // But if it updates current stroke width/opacity, those don't need history save yet.
        // Let's defer history save for actual draw.
    }
    
    fun updateMosaicIntensity(intensity: Float) {
        _uiState.update { it.copy(mosaicIntensity = intensity) }
    }
    
    fun updateMosaicPattern(pattern: MosaicPattern) {
        _uiState.update { it.copy(mosaicPattern = pattern) }
    }
    
    fun updateMosaicColorMode(mode: MosaicColorMode) {
        _uiState.update { it.copy(mosaicColorMode = mode) }
    }
    
    fun updatePosterizeLevels(levels: Int) {
        _uiState.update { it.copy(posterizeLevels = levels) }
    }
}
