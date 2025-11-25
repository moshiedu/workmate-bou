package com.moshitech.workmate.feature.deviceinfo.data.models

data class SystemInfoEnhanced(
    // Device
    val deviceModel: String = "Unknown",
    val deviceRadio: String = "Unknown",
    val deviceName: String = "Unknown",
    val deviceProduct: String = "Unknown",
    val deviceManufacturer: String = "Unknown",
    val deviceBrand: String = "Unknown",
    
    // Operating System
    val androidVersion: String = "Unknown",
    val androidCodename: String = "Unknown",
    val androidApiLevel: Int = 0,
    val securityPatch: String = "Unknown",
    val buildNumber: String = "Unknown",
    val buildFingerprint: String = "Unknown",
    
    // Instruction Sets
    val instructionSets: List<String> = emptyList(),
    
    // System Features
    val trebleSupport: Boolean = false,
    val seamlessUpdates: Boolean = false,
    val activeSlot: String = "Unknown",
    
    // Status
    val rootStatus: Boolean = false,
    val googlePlayCertified: Boolean = false,
    val googlePlayVersion: String = "Unknown",
    
    // System Components
    val toolboxVersion: String = "Unknown",
    val javaVm: String = "Unknown",
    val javaVmVersion: String = "Unknown",
    val seLinuxStatus: String = "Unknown",
    val seLinuxMode: String = "Unknown",
    
    // Locale
    val language: String = "Unknown",
    val timezone: String = "Unknown",
    
    // Kernel
    val kernelVersion: String = "Unknown",
    val kernelArchitecture: String = "Unknown",
    val kernelBuildDate: String = "Unknown",
    
    // Identifiers
    val deviceId: String = "Unknown",
    val androidId: String = "Unknown",
    val gsfId: String = "Unknown",
    
    // DRM
    val drmClearkey: String = "Unknown",
    val drmWidevineVendor: String = "Unknown",
    val drmWidevineVersion: String = "Unknown",
    val drmWidevineAlgorithms: String = "Unknown",
    val drmWidevineSecurityLevel: String = "Unknown",
    val drmWidevineMaxHdcpLevel: String = "Unknown",
    val drmWidevineMaxUses: String = "Unknown",
    
    // Bootloader
    val bootloader: String = "Unknown",
    val baseband: String = "Unknown",
    
    // OpenGL
    val openGlVersion: String = "Unknown",
    
    // System Uptime
    val uptimeMillis: Long = 0L
)
