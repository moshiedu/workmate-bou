package com.moshitech.workmate.feature.deviceinfo.testing

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

class HardwareTestExecutor(private val context: Context) {
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    
    fun executeTest(testId: String, onComplete: (Boolean) -> Unit) {
        when (testId) {
            "flashlight" -> testFlashlight(onComplete)
            "vibration" -> testVibration(onComplete)
            "buttons" -> {
                // Buttons test requires user interaction
                onComplete(true)
            }
            "multitouch" -> {
                // Multitouch test requires user interaction
                onComplete(true)
            }
            "display" -> {
                // Display test requires visual confirmation
                onComplete(true)
            }
            "backlight" -> {
                // Backlight test requires visual confirmation
                onComplete(true)
            }
            "light_sensor" -> testLightSensor(onComplete)
            "proximity" -> testProximitySensor(onComplete)
            "accelerometer" -> testAccelerometer(onComplete)
            "gyroscope" -> testGyroscope(onComplete)
            "magnetometer" -> testMagnetometer(onComplete)
            "charging" -> testCharging(onComplete)
            "speakers" -> testSpeakers(onComplete)
            "headset" -> testHeadset(onComplete)
            "earpiece" -> testEarpiece(onComplete)
            "microphone" -> testMicrophone(onComplete)
            "fingerprint" -> testFingerprint(onComplete)
            "usb" -> testUSB(onComplete)
            else -> onComplete(false)
        }
    }
    
    private fun testFlashlight(onComplete: (Boolean) -> Unit) {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                // Turn on flashlight
                cameraManager.setTorchMode(cameraId, true)
                
                // Wait 2 seconds
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    // Turn off flashlight
                    cameraManager.setTorchMode(cameraId, false)
                    onComplete(true)
                }, 2000)
            } else {
                onComplete(false)
            }
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testVibration(onComplete: (Boolean) -> Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testLightSensor(onComplete: (Boolean) -> Unit) {
        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val lightSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)
            onComplete(lightSensor != null)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testProximitySensor(onComplete: (Boolean) -> Unit) {
        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val proximitySensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_PROXIMITY)
            onComplete(proximitySensor != null)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testAccelerometer(onComplete: (Boolean) -> Unit) {
        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
            onComplete(accelerometer != null)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testCharging(onComplete: (Boolean) -> Unit) {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val isCharging = batteryManager.isCharging
            onComplete(isCharging)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testSpeakers(onComplete: (Boolean) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            // Play a test tone
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                android.media.ToneGenerator.MAX_VOLUME
            )
            toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 500)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
                onComplete(true)
            }, 600)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testHeadset(onComplete: (Boolean) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val isHeadsetConnected = audioManager.isWiredHeadsetOn
            onComplete(isHeadsetConnected)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testEarpiece(onComplete: (Boolean) -> Unit) {
        // Earpiece test would require playing audio through earpiece
        onComplete(true)
    }
    
    private fun testMicrophone(onComplete: (Boolean) -> Unit) {
        try {
            val audioRecord = android.media.AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                1024
            )
            
            val state = audioRecord.state
            audioRecord.release()
            onComplete(state == android.media.AudioRecord.STATE_INITIALIZED)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testFingerprint(onComplete: (Boolean) -> Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val fingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as android.hardware.fingerprint.FingerprintManager
                onComplete(fingerprintManager.isHardwareDetected)
            } else {
                onComplete(false)
            }
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testGyroscope(onComplete: (Boolean) -> Unit) {
        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val gyroscope = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE)
            onComplete(gyroscope != null)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testMagnetometer(onComplete: (Boolean) -> Unit) {
        try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val magnetometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)
            onComplete(magnetometer != null)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
    
    private fun testUSB(onComplete: (Boolean) -> Unit) {
        try {
            val packageManager = context.packageManager
            val hasOTGSupport = packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_USB_HOST)
            onComplete(hasOTGSupport)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
}
