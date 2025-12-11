package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import com.moshitech.workmate.feature.imagestudio.data.ConversionSettings
import com.moshitech.workmate.feature.imagestudio.data.ConversionPreset
import com.moshitech.workmate.feature.imagestudio.repository.BatchRepository
import com.moshitech.workmate.feature.imagestudio.util.MonetizationManager
import com.moshitech.workmate.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.isActive

enum class BatchScreenState {
    INPUT,
    SUCCESS,
    DETAIL
}

data class ConvertedImage(
    val uri: Uri,
    val originalUri: Uri,
    val name: String,
    val size: String,
    val resolution: String,
    val type: String,
    val originalSize: String,
    val originalResolution: String,

    val originalType: String,
    val sizeBytes: Long = 0,
    val width: Int = 0,
    val height: Int = 0
)

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
    val message: String? = null,
    val screenState: BatchScreenState = BatchScreenState.INPUT,
    val convertedImages: List<ConvertedImage> = emptyList(),
    val selectedDetailImage: ConvertedImage? = null,
    val savedFolderUri: Uri? = null,
    val lastSavedLocation: Uri? = null,
    val progress: Float = 0f,
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val maxInputWidth: Int = 0,
    val maxInputHeight: Int = 0,
    val showGuide: Boolean = false,
    val currentFileProgress: Float = 0f,
    val keepMetadata: Boolean = false,
    val presets: List<ConversionPreset> = emptyList()
)

class BatchConverterViewModel(application: Application) : AndroidViewModel(application) {
    
    private var conversionJob: kotlinx.coroutines.Job? = null

    // Reuse existing repository for now as logic is same
    private val repository: BatchRepository
    private val preferencesRepository: UserPreferencesRepository

