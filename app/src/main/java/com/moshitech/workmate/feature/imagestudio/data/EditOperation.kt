package com.moshitech.workmate.feature.imagestudio.data

import android.graphics.Rect

sealed class EditOperation {
    abstract val timestamp: Long
    abstract val description: String
    
    data class LayerAdded(
        val layerId: String,
        val layerType: LayerType,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Added ${layerType.name.lowercase()}"
    ) : EditOperation()
    
    data class LayerModified(
        val layerId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Modified layer"
    ) : EditOperation()
    
    data class LayerDeleted(
        val layerId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Deleted layer"
    ) : EditOperation()
    
    data class AdjustmentApplied(
        val adjustmentType: AdjustmentType,
        val value: Float,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "${adjustmentType.name}: $value"
    ) : EditOperation()
    
    data class CropApplied(
        val cropRect: Rect,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Cropped image"
    ) : EditOperation()
    
    data class FilterApplied(
        val filterId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Applied filter: $filterId"
    ) : EditOperation()
    
    data class LayersFlattened(
        override val timestamp: Long = System.currentTimeMillis(),
        override val description: String = "Applied all layers"
    ) : EditOperation()
}

enum class LayerType { TEXT, SHAPE, STICKER, DRAW }

enum class AdjustmentType { 
    BRIGHTNESS, CONTRAST, SATURATION, HUE, TEMPERATURE, TINT 
}
