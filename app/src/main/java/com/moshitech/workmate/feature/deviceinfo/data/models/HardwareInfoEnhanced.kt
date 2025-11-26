package com.moshitech.workmate.feature.deviceinfo.data.models

data class CpuCore(
    val name: String,
    val minFrequency: Long,
    val maxFrequency: Long,
    val currentFrequency: Long
)

data class HardwareInfoEnhanced(
    // Device
    val deviceManufacturer: String = "Unknown",
    val deviceModel: String = "Unknown",
    val deviceCodename: String = "Unknown",
    val deviceBrand: String = "Unknown",
    
    // SoC
    val socName: String = "Unknown",
    val socManufacturer: String = "Unknown",
    val socModel: String = "Unknown",
    
    // CPU
    val cpuName: String = "Unknown",
    val cpuArchitecture: String = "Unknown",
    val cpuCores: List<CpuCore> = emptyList(),
    val cpuTotalCores: Int = 0,
    val cpuImplementer: String = "Unknown",
    val cpuVariant: String = "Unknown",
    val cpuPart: String = "Unknown",
    val cpuRevision: String = "Unknown",
    
    // GPU
    val gpuName: String = "Unknown",
    val gpuVendor: String = "Unknown",
    val gpuRenderer: String = "Unknown",
    val gpuOpenGlVersion: String = "Unknown",
    val gpuVulkanVersion: String = "Unknown",
    val gpuShaderCores: Int = 0,
    val gpuMaxFrequency: String = "Unknown",
    
    // Display
    val displayResolution: String = "Unknown",
    val displayDensity: Int = 0,
    val displaySize: String = "Unknown",
    val displayRefreshRate: String = "Unknown",
    val displayTechnology: String = "Unknown",
    val displayHdr: Boolean = false,
    
    // RAM
    val ramTotal: String = "Unknown",
    val ramAvailable: String = "Unknown",
    val ramType: String = "Unknown",
    val ramFrequency: String = "Unknown",
    val ramChannels: Int = 0,
    
    // Storage
    val storageTotal: String = "Unknown",
    val storageAvailable: String = "Unknown",
    val storageType: String = "Unknown",
    val storagePartitions: List<StoragePartition> = emptyList(),
    
    // Network
    val wifiStandards: List<String> = emptyList(),
    val bluetoothVersion: String = "Unknown",
    val nfcSupport: Boolean = false,
    val cellularBands: List<String> = emptyList(),
    
    // Audio
    val audioCodecs: List<String> = emptyList(),
    
    // Camera
    val cameras: List<CameraInfo> = emptyList(),
    
    // Sensors
    val sensorCount: Int = 0,
    val sensors: List<String> = emptyList()
)

data class StoragePartition(
    val name: String,
    val path: String,
    val total: String,
    val available: String,
    val fileSystem: String
)

data class CameraInfo(
    val id: String,
    val facing: String,
    val megapixels: String,
    val aperture: String,
    val focalLength: String,
    val sensorSize: String,
    val pixelSize: String,
    val isoRange: String,
    val videoResolution: String
)
