package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.data.models.CameraInfo

@Composable
fun CameraTab(
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val hardwareInfo by viewModel.hardwareInfoEnhanced.collectAsState()
    val cameras = hardwareInfo?.cameras ?: emptyList()
    
    var selectedCameraIndex by remember { mutableIntStateOf(0) }
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = Color(0xFF10B981)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (cameras.isNotEmpty()) {
            // Camera Selector
            ScrollableTabRow(
                selectedTabIndex = selectedCameraIndex,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCameraIndex]),
                        color = accentColor
                    )
                },
                divider = {}
            ) {
                cameras.forEachIndexed { index, camera ->
                    Tab(
                        selected = selectedCameraIndex == index,
                        onClick = { selectedCameraIndex = index },
                        text = { 
                            Text(
                                text = "${camera.facing} (${camera.megapixels})",
                                color = if (selectedCameraIndex == index) accentColor else subtitleColor,
                                fontWeight = if (selectedCameraIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Camera Details
            val selectedCamera = cameras.getOrNull(selectedCameraIndex)
            if (selectedCamera != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Header Card
                    item {
                        CameraHeaderCard(
                            camera = selectedCamera,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            accentColor = accentColor
                        )
                    }
                    
                    // Technical Specs
                    item {
                        CameraSpecsCard(
                            camera = selectedCamera,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )
                    }
                    
                    // Capabilities
                    item {
                        CameraCapabilitiesCard(
                            camera = selectedCamera,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            accentColor = accentColor
                        )
                    }
                    
                    // Video Modes
                    item {
                        CameraVideoCard(
                            camera = selectedCamera,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            accentColor = accentColor
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No cameras detected", color = subtitleColor)
            }
        }
    }
}

@Composable
fun CameraHeaderCard(
    camera: CameraInfo,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = camera.megapixels,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "${camera.facing} Camera",
                fontSize = 16.sp,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun CameraSpecsCard(
    camera: CameraInfo,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Specifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            SpecRow("Megapixels", camera.megapixels, textColor, subtitleColor)
            SpecRow("Resolution", camera.resolution, textColor, subtitleColor)
            SpecRow("Sensor size", camera.sensorSize, textColor, subtitleColor)
            SpecRow("Pixel size", camera.pixelSize, textColor, subtitleColor)
            SpecRow("Filter color arrangement", camera.filterColorArrangement, textColor, subtitleColor)
            SpecRow("Aperture", camera.aperture, textColor, subtitleColor)
            SpecRow("Focal length", camera.focalLength, textColor, subtitleColor)
            SpecRow("35mm equivalent focal length", camera.focalLength35mm, textColor, subtitleColor)
            SpecRow("Crop factor", camera.cropFactor, textColor, subtitleColor)
            SpecRow("Field of view", camera.fieldOfView, textColor, subtitleColor)
            SpecRow("Shutter speed", camera.shutterSpeedRange, textColor, subtitleColor)
            SpecRow("ISO sensitivity range", camera.isoRange, textColor, subtitleColor)
            SpecRow("Exposure range", camera.exposureRange, textColor, subtitleColor)
        }
    }
}

@Composable
fun CameraCapabilitiesCard(
    camera: CameraInfo,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Boolean capabilities with icons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureRow("Flash", camera.hasFlash, textColor, accentColor)
                FeatureRow("Video stabilization", camera.hasVideoStabilization, textColor, accentColor)
                FeatureRow("Optical image stabilization", camera.hasOpticalStabilization, textColor, accentColor)
                FeatureRow("AF lock", camera.hasAfLock, textColor, accentColor)
                FeatureRow("WB lock", camera.hasWbLock, textColor, accentColor)
            }
            
            if (camera.capabilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Capabilities",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.capabilities.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // Exposure modes
            if (camera.exposureModes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Exposure modes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.exposureModes.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // Autofocus modes
            if (camera.autofocusModes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Autofocus modes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.autofocusModes.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // White balance modes
            if (camera.whiteBalanceModes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "White balance modes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.whiteBalanceModes.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // Scene modes
            if (camera.sceneModes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scene modes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.sceneModes.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // Color effects
            if (camera.colorEffects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Color effects",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = camera.colorEffects.joinToString(", "),
                    fontSize = 14.sp,
                    color = subtitleColor
                )
            }
            
            // Face detection
            if (camera.maxFaceCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                SpecRow("Max face count", camera.maxFaceCount.toString(), textColor, subtitleColor)
                SpecRow("Face detect mode", camera.faceDetectMode, textColor, subtitleColor)
            }
            
            // Camera2 API level
            Spacer(modifier = Modifier.height(16.dp))
            SpecRow("Camera2 API support", camera.camera2ApiLevel, textColor, subtitleColor)
        }
    }
}

@Composable
fun CameraVideoCard(
    camera: CameraInfo,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Video capture",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Video profiles
            if (camera.videoProfiles.isNotEmpty()) {
                Text(
                    text = "Profiles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                camera.videoProfiles.forEach { profile ->
                    Text(
                        text = profile,
                        fontSize = 14.sp,
                        color = subtitleColor,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Max frame rate
            if (camera.maxFrameRate != "Unknown") {
                SpecRow("Max frame rate", camera.maxFrameRate, textColor, subtitleColor)
            }
            
            // Video features
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureRow("High speed video", camera.hasHighSpeedVideo, textColor, accentColor)
                FeatureRow("Video stabilization", camera.hasVideoStabilization, textColor, accentColor)
                FeatureRow("HDR support", camera.hasHdr, textColor, accentColor)
            }
        }
    }
}

@Composable
fun SpecRow(
    label: String,
    value: String,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = subtitleColor,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FeatureRow(
    label: String,
    isSupported: Boolean,
    textColor: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isSupported) accentColor else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor
        )
    }
}
