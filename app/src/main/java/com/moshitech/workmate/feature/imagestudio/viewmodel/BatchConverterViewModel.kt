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
    
    data class ImageDetails(
        val name: String,
        val path: String,
        val size: String,
        val resolution: String,
        val type: String
    )
    
    suspend fun getImageDetails(uri: Uri): ImageDetails {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            var name = "Unknown"
            val path = uri.path ?: "Unknown"
            var sizeBytes: Long = -1
            var resolution = "Unknown"
            var type = "Unknown"
            
            try {
                // Try querying MediaStore
                try {
                    getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DISPLAY_NAME)
                            if (nameIndex != -1) name = cursor.getString(nameIndex) ?: "Unknown"
                            
                            val sizeIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.SIZE)
                            if (sizeIndex != -1) {
                                sizeBytes = cursor.getLong(sizeIndex)
                            }
                            
                            val mimeIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.MIME_TYPE)
                            if (mimeIndex != -1) type = cursor.getString(mimeIndex) ?: "Unknown"
                            
                            val widthIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.WIDTH)
                            val heightIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.HEIGHT)
                            if (widthIndex != -1 && heightIndex != -1) {
                                 val w = cursor.getInt(widthIndex)
                                 val h = cursor.getInt(heightIndex)
                                 if (w > 0 && h > 0) resolution = "$w x $h"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore query failure, proceed to fallbacks
                }

                // Fallbacks if data missing
                if (sizeBytes <= 0) {
                    try {
                        getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                            sizeBytes = pfd.statSize
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Fallback for resolution if still unknown
                if (resolution == "Unknown" || resolution.startsWith("0 x")) {
                     try {
                         val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                         getApplication<Application>().contentResolver.openInputStream(uri)?.use { 
                             android.graphics.BitmapFactory.decodeStream(it, null, options) 
                         }
                         if (options.outWidth > 0 && options.outHeight > 0) {
                             resolution = "${options.outWidth} x ${options.outHeight}"
                             if (type == "Unknown" && options.outMimeType != null) type = options.outMimeType
                         }
                     } catch (e: Exception) {}
                }
                
                // Name fallback (filename from path)
                if (name == "Unknown") {
                    name = uri.lastPathSegment ?: "Image"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val sizeString = if (sizeBytes > 0) formatFileSize(sizeBytes) else "Unknown"
            ImageDetails(name, path, sizeString, resolution, type)
        }
    }
    
    private fun formatFileSize(size: Long): String {
        val mb = size / (1024.0 * 1024.0)
        if (mb >= 1.0) return String.format("%.2f MB", mb)
        val kb = size / 1024.0
        return String.format("%.2f KB", kb)
    }
}
