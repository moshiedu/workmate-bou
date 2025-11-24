package com.moshitech.workmate.feature.photoconversion.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryEntity
import com.moshitech.workmate.feature.photoconversion.repository.CompressFormat
import com.moshitech.workmate.feature.photoconversion.repository.ConversionSettings
import com.moshitech.workmate.feature.photoconversion.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhotoConversionUiState(
    val selectedImages: List<Uri> = emptyList(),
    val format: CompressFormat = CompressFormat.JPEG,
    val quality: Int = 80,
    val width: String = "",
    val height: String = "",
    val maintainAspectRatio: Boolean = true,
    val isConverting: Boolean = false,
    val conversionMessage: String? = null
)

class PhotoConversionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ImageRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ImageRepository(application, database.conversionHistoryDao())
    }

    private val _uiState = MutableStateFlow(PhotoConversionUiState())
    val uiState: StateFlow<PhotoConversionUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<ConversionHistoryEntity>> = repository.getHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onImagesSelected(uris: List<Uri>) {
        _uiState.update { it.copy(selectedImages = it.selectedImages + uris) }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
    }

    fun updateImage(oldUri: Uri, newUri: Uri) {
        _uiState.update { state ->
            val newImages = state.selectedImages.map { if (it == oldUri) newUri else it }
            state.copy(selectedImages = newImages)
        }
    }

    fun updateFormat(format: CompressFormat) {
        _uiState.update { it.copy(format = format) }
    }

    fun updateQuality(quality: Int) {
        _uiState.update { it.copy(quality = quality) }
    }

    fun updateWidth(width: String) {
        _uiState.update { it.copy(width = width) }
        // Logic to update height if aspect ratio is maintained could be added here if we had original dimensions
    }

    fun updateHeight(height: String) {
        _uiState.update { it.copy(height = height) }
    }

    fun toggleAspectRatio() {
        _uiState.update { it.copy(maintainAspectRatio = !it.maintainAspectRatio) }
    }

    fun convertImages() {
        val state = _uiState.value
        if (state.selectedImages.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true, conversionMessage = null) }
            
            val widthInt = state.width.toIntOrNull()
            val heightInt = state.height.toIntOrNull()

            val settings = ConversionSettings(
                format = state.format,
                quality = state.quality,
                width = widthInt,
                height = heightInt,
                maintainAspectRatio = state.maintainAspectRatio
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
                    selectedImages = emptyList() // Clear selection after conversion
                ) 
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(conversionMessage = null) }
    }

    fun deleteHistoryItem(item: ConversionHistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistoryItem(item)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
