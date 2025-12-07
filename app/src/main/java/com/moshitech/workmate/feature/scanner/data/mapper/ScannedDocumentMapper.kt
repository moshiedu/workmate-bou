package com.moshitech.workmate.feature.scanner.data.mapper

import com.moshitech.workmate.feature.scanner.data.local.entity.ScannedDocumentEntity
import com.moshitech.workmate.feature.scanner.domain.model.ScannedDocument
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Mapper between entity and domain model
 */
fun ScannedDocumentEntity.toDomain(): ScannedDocument {
    return ScannedDocument(
        id = id,
        name = name,
        thumbnailPath = thumbnailPath,
        filePath = filePath,
        pageCount = pageCount,
        createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAt),
            ZoneId.systemDefault()
        ),
        modifiedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(modifiedAt),
            ZoneId.systemDefault()
        ),
        sizeInBytes = sizeInBytes
    )
}

fun ScannedDocument.toEntity(): ScannedDocumentEntity {
    return ScannedDocumentEntity(
        id = id,
        name = name,
        thumbnailPath = thumbnailPath,
        filePath = filePath,
        pageCount = pageCount,
        createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        modifiedAt = modifiedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        sizeInBytes = sizeInBytes
    )
}
