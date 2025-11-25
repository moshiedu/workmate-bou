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
fun SystemTabEnhanced(
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val systemInfo by viewModel.systemInfoEnhanced.collectAsState()
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
            subtitle = systemInfo.deviceModel,
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            initiallyExpanded = true
        ) {
            DetailRow("Model", systemInfo.deviceModel, textColor, subtitleColor)
            DetailRow("Radio", systemInfo.deviceRadio, textColor, subtitleColor)
            DetailRow("Device", systemInfo.deviceName, textColor, subtitleColor)
            DetailRow("Product", systemInfo.deviceProduct, textColor, subtitleColor)
            DetailRow("Manufacturer", systemInfo.deviceManufacturer, textColor, subtitleColor)
            DetailRow("Brand", systemInfo.deviceBrand, textColor, subtitleColor)
        }
        
        // Operating System Section
        ExpandableSection(
            title = "Operating System",
            subtitle = "Android ${systemInfo.androidVersion} (${systemInfo.androidCodename})",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor,
            initiallyExpanded = true
        ) {
            DetailRow("Android version", systemInfo.androidVersion, textColor, subtitleColor)
            DetailRow("Codename", systemInfo.androidCodename, textColor, subtitleColor)
            DetailRow("API", systemInfo.androidApiLevel.toString(), textColor, subtitleColor)
            DetailRow("Security patch", systemInfo.securityPatch, textColor, subtitleColor)
            DetailRow("Build", systemInfo.buildNumber, textColor, subtitleColor)
            DetailRow("Fingerprint", systemInfo.buildFingerprint, textColor, subtitleColor)
        }
        
        // Instruction Sets Section
        ExpandableSection(
            title = "Instruction sets",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            systemInfo.instructionSets.forEach { isa ->
                DetailRow(isa, "Supported", textColor, subtitleColor, showCheckmark = true)
            }
        }
        
        // System Features Section
        ExpandableSection(
            title = "System Features",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Treble", if (systemInfo.trebleSupport) "Supported" else "Not supported", textColor, subtitleColor, showCheckmark = systemInfo.trebleSupport)
            DetailRow("Seamless updates", if (systemInfo.seamlessUpdates) "Supported" else "Not supported", textColor, subtitleColor, showCheckmark = systemInfo.seamlessUpdates)
            DetailRow("Active slot", systemInfo.activeSlot, textColor, subtitleColor)
        }
        
        // Status Section
        ExpandableSection(
            title = "Status",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Root", if (systemInfo.rootStatus) "Rooted" else "Not rooted", textColor, subtitleColor, showCheckmark = !systemInfo.rootStatus)
            DetailRow("Google Play", systemInfo.googlePlayVersion, textColor, subtitleColor)
            DetailRow("Certified", if (systemInfo.googlePlayCertified) "Yes" else "No", textColor, subtitleColor, showCheckmark = systemInfo.googlePlayCertified)
        }
        
        // System Components Section
        ExpandableSection(
            title = "System Components",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Toolbox", systemInfo.toolboxVersion, textColor, subtitleColor)
            DetailRow("Java VM", systemInfo.javaVm, textColor, subtitleColor)
            DetailRow("Version", systemInfo.javaVmVersion, textColor, subtitleColor, indent = 1)
            DetailRow("SELinux", systemInfo.seLinuxStatus, textColor, subtitleColor)
            DetailRow("Mode", systemInfo.seLinuxMode, textColor, subtitleColor, indent = 1)
        }
        
        // Locale Section
        ExpandableSection(
            title = "Locale",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Language", systemInfo.language, textColor, subtitleColor)
            DetailRow("Timezone", systemInfo.timezone, textColor, subtitleColor)
        }
        
        // Kernel Section
        ExpandableSection(
            title = "Kernel",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Version", systemInfo.kernelVersion, textColor, subtitleColor)
            DetailRow("Architecture", systemInfo.kernelArchitecture, textColor, subtitleColor)
            DetailRow("Build date", systemInfo.kernelBuildDate, textColor, subtitleColor)
        }
        
        // Identifiers Section
        ExpandableSection(
            title = "Identifiers",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Device ID", systemInfo.deviceId, textColor, subtitleColor)
            DetailRow("Android ID", systemInfo.androidId, textColor, subtitleColor)
            DetailRow("GSF ID", systemInfo.gsfId, textColor, subtitleColor)
        }
        
        // DRM Section
        ExpandableSection(
            title = "DRM",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            SubSection("Clearkey", textColor) {
                DetailRow("Vendor", systemInfo.drmClearkey, textColor, subtitleColor)
            }
            SubSection("Widevine", textColor) {
                DetailRow("Vendor", systemInfo.drmWidevineVendor, textColor, subtitleColor)
                DetailRow("Version", systemInfo.drmWidevineVersion, textColor, subtitleColor)
                DetailRow("Algorithms", systemInfo.drmWidevineAlgorithms, textColor, subtitleColor)
                DetailRow("Security level", systemInfo.drmWidevineSecurityLevel, textColor, subtitleColor)
                DetailRow("Max HDCP level", systemInfo.drmWidevineMaxHdcpLevel, textColor, subtitleColor)
                DetailRow("Max uses", systemInfo.drmWidevineMaxUses, textColor, subtitleColor)
            }
        }
        
        // Bootloader Section
        ExpandableSection(
            title = "Bootloader",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Bootloader", systemInfo.bootloader, textColor, subtitleColor)
            DetailRow("Baseband", systemInfo.baseband, textColor, subtitleColor)
        }
        
        // Graphics Section
        ExpandableSection(
            title = "Graphics",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("OpenGL ES", systemInfo.openGlVersion, textColor, subtitleColor)
        }
        
        // System Uptime Section
        ExpandableSection(
            title = "System",
            cardColor = cardColor,
            textColor = textColor,
            subtitleColor = subtitleColor
        ) {
            DetailRow("Uptime", FormatUtils.formatUptime(systemInfo.uptimeMillis), textColor, subtitleColor)
        }
    }
}
