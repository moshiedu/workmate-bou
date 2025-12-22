package com.moshitech.workmate.feature.imagestudio.data

import com.moshitech.workmate.feature.imagestudio.viewmodel.ShapeLayer
import com.moshitech.workmate.feature.imagestudio.viewmodel.TextLayer
import com.moshitech.workmate.feature.imagestudio.viewmodel.StickerLayer

/**
 * Unified Layer interface for layer management
 * Provides common properties for all layer types
 */
sealed class Layer {
    abstract val id: String
    abstract val isVisible: Boolean
    abstract val zIndex: Int
    abstract val layerName: String
    abstract val type: LayerType
    
    data class Text(val textLayer: TextLayer) : Layer() {
        override val id = textLayer.id
        override val isVisible = textLayer.isVisible
        override val zIndex = textLayer.zIndex
        override val layerName = textLayer.layerName
        override val type = LayerType.TEXT
    }
    
    data class Sticker(val stickerLayer: StickerLayer) : Layer() {
        override val id = stickerLayer.id
        override val isVisible = stickerLayer.isVisible
        override val zIndex = stickerLayer.zIndex
        override val layerName = stickerLayer.layerName
        override val type = LayerType.STICKER
    }
    
    data class Shape(val shapeLayer: ShapeLayer) : Layer() {
        override val id = shapeLayer.id
        override val isVisible = shapeLayer.isVisible
        override val zIndex = shapeLayer.zIndex
        override val layerName = shapeLayer.layerName
        override val type = LayerType.SHAPE
    }
    
    // Note: Shape layer support added
}
