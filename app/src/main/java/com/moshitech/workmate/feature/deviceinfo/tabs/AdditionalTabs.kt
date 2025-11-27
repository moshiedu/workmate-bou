package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.components.DetailRow
import com.moshitech.workmate.feature.deviceinfo.components.ExpandableSection
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

// BatteryTab is now in its own file: tabs/BatteryTab.kt

//@Composable
//fun NetworkTab(
//    viewModel: DeviceInfoViewModel,
//    isDark: Boolean,
//    textColor: Color
//) {
//    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
//    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        ExpandableSection(
//            title = "Network Information",
//            subtitle = "WiFi, Mobile Data, Bluetooth",
//            cardColor = cardColor,
//            textColor = textColor,
//            subtitleColor = subtitleColor,
//            initiallyExpanded = true
//        ) {
//            Box(
//                modifier = Modifier.fillMaxWidth().padding(32.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Network monitoring features coming soon", color = subtitleColor)
//            }
//        }
//    }
//}

// CameraTab is now in its own file: tabs/CameraTab.kt

// SensorsTab is now in its own file: tabs/SensorsTab.kt

// AppsTab is now in its own file: tabs/AppsTab.kt
