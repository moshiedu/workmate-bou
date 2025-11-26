package com.moshitech.workmate.feature.compass.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val distance: Double, // in meters
    val avgSpeed: Float, // in km/h
    val maxSpeed: Float, // in km/h
    val duration: Long // in seconds
)
