package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.moshitech.workmate.feature.deviceinfo.DeviceInfoViewModel
import com.moshitech.workmate.feature.deviceinfo.components.AppDetailsModal
import com.moshitech.workmate.feature.deviceinfo.model.AppFilter
import com.moshitech.workmate.feature.deviceinfo.model.AppInfo

@Composable
fun AppsTab(
    viewModel: DeviceInfoViewModel,
    isDark: Boolean,
    textColor: Color
) {
    val apps by viewModel.apps.collectAsState()
    val appsLoading by viewModel.appsLoading.collectAsState()
    var selectedFilter by remember { mutableStateOf(AppFilter.USER) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8F9FA)
    val accentColor = Color(0xFF10B981)
    
    LaunchedEffect(selectedFilter) {
        viewModel.loadApps(selectedFilter)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Tabs
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterTab(
                    icon = Icons.Default.Person,
                    label = "User",
                    count = if (selectedFilter == AppFilter.USER) apps.size else null,
                    isSelected = selectedFilter == AppFilter.USER,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.USER
                }
                
                FilterTab(
                    icon = Icons.Default.Android,
                    label = "System",
                    count = if (selectedFilter == AppFilter.SYSTEM) apps.size else null,
                    isSelected = selectedFilter == AppFilter.SYSTEM,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.SYSTEM
                }
                
                FilterTab(
                    icon = Icons.Default.Apps,
                    label = "All",
                    count = if (selectedFilter == AppFilter.ALL) apps.size else null,
                    isSelected = selectedFilter == AppFilter.ALL,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.ALL
                }
            }
        }
        
        // Apps List
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (appsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(apps) { app ->
                        AppListItem(
                            app = app,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            accentColor = accentColor,
                            onClick = { selectedApp = app }
                        )
                    }
                }
            }
        }
    }
    
    // App Details Modal
    selectedApp?.let { app ->
        AppDetailsModal(
            app = app,
            isDark = isDark,
            textColor = textColor,
            onDismiss = { selectedApp = null }
        )
    }
}

@Composable
fun FilterTab(
    icon: ImageVector,
    label: String,
    count: Int?,
    isSelected: Boolean,
    accentColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) accentColor else subtitleColor
    
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
            count?.let {
                Text(
                    text = "($it)",
                    fontSize = 10.sp,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppInfo,
    textColor: Color,
    subtitleColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(48.dp)
                )
            } ?: Icon(
                imageVector = Icons.Default.Android,
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp),
                tint = subtitleColor
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (app.isUpdatedSystemApp) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UPDATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                
                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
