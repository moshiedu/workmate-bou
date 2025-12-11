package com.moshitech.workmate.feature.imagestudio.data

enum class CompressFormat {
    JPEG, PNG, WEBP, BMP, HEIF, PDF, ORIGINAL
}

data class ConversionSettings(
    val format: CompressFormat,
    val quality: Int,
    val width: Int?,
    val height: Int?,
    val maintainAspectRatio: Boolean,
    val targetSizeKB: Int? = null,
    val keepMetadata: Boolean = false
)

data class ConversionPreset(
    val name: String,
    val format: CompressFormat,
    val quality: Int,
    val width: String,
    val height: String,
    val maintainAspectRatio: Boolean,
    val targetSize: String,
    val isTargetSizeInMb: Boolean,
    val keepMetadata: Boolean
)
