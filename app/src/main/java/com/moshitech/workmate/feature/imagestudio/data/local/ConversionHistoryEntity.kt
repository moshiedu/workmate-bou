package com.moshitech.workmate.feature.imagestudio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history_table")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val originalUri: String,
    val outputUri: String,
    val format: String,
    val sizeBytes: Long
)
