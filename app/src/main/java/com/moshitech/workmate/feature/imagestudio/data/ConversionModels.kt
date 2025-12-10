package com.moshitech.workmate.feature.imagestudio.data

enum class CompressFormat {
    JPEG, PNG, WEBP, BMP, HEIF, ORIGINAL
}

data class ConversionSettings(
    val format: CompressFormat,
    val quality: Int,
    val width: Int?,
    val height: Int?,
    val maintainAspectRatio: Boolean,
    val targetSizeKB: Int? = null
)
