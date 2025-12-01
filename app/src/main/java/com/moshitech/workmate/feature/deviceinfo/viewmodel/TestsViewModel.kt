package com.moshitech.workmate.feature.deviceinfo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.deviceinfo.data.TestResultsRepository
import com.moshitech.workmate.feature.deviceinfo.data.models.HardwareTest
import com.moshitech.workmate.feature.deviceinfo.data.models.TestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TestsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TestResultsRepository(application)

    private val _tests = MutableStateFlow(
        listOf(
            HardwareTest("flashlight", "Flashlight", "flashlight"),
            HardwareTest("vibration", "Vibration", "vibration"),
            HardwareTest("buttons", "Buttons", "buttons"),
            HardwareTest("multitouch", "Multitouch", "multitouch"),
            HardwareTest("display", "Display", "display"),
            HardwareTest("backlight", "Backlight", "backlight"),
            HardwareTest("light_sensor", "Light sensor", "light"),
            HardwareTest("proximity", "Proximity", "proximity"),
            HardwareTest("accelerometer", "Accelerometer", "accelerometer"),
            HardwareTest("gyroscope", "Gyroscope", "gyroscope"),
            HardwareTest("magnetometer", "Magnetometer", "magnetometer"),
            HardwareTest("bluetooth", "Bluetooth", "bluetooth"),
            HardwareTest("wifi", "Wi-Fi", "wifi", isPro = true),
            HardwareTest("gps", "GPS", "gps", isPro = true),
            HardwareTest("nfc", "NFC", "nfc", isPro = true),
            HardwareTest("charging", "Charging", "charging", isPro = true),
            HardwareTest("speakers", "Speakers", "speakers", isPro = true),
            HardwareTest("headset", "Headset", "headset", isPro = true),
            HardwareTest("earpiece", "Earpiece", "earpiece", isPro = true),
            HardwareTest("microphone", "Microphone", "microphone", isPro = true),
            HardwareTest("fingerprint", "Fingerprint", "fingerprint", isPro = true),
            HardwareTest("usb", "USB", "usb", isPro = true)
        )
    )
    
    // We combine the static list with the dynamic statuses from the repository
    // But for simplicity and performance, we'll just load statuses on init and update them
    // Actually, observing all flows might be heavy. Let's just load them.
    // Better: Expose a single flow that combines the list with the statuses.
    
    private val _uiState = MutableStateFlow<List<HardwareTest>>(emptyList())
    val uiState: StateFlow<List<HardwareTest>> = _uiState.asStateFlow()

    init {
        refreshTests()
    }

    private fun refreshTests() {
        viewModelScope.launch {
            // Create a flow that combines the base list with the status of each item
            val baseList = _tests.value
            val flows = baseList.map { test ->
                repository.getTestStatus(test.id).map { status ->
                    test.copy(status = status)
                }
            }
            
            // Combine all individual status flows into one list flow
            combine<HardwareTest, List<HardwareTest>>(flows) { tests -> 
                tests.toList() 
            }.collect { updatedList ->
                _uiState.value = updatedList
            }
        }
    }

    fun updateTestStatus(testId: String, passed: Boolean) {
        viewModelScope.launch {
            val status = if (passed) TestStatus.PASSED else TestStatus.FAILED
            repository.setTestStatus(testId, status)
        }
    }

    fun resetAllTests() {
        viewModelScope.launch {
            val ids = _tests.value.map { it.id }
            repository.resetAllTests(ids)
        }
    }
}
