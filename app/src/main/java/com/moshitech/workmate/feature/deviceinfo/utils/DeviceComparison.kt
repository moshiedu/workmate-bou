package com.moshitech.workmate.feature.deviceinfo.utils

data class ReferenceDevice(
    val name: String,
    val cpuScore: Int,
    val gpuScore: Int,
    val storageScore: Int,
    val ramScore: Int,
    val compositeScore: Int
)

object DeviceComparison {
    val referenceDevices = listOf(
        ReferenceDevice(
            name = "Google Pixel 8 Pro",
            cpuScore = 2800,
            gpuScore = 2200,
            storageScore = 1800,
            ramScore = 2100,
            compositeScore = 8900
        ),
        ReferenceDevice(
            name = "Samsung Galaxy S24 Ultra",
            cpuScore = 3000,
            gpuScore = 2400,
            storageScore = 1900,
            ramScore = 2200,
            compositeScore = 9500
        ),
        ReferenceDevice(
            name = "iPhone 15 Pro Max",
            cpuScore = 3200,
            gpuScore = 2600,
            storageScore = 2000,
            ramScore = 2300,
            compositeScore = 10100
        ),
        ReferenceDevice(
            name = "OnePlus 12",
            cpuScore = 2700,
            gpuScore = 2100,
            storageScore = 1700,
            ramScore = 2000,
            compositeScore = 8500
        ),
        ReferenceDevice(
            name = "Xiaomi 14 Pro",
            cpuScore = 2600,
            gpuScore = 2000,
            storageScore = 1600,
            ramScore = 1900,
            compositeScore = 8100
        )
    )
    
    fun getPercentileRanking(score: Int): String {
        val sorted = referenceDevices.map { it.compositeScore }.sorted()
        val betterThan = sorted.count { it < score }
        val percentile = (betterThan.toFloat() / sorted.size * 100).toInt()
        return "Better than $percentile% of reference devices"
    }
    
    fun getClosestDevice(score: Int): ReferenceDevice? {
        return referenceDevices.minByOrNull { kotlin.math.abs(it.compositeScore - score) }
    }
}
