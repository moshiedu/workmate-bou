package com.moshitech.workmate.feature.imagestudio.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaItem(
    val uri: Uri,
    val displayName: String,
    val bucketName: String,
    val dateAdded: Long
)

enum class SortOption {
    DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC
}

data class GalleryUiState(
    val allImages: List<MediaItem> = emptyList(),
    val filteredImages: List<MediaItem> = emptyList(),
    val selectedImages: Set<Uri> = emptySet(),
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val availableFilters: List<String> = listOf("All"),
    val sortOption: SortOption = SortOption.DATE_DESC,
    val isLoading: Boolean = true,
    val permissionGranted: Boolean = false,
    val showMultiEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isSelectionMode: Boolean = false
)

class PhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) {
            loadImages()
        }
    }
    
    fun setSelectionMode(enabled: Boolean) {
        _uiState.update { it.copy(isSelectionMode = enabled) }
        if (!enabled) {
            clearSelection()
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val loadedImages = withContext(Dispatchers.IO) {
                val images = mutableListOf<MediaItem>()
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED
                )
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                getApplication<Application>().contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: "Unknown"
                        val bucket = cursor.getString(bucketColumn) ?: "Other"
                        val date = cursor.getLong(dateColumn)
                        
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        images.add(MediaItem(contentUri, name, bucket, date))
                    }
                }
                images
            }
            
            val filters = mutableListOf("All")
            filters.addAll(loadedImages.map { it.bucketName }.distinct().sorted())

            _uiState.update { 
                it.copy(
                    allImages = loadedImages, 
                    filteredImages = loadedImages, // Initially all
                    availableFilters = filters,
                    isLoading = false
                ) 
            }
            applyFilters() // Apply filters and sort
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }
    
    fun onSortOptionSelected(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.trim().lowercase()
        val filter = currentState.selectedFilter
        val sort = currentState.sortOption
        
        var result = currentState.allImages.filter { item ->
            val matchesSearch = if (query.isEmpty()) true else item.displayName.lowercase().contains(query)
            val matchesFilter = if (filter == "All") true else item.bucketName == filter
            matchesSearch && matchesFilter
        }
        
        result = when (sort) {
            SortOption.DATE_DESC -> result.sortedByDescending { it.dateAdded }
            SortOption.DATE_ASC -> result.sortedBy { it.dateAdded }
            SortOption.NAME_ASC -> result.sortedBy { it.displayName }
            SortOption.NAME_DESC -> result.sortedByDescending { it.displayName }
        }
        
        _uiState.update { it.copy(filteredImages = result) }
    }

    fun toggleSelection(uri: Uri) {
        _uiState.update { state ->
            val currentSelection = state.selectedImages.toMutableSet()
            if (currentSelection.contains(uri)) {
                currentSelection.remove(uri)
            } else {
                currentSelection.add(uri)
            }
            state.copy(selectedImages = currentSelection, isSelectionMode = true)
        }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedImages = emptySet(), isSelectionMode = false) }
    }
    
    fun getFirstSelectedImage(): Uri? {
        return _uiState.value.selectedImages.firstOrNull()
    }

    // Delete Logic
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
             _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
             val selected = _uiState.value.selectedImages.toList()
             withContext(Dispatchers.IO) {
                 selected.forEach { uri ->
                     try {
                         getApplication<Application>().contentResolver.delete(uri, null, null)
                     } catch (e: Exception) {
                         // On Android 10+, simple delete might fail or require specific exception handling
                         // For simplicity in this iteration, we attempt delete.
                         e.printStackTrace()
                     }
                 }
             }
             _uiState.update { it.copy(selectedImages = emptySet()) }
             loadImages() // Reload to reflect changes
        }
    }

    // Camera Logic
    private var _tempCameraUri: Uri? = null
    private var _tempCameraFile: java.io.File? = null

    fun createTempPictureUri(): Uri? {
        try {
            val cacheDir = getApplication<Application>().cacheDir
            val imageFile = java.io.File.createTempFile(
                "img_${System.currentTimeMillis()}_", 
                ".jpg", 
                cacheDir
            )
            _tempCameraFile = imageFile
            _tempCameraUri = androidx.core.content.FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.fileprovider",
                imageFile
            )
            return _tempCameraUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun onCameraResult(success: Boolean) {
        if (success && _tempCameraFile != null && _tempCameraFile!!.exists()) {
            viewModelScope.launch {
                // Save to MediaStore so it appears in gallery
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "Workmate_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                
                val resolver = getApplication<Application>().contentResolver
                val mediaUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                
                if (mediaUri != null) {
                    try {
                        resolver.openOutputStream(mediaUri)?.use { outputStream ->
                            _tempCameraFile!!.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(mediaUri, contentValues, null, null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        resolver.delete(mediaUri, null, null)
                    }
                }
                
                // Cleanup temp file
                _tempCameraFile?.delete()
                _tempCameraFile = null
                
                loadImages() // Reload to show new photo
            }
        } else {
            // Cleanup on failure
            _tempCameraFile?.delete()
            _tempCameraFile = null
        }
        _tempCameraUri = null
    }
}
