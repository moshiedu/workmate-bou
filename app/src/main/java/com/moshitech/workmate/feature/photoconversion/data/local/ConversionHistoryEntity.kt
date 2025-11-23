package com.moshitech.workmate.feature.photoconversion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val convertedPath: String,
    val format: String,
    val size: String,
    val timestamp: Long
)
