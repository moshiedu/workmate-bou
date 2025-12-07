package com.moshitech.workmate.feature.scanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for scanned documents
 */
@Entity(tableName = "scanned_documents")
data class ScannedDocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val thumbnailPath: String?,
    val filePath: String,
    val pageCount: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val sizeInBytes: Long
)
