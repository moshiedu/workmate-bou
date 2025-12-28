package com.moshitech.workmate.feature.imagestudio.data

enum class CompressFormat {
    JPEG, PNG, WEBP, BMP, HEIF, PDF, ORIGINAL
}

enum class PdfPageSize {
    ORIGINAL, A4, LETTER
}

enum class PdfOrientation {
    PORTRAIT, LANDSCAPE, AUTO
}

data class ConversionSettings(
    val format: CompressFormat,
    val quality: Int,
    val width: Int?,
    val height: Int?,
    val maintainAspectRatio: Boolean,
    val targetSizeKB: Int? = null,
    val keepMetadata: Boolean = false,
    val pdfPageSize: PdfPageSize = PdfPageSize.ORIGINAL,
    val pdfOrientation: PdfOrientation = PdfOrientation.AUTO
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
