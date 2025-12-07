package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import com.moshitech.workmate.feature.imagestudio.data.ConversionSettings
import com.moshitech.workmate.feature.imagestudio.repository.BatchRepository
import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BatchConverterUiState(
    val selectedImages: List<Uri> = emptyList(),
    val format: CompressFormat = CompressFormat.JPEG,
    val quality: Int = 80,
    val width: String = "",
    val height: String = "",
    val maintainAspectRatio: Boolean = true,
    val targetSize: String = "",
    val isTargetSizeInMb: Boolean = false,
    val isConverting: Boolean = false,
    val conversionMessage: String? = null,
    val message: String? = null
)

class BatchConverterViewModel(application: Application) : AndroidViewModel(application) {

    // Reuse existing repository for now as logic is same
    private val repository: BatchRepository

    init {
        repository = BatchRepository(application)
    }

    private val _uiState = MutableStateFlow(BatchConverterUiState())
    val uiState: StateFlow<BatchConverterUiState> = _uiState.asStateFlow()

    fun onImagesSelected(uris: List<Uri>) {
        val currentCount = _uiState.value.selectedImages.size
        val newCount = currentCount + uris.size
        
        if (MonetizationManager.isBatchLimitReached(newCount)) {
            _uiState.update { it.copy(message = "Free limit reached (${MonetizationManager.FREE_BATCH_LIMIT} images). Upgrade to Pro.") }
            // Add only up to limit if we wanted to be nice, but blocking is simpler
            return
        }
        
        _uiState.update { it.copy(selectedImages = it.selectedImages + uris) }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
    }

    fun updateFormat(format: CompressFormat) {
        _uiState.update { it.copy(format = format) }
    }

    fun updateQuality(quality: Int) {
        // Check Pro Quality Lock
        if (quality > 90 && MonetizationManager.isHighQualitySaveLocked()) {
             _uiState.update { it.copy(message = "High Quality (90%+) is a Pro feature") }
             // Reset to valid max
             _uiState.update { it.copy(quality = 90) }
             return
        }
        _uiState.update { it.copy(quality = quality) }
    }
    
    fun updateWidth(width: String) { 
        _uiState.update { it.copy(width = width) }
        if (_uiState.value.maintainAspectRatio && width.isNotBlank()) {
            val widthInt = width.toIntOrNull()
            if (widthInt != null) {
                // Use 16:9 as default ratio for UI calculation
                val calculatedHeight = (widthInt * 9 / 16).toString()
                _uiState.update { it.copy(height = calculatedHeight) }
            }
        }
    }
    
    fun updateHeight(height: String) { 
        _uiState.update { it.copy(height = height) }
        if (_uiState.value.maintainAspectRatio && height.isNotBlank()) {
            val heightInt = height.toIntOrNull()
            if (heightInt != null) {
                // Use 16:9 as default ratio for UI calculation
                val calculatedWidth = (heightInt * 16 / 9).toString()
                _uiState.update { it.copy(width = calculatedWidth) }
            }
        }
    }
    
    fun updateTargetSize(size: String) { _uiState.update { it.copy(targetSize = size) } }
    fun toggleTargetSizeUnit() { _uiState.update { it.copy(isTargetSizeInMb = !it.isTargetSizeInMb) } }
    fun toggleAspectRatio() { _uiState.update { it.copy(maintainAspectRatio = !it.maintainAspectRatio) } }

    fun convertImages() {
        val state = _uiState.value
        if (state.selectedImages.isEmpty()) return

        // Final Pro Check before action
        if (MonetizationManager.isBatchLimitReached(state.selectedImages.size)) {
             _uiState.update { it.copy(message = "Too many images for Free plan.") }
             return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true, conversionMessage = null) }
            
            val inputSize = state.targetSize.toIntOrNull()
            val targetSizeKb = if (inputSize != null) {
                if (state.isTargetSizeInMb) inputSize * 1024 else inputSize
            } else null

            val settings = ConversionSettings(
                format = state.format,
                quality = state.quality,
                width = if (state.width.isBlank()) null else state.width.toIntOrNull(),
                height = if (state.height.isBlank()) null else state.height.toIntOrNull(),
                maintainAspectRatio = state.maintainAspectRatio,
                targetSizeKB = targetSizeKb
            )

            var successCount = 0
            state.selectedImages.forEach { uri ->
                val result = repository.convertAndSaveImage(uri, settings)
                if (result.isSuccess) successCount++
            }

            _uiState.update { 
                it.copy(
                    isConverting = false, 
                    conversionMessage = "Converted $successCount of ${state.selectedImages.size} images",
                    selectedImages = emptyList() 
                ) 
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null, conversionMessage = null) }
    }
}
