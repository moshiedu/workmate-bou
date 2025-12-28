package com.moshitech.workmate.feature.imagestudio.viewmodel

import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AdjustmentDelegate(
    private val _uiState: MutableStateFlow<PhotoEditorUiState>,
    private val onApplyNeeded: (Boolean) -> Unit
) {

    fun updateBrightness(value: Float) {
        _uiState.update { it.copy(brightness = value) }
        onApplyNeeded(false)
    }

    fun updateContrast(value: Float) {
        _uiState.update { it.copy(contrast = value) }
        onApplyNeeded(false)
    }

    fun updateSaturation(value: Float) {
        _uiState.update { it.copy(saturation = value) }
        onApplyNeeded(false)
    }
    
    fun setHue(hue: Float) {
        _uiState.update { it.copy(hue = hue) }
        onApplyNeeded(false)
    }

    fun setTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = temp) }
        onApplyNeeded(false)
    }

    fun setTint(tint: Float) {
        _uiState.update { it.copy(tint = tint) }
        onApplyNeeded(false)
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
        onApplyNeeded(true)
    }
    
    // Filter Logic
    fun applyFilter(id: String, matrix: FloatArray) {
        if (MonetizationManager.isFilterLocked(id)) {
            _uiState.update { it.copy(message = "This filter is a Pro feature") }
            return
        }
        _uiState.update { it.copy(activeFilterId = id, activeFilterMatrix = matrix) }
        onApplyNeeded(true)
    }
    
    fun clearFilter() {
        _uiState.update { it.copy(activeFilterId = null, activeFilterMatrix = null) }
        onApplyNeeded(true)
    }
    
    // Rotation/Flip
    fun setRotationAngle(angle: Float) {
        _uiState.update { it.copy(rotationAngle = angle) }
        // Rotation usually requires re-render but might not auto-save history on slide
        onApplyNeeded(false) 
    }
    
    fun rotate90CCW() {
        _uiState.update { it.copy(rotationAngle = it.rotationAngle - 90f) }
        onApplyNeeded(true)
    }

    fun rotate90CW() {
        _uiState.update { it.copy(rotationAngle = it.rotationAngle + 90f) }
        onApplyNeeded(true)
    }
    
    fun flipHorizontal() {
         _uiState.update { it.copy(flipX = !it.flipX) }
         onApplyNeeded(true)
    }
    
    fun flipVertical() {
         _uiState.update { it.copy(flipY = !it.flipY) }
         onApplyNeeded(true)
    }
}
