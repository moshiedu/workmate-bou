package com.moshitech.workmate.feature.compass

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.hardware.camera2.CameraManager
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.compass.data.WaypointEntity
import com.moshitech.workmate.feature.compass.data.TripEntity
import kotlinx.coroutines.flow.collectLatest
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class CompassViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _cardinalDirection = MutableStateFlow("N")
    val cardinalDirection: StateFlow<String> = _cardinalDirection.asStateFlow()

    private val _accuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    private val _useTrueNorth = MutableStateFlow(false)
    val useTrueNorth: StateFlow<Boolean> = _useTrueNorth.asStateFlow()

    private val _qiblaDirection = MutableStateFlow(0f)
    val qiblaDirection: StateFlow<Float> = _qiblaDirection.asStateFlow()

    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _magneticFieldStrength = MutableStateFlow(0f)
    val magneticFieldStrength: StateFlow<Float> = _magneticFieldStrength.asStateFlow()

    private val _sunTimes = MutableStateFlow<SunTimes?>(null)
    val sunTimes: StateFlow<SunTimes?> = _sunTimes.asStateFlow()

    // Waypoint System
    private val database = AppDatabase.getDatabase(application)
    val waypoints = database.waypointDao().getAllWaypoints()
    private val _selectedWaypoint = MutableStateFlow<WaypointEntity?>(null)
    val selectedWaypoint: StateFlow<WaypointEntity?> = _selectedWaypoint.asStateFlow()

    // Trip Tracker
    val trips = database.tripDao().getAllTrips()
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    private val _tripDistance = MutableStateFlow(0.0)
    val tripDistance: StateFlow<Double> = _tripDistance.asStateFlow()
    private val _tripDuration = MutableStateFlow(0L)
    val tripDuration: StateFlow<Long> = _tripDuration.asStateFlow()
    private val _currentSpeed = MutableStateFlow(0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()
    private val _avgSpeed = MutableStateFlow(0f)
    val avgSpeed: StateFlow<Float> = _avgSpeed.asStateFlow()
    private val _maxSpeed = MutableStateFlow(0f)
    val maxSpeed: StateFlow<Float> = _maxSpeed.asStateFlow()
    private var tripStartTime = 0L
    private var lastTripLocation: android.location.Location? = null
    private var tripJob: Job? = null

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var declination = 0f
    
    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var sosJob: Job? = null
    private var lastHapticTime = 0L

    // Low-pass filter alpha (0 = no change, 1 = instant change). Lower = smoother but more lag.
    private val alpha = 0.05f 

    fun startSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    fun toggleTrueNorth(enabled: Boolean) {
        _useTrueNorth.value = enabled
        // In a real app, we would request location updates here to calculate declination.
        // For now, we'll simulate a declination or assume 0 if no location.
        // To implement properly, we need LocationManager.
        if (enabled) {
            updateLocationData()
        } else {
            declination = 0f
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        _hapticEnabled.value = enabled
    }

    fun toggleFlashlight() {
        if (_isSosActive.value) {
            stopSos()
        }
        val newState = !_isFlashlightOn.value
        setTorchMode(newState)
    }

    fun toggleSos() {
        if (_isSosActive.value) {
            stopSos()
        } else {
            startSos()
        }
    }

    private fun setTorchMode(enabled: Boolean) {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Usually back camera
            cameraManager.setTorchMode(cameraId, enabled)
            _isFlashlightOn.value = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSos() {
        _isSosActive.value = true
        sosJob = viewModelScope.launch {
            val dot = 200L
            val dash = 600L
            val gap = 200L
            
            while (isActive) {
                // S (...)
                repeat(3) {
                    setTorchMode(true)
                    delay(dot)
                    setTorchMode(false)
                    delay(gap)
                }
                delay(gap * 2)
                // O (---)
                repeat(3) {
                    setTorchMode(true)
                    delay(dash)
                    setTorchMode(false)
                    delay(gap)
                }
                delay(gap * 2)
                // S (...)
                repeat(3) {
                    setTorchMode(true)
                    delay(dot)
                    setTorchMode(false)
                    delay(gap)
                }
                delay(gap * 6) // Pause between SOS
            }
        }
    }

    private fun stopSos() {
        sosJob?.cancel()
        sosJob = null
        _isSosActive.value = false
        setTorchMode(false)
    }

    private fun updateLocationData() {
        try {
            val locationManager = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            
            // Request fresh location update
            val locationListener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    processLocation(location)
                    // Remove listener after getting fresh location
                    locationManager.removeUpdates(this)
                }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            }
            
            // Try GPS first, fallback to Network
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            } else if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }
            
            // Also use last known location as fallback
            val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) 
                android.location.LocationManager.GPS_PROVIDER 
            else 
                android.location.LocationManager.NETWORK_PROVIDER
            
            val lastLocation = locationManager.getLastKnownLocation(provider)
            if (lastLocation != null) {
                processLocation(lastLocation)
            }
        } catch (e: SecurityException) {
            declination = 0f
        } catch (e: Exception) {
            declination = 0f
        }
    }
    
    private fun processLocation(location: android.location.Location) {
        // Update Declination
        val geoField = android.hardware.GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = geoField.declination
        
        // Update Qibla
        _qiblaDirection.value = calculateQibla(location.latitude, location.longitude)
        
        // Calculate Sun Times
        _sunTimes.value = calculateSunTimes(location.latitude, location.longitude)
        
        // Update Location Info
        viewModelScope.launch {
            val address = getAddress(location)
            _locationInfo.value = LocationInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed * 3.6f, // m/s to km/h
                address = address
            )
        }
        
        // Update trip tracking if active
        updateTripTracking(location)
    }

    private fun getAddress(location: Location): String {
        return try {
            val geocoder = Geocoder(getApplication(), Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Async implementation for API 33+ (simplified here, ideally use callback)
                // For simplicity in this sync function, we might fallback or use blocking if needed, 
                // but Geocoder is blocking. In coroutine it's fine.
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
            }
        } catch (e: Exception) {
            "Location Unavailable"
        }
    }

    private fun calculateQibla(lat: Double, lon: Double): Float {
        // Mecca Coordinates
        val meccaLat = 21.4225
        val meccaLon = 39.8262
        
        val phiK = Math.toRadians(meccaLat)
        val lambdaK = Math.toRadians(meccaLon)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)
        
        val psi = Math.atan2(
            Math.sin(lambdaK - lambda),
            Math.cos(phi) * Math.tan(phiK) - Math.sin(phi) * Math.cos(lambdaK - lambda)
        )
        
        return Math.toDegrees(psi).toFloat()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = lowPass(event.values.clone(), gravity)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = lowPass(event.values.clone(), geomagnetic)
            // Calculate magnetic field strength
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            _magneticFieldStrength.value = kotlin.math.sqrt(x * x + y * y + z * z)
        }

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                
                // Orientation[0] is azimuth, [1] is pitch, [2] is roll
                var azimuthInRadians = orientation[0]
                var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                
                // Apply Declination for True North
                if (_useTrueNorth.value) {
                    azimuthInDegrees += declination
                }

                // Normalize to 0-360
                azimuthInDegrees = (azimuthInDegrees + 360) % 360
                
                _azimuth.value = azimuthInDegrees
                updateCardinalDirection(azimuthInDegrees)
                checkHapticFeedback(azimuthInDegrees)
                
                // Pitch and Roll (in degrees)
                _pitch.value = Math.toDegrees(orientation[1].toDouble()).toFloat()
                _roll.value = Math.toDegrees(orientation[2].toDouble()).toFloat()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _accuracy.value = accuracy
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    private fun updateCardinalDirection(degrees: Float) {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees + 22.5) / 45).toInt() % 8
        _cardinalDirection.value = directions[index]
    }
    
    private fun checkHapticFeedback(azimuth: Float) {
        if (!_hapticEnabled.value) return
        
        val now = System.currentTimeMillis()
        if (now - lastHapticTime < 500) return // Debounce

        // Check North (0)
        if (abs(azimuth - 0) < 2 || abs(azimuth - 360) < 2) {
            triggerVibration()
            lastHapticTime = now
            return
        }

        // Check Qibla
        if (_useTrueNorth.value) {
            val qibla = _qiblaDirection.value
            if (abs(azimuth - qibla) < 2) {
                triggerVibration()
                lastHapticTime = now
            }
        }
    }

    private fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    // Waypoint Management
    fun saveWaypoint(name: String, notes: String = "") {
        viewModelScope.launch {
            val location = _locationInfo.value ?: return@launch
            val waypoint = WaypointEntity(
                name = name,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            database.waypointDao().insertWaypoint(waypoint)
        }
    }

    fun deleteWaypoint(waypoint: WaypointEntity) {
        viewModelScope.launch {
            database.waypointDao().deleteWaypoint(waypoint)
            if (_selectedWaypoint.value?.id == waypoint.id) {
                _selectedWaypoint.value = null
            }
        }
    }

    fun selectWaypoint(waypoint: WaypointEntity?) {
        _selectedWaypoint.value = waypoint
    }

    fun getWaypointDistance(waypoint: WaypointEntity): String {
        val location = _locationInfo.value ?: return "--"
        val distance = NavigationUtils.calculateDistance(
            location.latitude, location.longitude,
            waypoint.latitude, waypoint.longitude
        )
        return NavigationUtils.formatDistance(distance)
    }

    fun getWaypointBearing(waypoint: WaypointEntity): Float {
        val location = _locationInfo.value ?: return 0f
        return NavigationUtils.calculateBearing(
            location.latitude, location.longitude,
            waypoint.latitude, waypoint.longitude
        )
    }

    // Trip Tracking
    fun startTrip() {
        if (_isTracking.value) return
        
        _isTracking.value = true
        _tripDistance.value = 0.0
        _tripDuration.value = 0L
        _maxSpeed.value = 0f
        _avgSpeed.value = 0f
        tripStartTime = System.currentTimeMillis()
        lastTripLocation = null
        
        // Start duration counter
        tripJob = viewModelScope.launch {
            while (isActive && _isTracking.value) {
                _tripDuration.value = (System.currentTimeMillis() - tripStartTime) / 1000
                delay(1000)
            }
        }
    }

    fun stopTrip() {
        if (!_isTracking.value) return
        
        _isTracking.value = false
        tripJob?.cancel()
        
        // Save trip to database
        viewModelScope.launch {
            val trip = TripEntity(
                startTime = tripStartTime,
                endTime = System.currentTimeMillis(),
                distance = _tripDistance.value,
                avgSpeed = _avgSpeed.value,
                maxSpeed = _maxSpeed.value,
                duration = _tripDuration.value
            )
            database.tripDao().insertTrip(trip)
        }
    }

    fun resetTrip() {
        _tripDistance.value = 0.0
        _tripDuration.value = 0L
        _currentSpeed.value = 0f
        _avgSpeed.value = 0f
        _maxSpeed.value = 0f
        lastTripLocation = null
    }

    fun updateTripTracking(location: android.location.Location) {
        if (!_isTracking.value) return
        
        var speedKmh = location.speed * 3.6f // m/s to km/h
        
        if (lastTripLocation != null) {
            val distance = NavigationUtils.calculateDistance(
                lastTripLocation!!.latitude, lastTripLocation!!.longitude,
                location.latitude, location.longitude
            )
            
            // Filter out GPS noise (e.g., less than 2 meters movement)
            if (distance > 2.0) {
                _tripDistance.value += distance
                
                // Fallback speed calculation if GPS speed is 0 but we moved
                if (speedKmh == 0f) {
                    val timeDiff = (location.time - lastTripLocation!!.time) / 1000.0 // seconds
                    if (timeDiff > 0) {
                        val calculatedSpeed = (distance / timeDiff) * 3.6f
                        if (calculatedSpeed < 200) { // Sanity check (ignore teleportation)
                            speedKmh = calculatedSpeed.toFloat()
                        }
                    }
                }
                
                lastTripLocation = location
            }
        } else {
            lastTripLocation = location
        }
        
        _currentSpeed.value = speedKmh
        
        if (_currentSpeed.value > _maxSpeed.value) {
            _maxSpeed.value = _currentSpeed.value
        }
        
        // Calculate average speed
        val durationHours = _tripDuration.value / 3600.0
        if (durationHours > 0.001) { // Avoid division by zero or very small numbers
            _avgSpeed.value = (_tripDistance.value / 1000 / durationHours).toFloat()
        }
    }
    
    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            database.tripDao().deleteTrip(trip)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
        stopSos()
    }
}

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val address: String
)

