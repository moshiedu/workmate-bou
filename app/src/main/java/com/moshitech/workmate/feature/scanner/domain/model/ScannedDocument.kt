package com.moshitech.workmate.feature.scanner.domain.model

import java.time.LocalDateTime

/**
 * Represents a scanned document
 */
data class ScannedDocument(
    val id: String,
    val name: String,
    val thumbnailPath: String?,
    val filePath: String,
    val pageCount: Int,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val sizeInBytes: Long,
    val tags: List<String> = emptyList()
)
