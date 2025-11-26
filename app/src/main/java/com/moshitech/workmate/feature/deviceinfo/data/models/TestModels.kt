package com.moshitech.workmate.feature.deviceinfo.data.models

enum class TestStatus {
    UNTESTED,
    PASSED,
    FAILED
}

data class HardwareTest(
    val id: String,
    val name: String,
    val icon: String,
    val isPro: Boolean = false,
    var status: TestStatus = TestStatus.UNTESTED
)

data class TemperatureReading(
    val cpuTemp: Float = 0f,
    val socTemp: Float = 0f,
    val batteryTemp: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