data class SunTimes(
    val sunrise: String,
    val sunset: String
)

// Extension functions for CompassViewModel
fun CompassViewModel.shareCoordinates(context: Context) {
    val location = locationInfo.value ?: return
    val text = buildString {
        appendLine("My Location:")
        appendLine("Lat: ${String.format("%.6f", location.latitude)}")
        appendLine("Lon: ${String.format("%.6f", location.longitude)}")
        appendLine("Alt: ${location.altitude.toInt()}m")
        appendLine("")
        appendLine("Google Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}")
    }
    
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Location"))
}

fun CompassViewModel.copyCoordinates(context: Context) {
    val location = locationInfo.value ?: return
    val text = "${location.latitude}, ${location.longitude}"
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Coordinates", text)
    clipboard.setPrimaryClip(clip)
}

private fun CompassViewModel.calculateSunTimes(lat: Double, lon: Double): SunTimes {
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    
    // Julian Day calculation
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    val jd = jdn.toDouble() - 0.5
    
    // Solar noon
    val n = jd - 2451545.0 + 0.0008
    val j = n - lon / 360.0
    val m2 = (357.5291 + 0.98560028 * j) % 360
    val c = 1.9148 * kotlin.math.sin(Math.toRadians(m2)) + 0.02 * kotlin.math.sin(Math.toRadians(2 * m2))
    val lambda = (m2 + c + 180 + 102.9372) % 360
    val jTransit = 2451545.0 + j + 0.0053 * kotlin.math.sin(Math.toRadians(m2)) - 0.0069 * kotlin.math.sin(Math.toRadians(2 * lambda))
    
    // Declination
    val delta = kotlin.math.asin(kotlin.math.sin(Math.toRadians(lambda)) * kotlin.math.sin(Math.toRadians(23.44)))
    
    // Hour angle
    val latRad = Math.toRadians(lat)
    val cosOmega = (kotlin.math.sin(Math.toRadians(-0.83)) - kotlin.math.sin(latRad) * kotlin.math.sin(delta)) / (kotlin.math.cos(latRad) * kotlin.math.cos(delta))
    
    if (cosOmega > 1 || cosOmega < -1) {
        // Polar day or night
        return SunTimes("--:--", "--:--")
    }
    
    val omega = Math.toDegrees(kotlin.math.acos(cosOmega))
    val jSet = jTransit + omega / 360.0
    val jRise = jTransit - omega / 360.0
    
    // Convert to local time with timezone adjustment
    fun jdToTime(jd: Double): String {
        // Convert Julian Date to UTC milliseconds
        val utcMillis = ((jd - 2440587.5) * 86400000).toLong()
        
        // Create calendar with local timezone
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = utcMillis
        
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        
        // Convert to 12-hour format with AM/PM
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }
    
    return SunTimes(jdToTime(jRise), jdToTime(jSet))
}
