package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val cameras = hardwareInfo.cameras
    
    var selectedCameraIndex by remember { mutableIntStateOf(0) }
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = Color(0xFF4CAF50) // Green color from reference

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Disclaimer Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF66BB6A)), // Green background
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "The full megapixel count and number of cameras may not be available due to limitations of the Android camera API.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on Left
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Info on Right
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = camera.megapixels,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = camera.aperture,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
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
            // No title needed for cleaner look, or small title
            
            SpecRow("Effective megapixels", camera.megapixels, textColor, subtitleColor)
            SpecRow("Resolution", camera.resolution, textColor, subtitleColor)
            SpecRow("Sensor size", camera.sensorSize, textColor, subtitleColor)
            SpecRow("Pixel size", camera.pixelSize, textColor, subtitleColor)
            SpecRow("35mm equivalent focal length", camera.focalLength35mm, textColor, subtitleColor)
            SpecRow("Shutter speed", camera.shutterSpeedRange, textColor, subtitleColor)
            SpecRow("ISO sensitivity range", camera.isoRange, textColor, subtitleColor)
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
            // Boolean capabilities with icons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureRow("Optical image stabilization", camera.hasOpticalStabilization, textColor, accentColor)
                FeatureRow("Flash", camera.hasFlash, textColor, accentColor)
            }
            
            if (camera.capabilities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                // Expandable or simple list for "More"
                // For now, keeping it simple but cleaner
                 Text(
                    text = "More",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
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
            Text(
                text = "Video capture",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Video profiles
            if (camera.videoProfiles.isNotEmpty()) {
                Text(
                    text = "Profiles",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Video features
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureRow("High speed video", camera.hasHighSpeedVideo, textColor, accentColor)
                FeatureRow("Video stabilization", camera.hasVideoStabilization, textColor, accentColor)
                FeatureRow("HDR10 support", camera.hasHdr, textColor, accentColor)
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
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
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
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor
        )
    }
}
