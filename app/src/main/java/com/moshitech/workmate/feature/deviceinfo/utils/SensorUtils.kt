package com.moshitech.workmate.feature.deviceinfo.utils

import android.hardware.Sensor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object SensorUtils {
    fun getSensorIcon(sensorType: Int): ImageVector {
        return when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> Icons.Default.Speed
            Sensor.TYPE_MAGNETIC_FIELD -> Icons.Default.Explore
            Sensor.TYPE_GYROSCOPE -> Icons.Default.RotateRight
            Sensor.TYPE_LIGHT -> Icons.Default.LightMode
            Sensor.TYPE_PRESSURE -> Icons.Default.Compress
            Sensor.TYPE_PROXIMITY -> Icons.Default.Sensors
            Sensor.TYPE_GRAVITY -> Icons.Default.ArrowDownward
            Sensor.TYPE_LINEAR_ACCELERATION -> Icons.Default.FastForward
            Sensor.TYPE_ROTATION_VECTOR -> Icons.Default.ScreenRotation
            Sensor.TYPE_RELATIVE_HUMIDITY -> Icons.Default.WaterDrop
            Sensor.TYPE_AMBIENT_TEMPERATURE -> Icons.Default.Thermostat
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> Icons.Default.Explore
            Sensor.TYPE_GAME_ROTATION_VECTOR -> Icons.Default.Gamepad
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> Icons.Default.RotateRight
            Sensor.TYPE_SIGNIFICANT_MOTION -> Icons.Default.DirectionsRun
            Sensor.TYPE_STEP_DETECTOR -> Icons.Default.DirectionsWalk
            Sensor.TYPE_STEP_COUNTER -> Icons.Default.DirectionsWalk
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> Icons.Default.Explore
            Sensor.TYPE_HEART_RATE -> Icons.Default.Favorite
            else -> Icons.Default.Sensors
        }
    }

    fun getSensorCategory(sensorType: Int): String {
        return when (sensorType) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_SIGNIFICANT_MOTION,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR -> "Motion"

            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
            Sensor.TYPE_PROXIMITY -> "Position"

            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Environment"

            else -> "Other"
        }
    }
}
