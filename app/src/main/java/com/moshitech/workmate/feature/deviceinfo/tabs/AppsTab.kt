package com.moshitech.workmate.feature.deviceinfo.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
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
    var searchQuery by remember { mutableStateOf("") }
    
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val accentColor = Color(0xFF10B981)
    
    // Filter apps based on search query
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    LaunchedEffect(selectedFilter) {
        viewModel.loadApps(selectedFilter)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter Tabs - Ultra Compact Design
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Reduced gap
            ) {
                FilterTab(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Person,
                    label = "User",
                    count = if (selectedFilter == AppFilter.USER) filteredApps.size else null,
                    isSelected = selectedFilter == AppFilter.USER,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.USER
                }
                
                FilterTab(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Android,
                    label = "System",
                    count = if (selectedFilter == AppFilter.SYSTEM) filteredApps.size else null,
                    isSelected = selectedFilter == AppFilter.SYSTEM,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.SYSTEM
                }
                
                FilterTab(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Apps,
                    label = "All",
                    count = if (selectedFilter == AppFilter.ALL) filteredApps.size else null,
                    isSelected = selectedFilter == AppFilter.ALL,
                    accentColor = accentColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor
                ) {
                    selectedFilter = AppFilter.ALL
                }
            }
        }
        
        // Search Bar - Ultra Compact Design
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Compact padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = subtitleColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search apps...",
                            color = subtitleColor,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(accentColor),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = subtitleColor,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }
        
        // Apps List
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (appsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No apps found" else "No apps",
                        color = subtitleColor,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredApps) { app ->
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
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .height(56.dp) // Fixed reduced height
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (count != null) "$label ($count)" else label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(40, 40).asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(40.dp)
                )
            } ?: Icon(
                imageVector = Icons.Default.Android,
                contentDescription = app.appName,
                modifier = Modifier.size(40.dp),
                tint = subtitleColor
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (app.isUpdatedSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UPDATED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
