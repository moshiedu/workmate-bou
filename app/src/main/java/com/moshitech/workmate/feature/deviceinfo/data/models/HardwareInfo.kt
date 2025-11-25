package com.moshitech.workmate.feature.deviceinfo.data.models

data class HardwareInfo(
    val deviceManufacturer: String = "Unknown",
    val deviceModel: String = "Unknown",
    val deviceCodename: String = "Unknown",
    val deviceBrand: String = "Unknown",
    val cpuName: String = "Unknown",
    val cpuArchitecture: String = "Unknown",
    val cpuCores: Int = 0,
    val cpuMaxFrequency: String = "Unknown",
    val gpuName: String = "Unknown",
    val gpuVendor: String = "Unknown",
    val displayResolution: String = "Unknown",
    val displayDensity: Int = 0,
    val displaySize: String = "Unknown",
    val displayRefreshRate: String = "Unknown",
    val ramTotal: String = "Unknown",
    val ramType: String = "Unknown",
    val storageTotal: String = "Unknown",
    val storageType: String = "Unknown"
)
