package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.components.DetailRow
import com.moshitech.workmate.feature.deviceinfo.components.ExpandableSection
import com.moshitech.workmate.feature.deviceinfo.components.SubSection
import com.moshitech.workmate.feature.deviceinfo.utils.FormatUtils

@Composable
fun HardwareTabEnhanced(
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val hardwareInfo by viewModel.hardwareInfoEnhanced.collectAsState()
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Device Section
        ExpandableSection(
            title = "Device",
            subtitle = hardwareInfo.deviceModel,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            initiallyExpanded = true
        ) {
            DetailRow("Manufacturer", hardwareInfo.deviceManufacturer, textColor, subtitleColor)
            DetailRow("Model", hardwareInfo.deviceModel, textColor, subtitleColor)
            DetailRow("Codename", hardwareInfo.deviceCodename, textColor, subtitleColor)
            DetailRow("Brand", hardwareInfo.deviceBrand, textColor, subtitleColor)
        }
        
        // SoC Section
        ExpandableSection(
            title = "System on Chip",
            subtitle = hardwareInfo.socName,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            initiallyExpanded = true
        ) {
            DetailRow("Name", hardwareInfo.socName, textColor, subtitleColor)
            DetailRow("Manufacturer", hardwareInfo.socManufacturer, textColor, subtitleColor)
            DetailRow("Model", hardwareInfo.socModel, textColor, subtitleColor)
        }
        
        // CPU Section
        ExpandableSection(
            title = "Processor",
            subtitle = "${hardwareInfo.cpuTotalCores} cores",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            initiallyExpanded = true
        ) {
            DetailRow("Name", hardwareInfo.cpuName, textColor, subtitleColor)
            DetailRow("Architecture", hardwareInfo.cpuArchitecture, textColor, subtitleColor)
            DetailRow("Cores", hardwareInfo.cpuTotalCores.toString(), textColor, subtitleColor)
            
            if (hardwareInfo.cpuCores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SubSection("Core Details", textColor) {
                    hardwareInfo.cpuCores.forEachIndexed { index, core ->
                        DetailRow(
                            core.name,
                            FormatUtils.formatFrequency(core.maxFrequency),
                            textColor,
                            subtitleColor
                        )
                    }
                }
            }
            
            if (hardwareInfo.cpuImplementer != "Unknown") {
                Spacer(modifier = Modifier.height(8.dp))
                SubSection("CPU Info", textColor) {
                    DetailRow("Implementer", hardwareInfo.cpuImplementer, textColor, subtitleColor)
                    DetailRow("Variant", hardwareInfo.cpuVariant, textColor, subtitleColor)
                    DetailRow("Part", hardwareInfo.cpuPart, textColor, subtitleColor)
                    DetailRow("Revision", hardwareInfo.cpuRevision, textColor, subtitleColor)
                }
            }
        }
        
        // GPU Section
        ExpandableSection(
            title = "Graphics",
            subtitle = hardwareInfo.gpuName,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("GPU", hardwareInfo.gpuName, textColor, subtitleColor)
            DetailRow("Vendor", hardwareInfo.gpuVendor, textColor, subtitleColor)
            DetailRow("Renderer", hardwareInfo.gpuRenderer, textColor, subtitleColor)
            DetailRow("OpenGL ES", hardwareInfo.gpuOpenGlVersion, textColor, subtitleColor)
            DetailRow("Vulkan", hardwareInfo.gpuVulkanVersion, textColor, subtitleColor)
        }
        
        // Display Section
        ExpandableSection(
            title = "Display",
            subtitle = hardwareInfo.displayResolution,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Resolution", hardwareInfo.displayResolution, textColor, subtitleColor)
            DetailRow("Density", "${hardwareInfo.displayDensity} dpi", textColor, subtitleColor)
            DetailRow("Size", hardwareInfo.displaySize, textColor, subtitleColor)
            DetailRow("Refresh Rate", hardwareInfo.displayRefreshRate, textColor, subtitleColor)
        }
        
        // Memory Section
        ExpandableSection(
            title = "Memory",
            subtitle = hardwareInfo.ramTotal,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Total", hardwareInfo.ramTotal, textColor, subtitleColor)
            DetailRow("Available", hardwareInfo.ramAvailable, textColor, subtitleColor)
            DetailRow("Type", hardwareInfo.ramType, textColor, subtitleColor)
        }
        
        // Storage Section
        ExpandableSection(
            title = "Storage",
            subtitle = hardwareInfo.storageTotal,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Total", hardwareInfo.storageTotal, textColor, subtitleColor)
            DetailRow("Available", hardwareInfo.storageAvailable, textColor, subtitleColor)
            DetailRow("Type", hardwareInfo.storageType, textColor, subtitleColor)
            
            if (hardwareInfo.storagePartitions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SubSection("Partitions", textColor) {
                    hardwareInfo.storagePartitions.forEach { partition ->
                        DetailRow(partition.name, partition.total, textColor, subtitleColor)
                        DetailRow("File System", partition.fileSystem, textColor, subtitleColor, indent = 1)
                    }
                }
            }
        }
        
        // Camera Section
        if (hardwareInfo.cameras.isNotEmpty()) {
            ExpandableSection(
                title = "Camera",
                subtitle = "${hardwareInfo.cameras.size} camera(s)",
                cardColor = cardColor,
                textColor = textColor,
                subtitleColor = subtitleColor
            ) {
                hardwareInfo.cameras.forEach { camera ->
                    SubSection("${camera.facing} Camera", textColor) {
                        DetailRow("ID", camera.id, textColor, subtitleColor)
                        DetailRow("Facing", camera.facing, textColor, subtitleColor)
                        DetailRow("Megapixels", camera.megapixels, textColor, subtitleColor)
                    }
                }
            }
        }
    }
}