    init {
        repository = BatchRepository(application)
        preferencesRepository = UserPreferencesRepository(application)
        
        viewModelScope.launch {
            preferencesRepository.batchOutputFolder.collect { uriString ->
                 if (!uriString.isNullOrBlank()) {
                     try {
                         val uri = Uri.parse(uriString)
                         _uiState.update { it.copy(savedFolderUri = uri) }
                     } catch (e: Exception) {
                         // Ignore invalid URI
                     }
                 }
            }
        }
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
    recalculateMaxDimensions()
}

fun removeImage(uri: Uri) {
    _uiState.update { it.copy(selectedImages = it.selectedImages - uri) }
    recalculateMaxDimensions()
}

private fun recalculateMaxDimensions() {
    viewModelScope.launch(Dispatchers.IO) {
        var maxW = 0
        var maxH = 0
        _uiState.value.selectedImages.forEach { uri ->
           val details = getImageDetails(uri)
           if (details.width > maxW) maxW = details.width
           if (details.height > maxH) maxH = details.height
        }
        _uiState.update { it.copy(maxInputWidth = maxW, maxInputHeight = maxH) }
    }
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
    fun toggleKeepMetadata() { _uiState.update { it.copy(keepMetadata = !it.keepMetadata) } }
    
    fun savePreset(name: String) {
        val state = _uiState.value
        val preset = ConversionPreset(
            name = name,
            format = state.format,
            quality = state.quality,
            width = state.width,
            height = state.height,
            maintainAspectRatio = state.maintainAspectRatio,
            targetSize = state.targetSize,
            isTargetSizeInMb = state.isTargetSizeInMb,
            keepMetadata = state.keepMetadata
        )
        viewModelScope.launch {
            preferencesRepository.savePreset(preset)
        }
    }
    
    fun loadPreset(preset: ConversionPreset) {
        _uiState.update {
            it.copy(
                format = preset.format,
                quality = preset.quality,
                width = preset.width,
                height = preset.height,
                maintainAspectRatio = preset.maintainAspectRatio,
                targetSize = preset.targetSize,
                isTargetSizeInMb = preset.isTargetSizeInMb,
                keepMetadata = preset.keepMetadata
            )
        }
    }
    
    fun deletePreset(name: String) {
        viewModelScope.launch {
            preferencesRepository.deletePreset(name)
        }
    }

    fun convertImages() {
        val state = _uiState.value
        if (state.selectedImages.isEmpty()) return

        // Final Pro Check before action
        if (MonetizationManager.isBatchLimitReached(state.selectedImages.size)) {
             _uiState.update { it.copy(message = "Too many images for Free plan.") }
             return
        }

        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isConverting = true, 
                    conversionMessage = "Starting...",
                    progress = 0f,
                    processedCount = 0,
                    totalCount = state.selectedImages.size,
                    currentFileProgress = 0f
                ) 
            }
            
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
                targetSizeKB = targetSizeKb,
                keepMetadata = state.keepMetadata
            )



            val results = mutableListOf<ConvertedImage>()
            
            // PDF Logic
            if (state.format == CompressFormat.PDF) {
                 _uiState.update { it.copy(conversionMessage = "Merging into PDF...") }
                 val result = repository.createPdfFromImages(
                     state.selectedImages,
                     settings
                 ) { p ->
                     _uiState.update { it.copy(currentFileProgress = p, progress = p) }
                 }
                 
                 result.onSuccess { uri ->
                     // For PDF, we treat it as 1 "converted image" result
                     var sizeBytes: Long = 0
                     var sizeString = "Unknown"
                     try {
                          getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                              sizeBytes = pfd.statSize
                              sizeString = formatFileSize(sizeBytes)
                          }
                     } catch (e: Exception) { e.printStackTrace() }

                     results.add(ConvertedImage(
                         uri = uri,
                         originalUri = state.selectedImages.firstOrNull() ?: Uri.EMPTY, // Fallback
                         name = "Merged PDF",
                         size = sizeString,
                         resolution = "Multi-Page",
                         type = "application/pdf",
                         originalSize = "Multiple",
                         originalResolution = "Multiple",
                         originalType = "Multiple",
                         sizeBytes = sizeBytes
                     ))
                 }.onFailure { e ->
                     // Handle failure
                 }
                 
                 _uiState.update { 
                    it.copy(
                        isConverting = false, 
                        conversionMessage = "Done!", 
                        progress = 1f, 
                        convertedImages = results,
                        screenState = BatchScreenState.SUCCESS,
                        processedCount = if(result.isSuccess) state.selectedImages.size else 0
                    ) 
                 }
                 return@launch
            }

            // Standard Logic: Loop through images
            var successCount = 0
            
            val totalImages = state.selectedImages.size
            
            state.selectedImages.forEachIndexed { index, uri ->
                 ensureActive() // Check cancellation and throw if cancelled

                 _uiState.update { 
                     it.copy(
                         conversionMessage = "Converting ${index + 1} of $totalImages",
                         processedCount = index + 1
                     ) 
                 }
                 
                // Fetch Original Details First
                val originalDetails = getImageDetails(uri)
                
                // Use original name for destination if possible
                val result = repository.convertAndSaveImage(
                    uri, 
                    settings,
                    onProgress = { p ->
                        _uiState.update { it.copy(currentFileProgress = p) }
                    }
                )
                
                if (result.isSuccess) {
                    successCount++
                    val convertedUri = result.getOrThrow()
                    // Get details of converted image
                    val details = getImageDetails(convertedUri)
                    results.add(ConvertedImage(
                        uri = convertedUri,
                        originalUri = uri,
                        name = "CONVERTED_${originalDetails.name}", // Placeholder, will fix in save
                        size = details.size,
                        resolution = details.resolution,
                        type = details.type,
                        originalSize = originalDetails.size,
                        originalResolution = originalDetails.resolution,
                        originalType = originalDetails.type,
                        sizeBytes = details.sizeBytes,
                        width = details.width,
                        height = details.height
                    ))
                }
                
                // Update progress after each item
                val currentProgress = (index + 1).toFloat() / totalImages
                _uiState.update { it.copy(progress = currentProgress) }
            }
            
            if (isActive) {
                 _uiState.update { 
                    it.copy(
                        isConverting = false, 
                        conversionMessage = null,
                        selectedImages = emptyList(),
                        convertedImages = results,
                        screenState = BatchScreenState.SUCCESS,
                        progress = 1f
                    ) 
                }
            }
        }
    }
    
    fun cancelConversion() {
        conversionJob?.cancel()
        _uiState.update { 
            it.copy(
                isConverting = false, 
                conversionMessage = null,
                progress = 0f,
                processedCount = 0
            ) 
        }
    }
    
    fun saveAllToDevice(context: android.content.Context, treeUri: Uri?) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val state = _uiState.value
            var savedCount = 0
            
            // Just use the timestamp once for batch consistency if needed, or per file
            // User requested: "how the new name is being defining". We should use OriginalName_Converted.ext
            
            state.convertedImages.forEach { image ->
                 try {
                     // Name generation logic: OriginalName_Converted.ext
                     val originalName = image.name.removePrefix("CONVERTED_") // It was placeholder
                     // Better: use originalUri filename logic again or store clean name
                     // Let's rely on original filename from a fresh query or cached if we had it.
                     // We actually stored "CONVERTED_${originalDetails.name}" in image.name in the loop above.
                     // Let's refine that: image.name currently has the extension of the *original* usually.
                     // We need the extension of the *new* format.
                     
                     val ext = when (image.type) {
                         "image/jpeg" -> "jpg"
                         "image/png" -> "png"
                         "image/webp" -> "webp"
                         "image/bmp" -> "bmp"
                         "image/heic", "image/heif" -> "heic"
                         else -> "jpg"
                     }
                     
                     val baseName = image.name.substringBeforeLast(".") // Remove old extension
                     val finalName = "${baseName}_Edited.$ext"

                     if (treeUri != null) {
                         // Save to User Selected Folder
                         val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                         val newFile = docFile?.createFile(image.type, finalName)
                         if (newFile != null) {
                             context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                                 context.contentResolver.openInputStream(image.uri)?.use { input ->
                                     input.copyTo(out)
                                 }
                             }
                             savedCount++
                         }

                         // Persist location & Take Permission if needed
                         try {
                             val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                     android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                             context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                         } catch (e: Exception) {
                             // Ignore if already taken or not supported
                         }

                         viewModelScope.launch {
                             preferencesRepository.setBatchOutputFolder(treeUri.toString())
                         }
                     } else {
                         // Save to Default MediaStore (Pictures/Workmate/Converted)
                         val values = android.content.ContentValues().apply {
                             put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, finalName)
                             put(android.provider.MediaStore.Images.Media.MIME_TYPE, image.type)
                             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                 put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                                 put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Workmate/Converted")
                             }
                         }
                         
                         val resolver = context.contentResolver
                         val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                             android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
                         } else {
                             android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                         }
                         
                         val itemUri = resolver.insert(collection, values)
                         if (itemUri != null) {
                             resolver.openOutputStream(itemUri)?.use { out ->
                                 resolver.openInputStream(image.uri)?.use { input ->
                                     input.copyTo(out)
                                 }
                             }
                             
                             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                 values.clear()
                                 values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                                 resolver.update(itemUri, values, null, null)
                             }
                             savedCount++
                         }
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
            }
            
            _uiState.update { it.copy(message = "Saved $savedCount images", lastSavedLocation = treeUri) }
        }
    }
    
    fun resetState() {
        _uiState.update { 
            it.copy(
                screenState = BatchScreenState.INPUT,
                convertedImages = emptyList(),
                selectedImages = emptyList(),
                selectedDetailImage = null,
                lastSavedLocation = null
            )
        }
    }
    
    fun selectDetailImage(image: ConvertedImage) {
        _uiState.update { it.copy(screenState = BatchScreenState.DETAIL, selectedDetailImage = image) }
    }
    
    fun closeDetail() {
        _uiState.update { it.copy(screenState = BatchScreenState.SUCCESS, selectedDetailImage = null) }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null, conversionMessage = null, lastSavedLocation = null) }
    }
    
    data class ImageDetails(
        val name: String,
        val path: String,
        val size: String,
        val resolution: String,
        val type: String,
        val sizeBytes: Long = 0,
        val width: Int = 0,
        val height: Int = 0,
        val exifData: Map<String, String> = emptyMap()
    )
    
    fun toggleGuide() {
        _uiState.update { it.copy(showGuide = !it.showGuide) }
    }

    suspend fun getImageDetails(uri: Uri): ImageDetails {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            var name = "Unknown"
            val path = uri.path ?: "Unknown"
            var sizeBytes: Long = -1
            var resolution = "Unknown"
            var type = "Unknown"
            var wRef = 0
            var hRef = 0
            val exifMap = mutableMapOf<String, String>()
            
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
                                 if (w > 0 && h > 0) {
                                     resolution = "$w x $h"
                                     wRef = w
                                     hRef = h
                                 }
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
                 // Try file length if local
                if (sizeBytes <= 0 && uri.scheme == "file") {
                     val f = java.io.File(uri.path!!)
                     if (f.exists()) sizeBytes = f.length()
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
                             wRef = options.outWidth
                             hRef = options.outHeight
                             if (type == "Unknown" && options.outMimeType != null) type = options.outMimeType
                         }
                     } catch (e: Exception) {}
                }
                
                // EXIF Extraction
                try {
                     getApplication<Application>().contentResolver.openInputStream(uri)?.use { 
                         val exif = androidx.exifinterface.media.ExifInterface(it)
                         
                         val tags = listOf(
                             androidx.exifinterface.media.ExifInterface.TAG_DATETIME,
                             androidx.exifinterface.media.ExifInterface.TAG_MAKE,
                             androidx.exifinterface.media.ExifInterface.TAG_MODEL,
                             androidx.exifinterface.media.ExifInterface.TAG_APERTURE_VALUE,
                             androidx.exifinterface.media.ExifInterface.TAG_ISO_SPEED_RATINGS,
                             androidx.exifinterface.media.ExifInterface.TAG_EXPOSURE_TIME,
                             androidx.exifinterface.media.ExifInterface.TAG_FOCAL_LENGTH
                         )
                         
                         tags.forEach { tag ->
                             val value = exif.getAttribute(tag)
                             if (!value.isNullOrBlank()) {
                                 exifMap[tag] = value
                             }
                         }
                         
                         // Lat/Long special handling
                         val latLong = exif.latLong
                         if (latLong != null) {
                             exifMap["Location"] = "${latLong[0]}, ${latLong[1]}"
                         }
                     }
                } catch (e: Exception) {
                    // Ignore EXIF errors
                }

                // Name fallback (filename from path)
                if (name == "Unknown") {
                    name = uri.lastPathSegment ?: "Image"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val sizeString = if (sizeBytes > 0) formatFileSize(sizeBytes) else "Unknown"
            ImageDetails(name, path, sizeString, resolution, type, sizeBytes, wRef, hRef, exifMap)
        }
    }
    
    fun formatFileSize(size: Long): String {
        val mb = size / (1024.0 * 1024.0)
        if (mb >= 1.0) return String.format("%.2f MB", mb)
        val kb = size / 1024.0
        return String.format("%.2f KB", kb)
    }
}
